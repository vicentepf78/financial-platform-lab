import { readdir, readFile } from "node:fs/promises";
import { join } from "node:path";

const ownerRepo = process.env.GITHUB_REPOSITORY;
const token = process.env.GITHUB_TOKEN;
const modelToken = process.env.AI_API_KEY || token;
const prNumber = process.env.PR_NUMBER;
const apiUrl = process.env.GITHUB_API_URL || "https://api.github.com";
const modelBaseUrl = (process.env.AI_BASE_URL || "https://models.github.ai/inference").replace(/\/$/, "");
const model = process.env.AI_MODEL || "openai/gpt-4o-mini";
const promptPath = process.env.AI_REVIEW_PROMPT_PATH || ".github/prompts/pr-review.md";
const overlayPath = process.env.AI_REVIEW_OVERLAY_PATH || ".github/prompts/pr-review.overlay.md";
const commentMarker = process.env.AI_REVIEW_COMMENT_MARKER || "ai-pr-review";
const maxContextChars = Number(process.env.AI_REVIEW_MAX_CONTEXT_CHARS || 12000);
const maxPatchChars = Number(process.env.AI_REVIEW_MAX_PATCH_CHARS || 20000);
const maxRequestTokens = Number(process.env.AI_REVIEW_MAX_REQUEST_TOKENS || 4500);
const charsPerToken = Number(process.env.AI_REVIEW_CHARS_PER_TOKEN || 2);
const scriptVersion = "2026-06-17-base-tip-v4";
const maxComments = Number(process.env.AI_REVIEW_MAX_COMMENTS || 40);

const dimensions = (process.env.AI_REVIEW_DIMENSIONS || "security,requirements,tests,architecture,regression,performance")
  .split(",")
  .map((dimension) => dimension.trim())
  .filter(Boolean);

const defaultContextPaths = [
  "AGENTS.md",
  ".github/prompts/pr-review.overlay.md",
  ".specs/codebase/INDEX.md",
  ".specs/codebase/TESTING.md",
  ".specs/codebase/CONVENTIONS.md",
];

const severityLabels = {
  security: "Security",
  critical: "Critical",
  performance: "Performance",
  warning: "Warning",
  suggestion: "Suggestion",
};

if (!ownerRepo || !token || !prNumber) {
  throw new Error("GITHUB_REPOSITORY, GITHUB_TOKEN, and PR_NUMBER are required.");
}

const [owner, repo] = ownerRepo.split("/");

