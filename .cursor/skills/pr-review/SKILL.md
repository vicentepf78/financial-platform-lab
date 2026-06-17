---
name: pr-review
description: Review GitHub pull requests. Use only when explicitly asked to review a PR.
---

# PR Review

Use this skill only for explicit PR review requests, such as "review PR #12" or "revisar este PR".

The repository CI version of this protocol lives in:

- `.github/workflows/ai-pr-review.yml`
- `.github/scripts/ai-pr-review.mjs`
- `.github/prompts/pr-review.md`
- `.github/prompts/pr-review.overlay.md` when a project needs custom rules

## Protocol

1. Identify the PR number.
2. Fetch PR metadata and diff with `gh`.
3. Read the generic prompt, optional project overlay, and configured repository context files.
4. Review the diff across six dimensions:
   - security
   - requirements
   - tests
   - architecture
   - regression
   - performance
5. Report only findings with at least 80% confidence.
6. Comment only on added diff lines and avoid duplicates near existing `<!-- ai-pr-review:* -->` comments.
7. Never approve, request changes, merge, push, or modify files as part of the review.

For GitHub Actions behavior and configuration, see `docs/ai-pr-review.md`.
