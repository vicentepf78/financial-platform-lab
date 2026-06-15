# Deploy Kubernetes

**Módulo:** `infra`  
**Sprint:** 7 — Kubernetes

## Problem Statement

A POC roda via Docker Compose, mas para demonstrar arquitetura cloud-native e escalabilidade, a aplicação precisa ser deployável em Kubernetes com manifests/Helm charts, rolling updates e autoscaling. Sem deploy K8s, o critério de sucesso #8 do projeto não é atingido.

## Goals

- [ ] Criar manifests Kubernetes (ou Helm chart) para backend, frontend e dependências
- [ ] Configurar rolling update para deploy sem downtime
- [ ] Configurar Horizontal Pod Autoscaler (HPA) baseado em CPU/memória
- [ ] Configurar ConfigMaps e Secrets para configuração
- [ ] Documentar processo de deploy em cluster local (kind/minikube) e cloud

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Multi-região / multi-cluster | Alta disponibilidade geográfica — futuro |
| GitOps com ArgoCD/Flux | Deploy manual/scripted na v1 |
| Service mesh (Istio) | Complexidade fora da POC |
| Cluster gerenciado em produção (EKS/GKE/AKS) | kind/minikube para demonstração |

---

## User Stories

### P1: Manifests Kubernetes para aplicação ⭐ MVP

**User Story**: Como DevOps, quero manifests Kubernetes para backend e frontend, para deployar a plataforma em cluster.

**Why P1**: Sem manifests, não há deploy K8s.

**Acceptance Criteria**:

1. WHEN DevOps aplica manifests em `infra/k8s/` THEN o cluster SHALL criar Deployments para: backend (Spring Boot), frontend (nginx/React) e dependências (PostgreSQL, Kafka, observability)
2. WHEN pods iniciam THEN o sistema SHALL configurar variáveis via ConfigMaps e Secrets (credenciais DB, Kafka, Mercado Pago)
3. WHEN Services são criados THEN backend SHALL ser acessível em `backend:8080` e frontend em `frontend:80` dentro do cluster
4. WHEN Ingress é configurado THEN a aplicação SHALL ser acessível externamente via hostname configurável

**Independent Test**: Deploy em kind/minikube e acessar frontend via Ingress.

---

### P2: Rolling update sem downtime

**User Story**: Como DevOps, quero realizar rolling update da aplicação sem downtime, para deploy seguro de novas versões.

**Why P2**: Prática essencial de cloud-native — diferencial do portfólio.

**Acceptance Criteria**:

1. WHEN nova versão da imagem é deployada THEN o Kubernetes SHALL executar rolling update com `maxUnavailable: 0` e `maxSurge: 1`
2. WHEN novo pod não passa readiness probe THEN o rollout SHALL ser pausado sem derrubar pods saudáveis
3. WHEN rollout falha THEN o DevOps SHALL poder executar `kubectl rollout undo` para reverter
4. WHEN deploy completa THEN todas as requisições durante o rollout SHALL ser atendidas sem erro 5xx

**Independent Test**: Deploy v1, depois v2 com rolling update e verificar zero downtime via curl contínuo.

---

### P3: Horizontal Pod Autoscaler (HPA)

**User Story**: Como DevOps, quero que o backend escale automaticamente sob carga, para demonstrar elasticidade.

**Why P3**: HPA é requisito explícito do Sprint 7 no roadmap.

**Acceptance Criteria**:

1. WHEN HPA está configurado para backend THEN o cluster SHALL manter entre `minReplicas: 2` e `maxReplicas: 5`
2. WHEN CPU média dos pods excede 70% por 2 minutos THEN o HPA SHALL adicionar réplicas
3. WHEN carga diminui THEN o HPA SHALL reduzir réplicas respeitando `minReplicas`
4. WHEN métricas server não está disponível (minikube) THEN documentação SHALL descrever alternativa (metrics-server)

---

## Edge Cases

- WHEN Secret não está configurado THEN pods SHALL falhar em CrashLoopBackOff com mensagem clara nos logs
- WHEN PostgreSQL em cluster é reiniciado THEN backend SHALL reconectar via connection pool (retry)
- WHEN imagem Docker não existe no registry THEN deploy SHALL falhar com ImagePullBackOff documentado
- WHEN recursos do cluster são insuficientes THEN HPA SHALL respeitar limites sem evictar pods críticos

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| K8S-01 | P1: Manifests backend e frontend | Design | Pending |
| K8S-02 | P1: ConfigMaps e Secrets | Design | Pending |
| K8S-03 | P1: Services e Ingress | Design | Pending |
| K8S-04 | P2: Rolling update | Design | Pending |
| K8S-05 | P2: Rollback | Design | Pending |
| K8S-06 | P3: HPA configuração | Design | Pending |
| K8S-07 | P3: Scale up/down | Design | Pending |
| K8S-08 | Edge: Secrets ausentes | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] Aplicação completa roda em cluster kind/minikube com um script de deploy
- [ ] Rolling update executado sem downtime verificável
- [ ] HPA escala backend de 2 para 3+ réplicas sob carga simulada
- [ ] Manifests versionados em `infra/k8s/` sem secrets commitados