async function github(path, options = {}) {
  const response = await fetch(`${apiUrl}${path}`, {
    ...options,
    headers: {
      Accept: "application/vnd.github+json",
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-GitHub-Api-Version": "2022-11-28",
      ...(options.headers || {}),
    },
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`GitHub API ${response.status} for ${path}: ${body}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function githubPaginated(path) {
  const results = [];

  for (let page = 1; page <= 10; page += 1) {
    const separator = path.includes("?") ? "&" : "?";
    const batch = await github(`${path}${separator}per_page=100&page=${page}`);
    results.push(...batch);

    if (batch.length < 100) {
      break;
    }
  }

  return results;
}

async function readOptional(path) {
  try {
    return await readFile(path, "utf8");
  } catch (error) {
    if (error.code === "ENOENT" || error.code === "ENOTDIR") {
      return "";
    }

    throw error;
  }
}

async function listMarkdownFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true }).catch((error) => {
    if (error.code === "ENOENT" || error.code === "ENOTDIR") {
      return [];
    }

    throw error;
  });

  const files = [];

  for (const entry of entries) {
    const path = join(directory, entry.name);

    if (entry.isDirectory()) {
      files.push(...(await listMarkdownFiles(path)));
      continue;
    }

    if (entry.isFile() && entry.name.endsWith(".md")) {
      files.push(path);
    }
  }

  return files;
}

async function expandContextPath(pattern) {
  if (pattern.endsWith("/**/*.md")) {
    return listMarkdownFiles(pattern.slice(0, -"**/*.md".length));
  }

  if (pattern.endsWith("/*.md")) {
    const directory = pattern.slice(0, -"*.md".length);
    const entries = await readdir(directory, { withFileTypes: true }).catch((error) => {
      if (error.code === "ENOENT" || error.code === "ENOTDIR") {
        return [];
      }

      throw error;
    });

    return entries
      .filter((entry) => entry.isFile() && entry.name.endsWith(".md"))
      .map((entry) => join(directory, entry.name));
  }

  return [pattern];
}

async function loadRepositoryContext() {
  const configuredPaths = (process.env.AI_REVIEW_CONTEXT_PATHS || "")
    .split(",")
    .map((path) => path.trim())
    .filter(Boolean);
  const patterns = configuredPaths.length > 0 ? configuredPaths : defaultContextPaths;
  const expanded = await Promise.all(patterns.map(expandContextPath));
  const paths = [...new Set(expanded.flat())];
  const files = await Promise.all(
    paths.map(async (path) => {
      const content = await readOptional(path);
      return content ? `FILE: ${path}\n${content}` : "";
    })
  );

  return files.filter(Boolean).join("\n\n---\n\n").slice(0, maxContextChars);
}

function parseAddedLines(patch) {
  const added = new Set();
  let newLine = 0;

  for (const line of (patch || "").split("\n")) {
    if (line === "") {
      continue;
    }

    const hunk = line.match(/^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@/);
    if (hunk) {
      newLine = Number(hunk[1]) - 1;
      continue;
    }

    if (line.startsWith("+++") || line.startsWith("---")) {
      continue;
    }

    if (line.startsWith("+")) {
      newLine += 1;
      added.add(newLine);
      continue;
    }

    if (!line.startsWith("-")) {
      newLine += 1;
    }
  }

  return added;
}

function formatChangedFiles(files) {
  let output = "";

  for (const file of files) {
    if (!file.patch) {
      continue;
    }

    const next = [
      `FILE: ${file.filename}`,
      `STATUS: ${file.status}`,
      "PATCH:",
      file.patch,
      "",
    ].join("\n");

    if (output.length + next.length > maxPatchChars) {
      output += "\n[Diff truncated because AI_REVIEW_MAX_PATCH_CHARS was reached.]\n";
      break;
    }

    output += next;
  }

  return output;
}

function extractJson(text) {
  const trimmed = text.trim();
  const fenced = trimmed.match(/^```(?:json)?\s*([\s\S]*?)\s*```$/);
  return JSON.parse(fenced ? fenced[1] : trimmed);
}

function estimateTokens(text) {
  return Math.ceil(String(text || "").length / charsPerToken);
}

function buildRequestBody(messages) {
  return {
    model,
    messages,
    temperature: 0.1,
    response_format: { type: "json_object" },
  };
}

function estimateRequestBodyTokens(messages) {
  const serialized = JSON.stringify(buildRequestBody(messages));
  return Math.ceil(serialized.length / charsPerToken);
}

function truncateToTokenBudget(text, maxTokens, suffix) {
  const content = String(text || "");

  if (maxTokens <= 0) {
    return "";
  }

  if (estimateTokens(content) <= maxTokens) {
    return content;
  }

  const suffixText = suffix ?? "\n\n[Truncated to fit model token limit.]";
  const suffixTokens = estimateTokens(suffixText);
  const bodyBudget = Math.max(0, maxTokens - suffixTokens);
  let low = 0;
  let high = content.length;

  while (low < high) {
    const mid = Math.ceil((low + high) / 2);

    if (estimateTokens(content.slice(0, mid)) <= bodyBudget) {
      low = mid;
    } else {
      high = mid - 1;
    }
  }

  return `${content.slice(0, low)}${suffixText}`;
}

function buildPrHeader(pr) {
  return [
    `PR #${pr.number}: ${pr.title}`,
    `Base branch: ${pr.base.ref} (${pr.base.sha})`,
    `Head branch: ${pr.head.ref} (${pr.head.sha})`,
    "",
    "PR body:",
    pr.body || "(empty)",
  ].join("\n");
}

function buildDimensionMessages({ prompt, pr, contextText, patchText, dimension, budgetScale = 1 }) {
  const scaledMaxTokens = Math.max(1000, Math.floor(maxRequestTokens * budgetScale));
  const dimensionPrompt = `Run only the "${dimension}" review dimension. Return JSON with findings where type is "${dimension}".`;
  const prHeader = buildPrHeader(pr);

  const fixedTokens =
    estimateTokens(prompt) + estimateTokens(dimensionPrompt) + estimateTokens(prHeader) + 120;
  const contentBudget = Math.max(0, scaledMaxTokens - fixedTokens);
  const contextBudget = Math.floor(contentBudget * 0.35);
  const patchBudget = contentBudget - contextBudget;

  const truncatedContext = truncateToTokenBudget(
    contextText,
    contextBudget,
    "\n\n[Repository context truncated to fit model token limit.]"
  );
  const truncatedPatch = truncateToTokenBudget(
    patchText || "(No textual patch available.)",
    patchBudget,
    "\n\n[Diff truncated to fit model token limit.]"
  );

  const userContent = [
    prHeader,
    "",
    "Repository review context:",
    truncatedContext,
    "",
    "Changed files and patches:",
    truncatedPatch,
  ].join("\n");

  const messages = [
    { role: "system", content: prompt },
    { role: "user", content: userContent },
    { role: "user", content: dimensionPrompt },
  ];

  const estimatedTokens = estimateRequestBodyTokens(messages);
  if (estimatedTokens > scaledMaxTokens) {
    console.warn(
      `Estimated request body (${estimatedTokens} tokens) exceeds budget (${scaledMaxTokens}) for dimension "${dimension}".`
    );
  }

  return messages;
}

async function callModel(messages) {
  const response = await fetch(`${modelBaseUrl}/chat/completions`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${modelToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(buildRequestBody(messages)),
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(`AI model API ${response.status}: ${body}`);
  }

  const data = await response.json();
  const content = data.choices?.[0]?.message?.content;

  if (!content) {
    throw new Error("AI model response did not include message content.");
  }

  return extractJson(content);
}

function isTokenLimitError(error) {
  const message = String(error?.message || "");
  return message.includes("413") || message.includes("tokens_limit_reached") || message.includes("too large");
}

async function callModelForDimension(params, budgetScale = 1) {
  let scale = budgetScale;

  while (scale >= 0.35) {
    const messages = buildDimensionMessages({ ...params, budgetScale: scale });
    const requestTokens = estimateRequestBodyTokens(messages);

    if (requestTokens > maxRequestTokens) {
      console.warn(
        `Preflight shrink for "${params.dimension}": ~${requestTokens} tokens > ${maxRequestTokens} at scale ${scale.toFixed(2)}.`
      );
      scale *= 0.55;
      continue;
    }

    try {
      console.log(
        `Calling model for "${params.dimension}" at scale ${scale.toFixed(2)} (~${requestTokens} est. tokens).`
      );
      return await callModel(messages);
    } catch (error) {
      if (isTokenLimitError(error)) {
        console.warn(
          `Token limit reached for "${params.dimension}" at scale ${scale.toFixed(2)}; shrinking payload.`
        );
        scale *= 0.55;
        continue;
      }

      throw error;
    }
  }

  throw new Error(`Unable to fit "${params.dimension}" review request within model token limit.`);
}

function hasNearbyExistingComment(existingComments, path, line) {
  return existingComments.some((comment) => {
    if (comment.path !== path || !comment.body?.includes(`<!-- ${commentMarker}:`)) {
      return false;
    }

    return Math.abs(Number(comment.line || comment.original_line) - Number(line)) <= 3;
  });
}

function normalizeFinding(finding, expectedType, addedLinesByFile, existingComments) {
  if (!finding || finding.type !== expectedType) {
    return null;
  }

  if (!severityLabels[finding.severity] || Number(finding.confidence) < 0.8) {
    return null;
  }

  const path = String(finding.path || "");
  const line = Number(finding.line);

  if (!path || !Number.isInteger(line) || !addedLinesByFile.get(path)?.has(line)) {
    return null;
  }

  if (hasNearbyExistingComment(existingComments, path, line)) {
    return null;
  }

  const title = String(finding.title || "Review finding").trim();
  const body = String(finding.body || "").trim();

  if (!body) {
    return null;
  }

  return {
    path,
    line,
    side: "RIGHT",
    body: [
      `<!-- ${commentMarker}:${expectedType} -->`,
      `**${severityLabels[finding.severity]} - ${title}**`,
      "",
      body,
    ].join("\n"),
    severity: finding.severity,
    type: expectedType,
    title,
  };
}

function dedupeFindings(findings) {
  const seen = new Set();
  const unique = [];

  for (const finding of findings) {
    const key = `${finding.path}:${finding.line}:${finding.type}:${finding.title.toLowerCase()}`;
    if (!seen.has(key)) {
      seen.add(key);
      unique.push(finding);
    }
  }

  return unique;
}

function buildSummary(pr, reviews, comments) {
  const counts = comments.reduce((accumulator, comment) => {
    accumulator[comment.severity] = (accumulator[comment.severity] || 0) + 1;
    return accumulator;
  }, {});

  const highlights = reviews
    .flatMap((review) => review.highlights || [])
    .map((highlight) => String(highlight).trim())
    .filter(Boolean)
    .slice(0, 6);

  const sections = [
    "## AI Review Summary",
    "",
    `PR: #${pr.number} - ${pr.title}`,
    `Base branch: ${pr.base.ref}`,
    `Head branch: ${pr.head.ref}`,
    `Subreviews invoked: ${dimensions.join(", ")}`,
    `Findings posted: ${comments.length}`,
    "",
    "### Findings By Severity",
    `- Security: ${counts.security || 0}`,
    `- Critical: ${counts.critical || 0}`,
    `- Performance: ${counts.performance || 0}`,
    `- Warning: ${counts.warning || 0}`,
    `- Suggestion: ${counts.suggestion || 0}`,
  ];

  if (comments.length === 0) {
    sections.push("", "No inline findings passed the confidence and diff-line validation gates.");
  }

  if (highlights.length > 0) {
    sections.push("", "### Highlights", ...highlights.map((highlight) => `- ${highlight}`));
  }

  sections.push(
    "",
    "> Review is advisory and generated by the repository AI PR review workflow. Inline comments are limited to added lines in the PR diff."
  );

  return sections.join("\n");
}

async function main() {
  const [basePrompt, overlayPrompt, pr, files, existingComments] = await Promise.all([
    readFile(promptPath, "utf8"),
    readOptional(overlayPath),
    github(`/repos/${owner}/${repo}/pulls/${prNumber}`),
    githubPaginated(`/repos/${owner}/${repo}/pulls/${prNumber}/files`),
    githubPaginated(`/repos/${owner}/${repo}/pulls/${prNumber}/comments`),
  ]);

  const prompt = overlayPrompt
    ? `${basePrompt}\n\n## Project Overlay\n\n${overlayPrompt}`
    : basePrompt;
  const contextText = await loadRepositoryContext();

  const patchText = formatChangedFiles(files);
  const addedLinesByFile = new Map(files.map((file) => [file.filename, parseAddedLines(file.patch)]));

  console.log(
    `AI review script ${scriptVersion}; limits: context=${maxContextChars} chars, patch=${maxPatchChars} chars, request=${maxRequestTokens} tokens, model=${model}.`
  );

  const reviews = [];

  for (const dimension of dimensions) {
    try {
      const result = await callModelForDimension({
        prompt,
        pr,
        contextText,
        patchText,
        dimension,
      });
      reviews.push({ dimension, ...result });
    } catch (error) {
      console.error(`Dimension "${dimension}" failed:`, error);
      reviews.push({
        dimension,
        highlights: [`${dimension} review failed: ${error.message}`],
        findings: [],
      });
    }
  }

  const failedDimensions = reviews.filter((review) =>
    (review.highlights || []).some((highlight) => highlight.includes("review failed:"))
  );

  if (failedDimensions.length === dimensions.length) {
    throw new Error(
      `All AI review dimensions failed. Script ${scriptVersion}; model ${model}; request budget ${maxRequestTokens} tokens.`
    );
  }

  const comments = dedupeFindings(
    reviews.flatMap((review) =>
      (review.findings || [])
        .map((finding) => normalizeFinding(finding, review.dimension, addedLinesByFile, existingComments))
        .filter(Boolean)
    )
  ).slice(0, maxComments);

  const body = buildSummary(pr, reviews, comments);

  const reviewPayload = {
    event: "COMMENT",
    body,
  };

  if (comments.length > 0) {
    reviewPayload.comments = comments.map(({ path, line, side, body: commentBody }) => ({
      path,
      line,
      side,
      body: commentBody,
    }));
  }

  await github(`/repos/${owner}/${repo}/pulls/${prNumber}/reviews`, {
    method: "POST",
    body: JSON.stringify(reviewPayload),
  });

  console.log(`Posted AI review for PR #${prNumber} with ${comments.length} inline comments.`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
