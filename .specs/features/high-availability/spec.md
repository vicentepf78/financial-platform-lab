# Alta Disponibilidade (Infra)

**Módulo:** `infra`  
**Sprint:** 7 — Kubernetes  
**Dependência:** `kubernetes-deploy`

## Problem Statement

Para operar em ambiente cloud-native com confiabilidade, a aplicação precisa de múltiplas réplicas, health checks, readiness probes e tolerância a falhas de pods individuais. Sem configuração de alta disponibilidade, um único pod com falha derruba o serviço.

## Goals

- [ ] Configurar réplicas mínimas para backend e frontend (≥ 2)
- [ ] Implementar liveness e readiness probes em todos os deployments
- [ ] Configurar Pod Disruption Budgets (PDB) para manter disponibilidade durante manutenção
- [ ] Configurar resource requests e limits para scheduling adequado
- [ ] Garantir que dependências stateful (PostgreSQL, Kafka) tenham estratégia de persistência

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| HA multi-região | Explicitamente fora do escopo v1 (PROJECT.md) |
| PostgreSQL cluster (Patroni/Citus) | Single instance com PVC na POC |
| Kafka cluster multi-broker | Single broker com PVC na POC |
| Zero-downtime para dependências stateful | Apenas app stateless com múltiplas réplicas |

---

## User Stories

### P1: Health checks e readiness probes ⭐ MVP

**User Story**: Como DevOps, quero liveness e readiness probes configurados, para que Kubernetes gerencie saúde dos pods automaticamente.

**Why P1**: Sem probes, K8s não sabe quando pod está pronto ou deve ser reiniciado.

**Acceptance Criteria**:

1. WHEN backend Deployment é criado THEN cada pod SHALL ter liveness probe em `GET /actuator/health/liveness` com `initialDelaySeconds: 30`, `periodSeconds: 10`
2. WHEN backend pod inicia THEN readiness probe em `GET /actuator/health/readiness` SHALL impedir tráfego até aplicação estar pronta (DB conectado)
3. WHEN liveness probe falha 3 vezes consecutivas THEN Kubernetes SHALL reiniciar o pod
4. WHEN readiness probe falha THEN o pod SHALL ser removido do Service endpoints até recuperar
5. WHEN frontend Deployment é criado THEN probe SHALL verificar `GET /` retornando 200

**Independent Test**: Matar processo Java dentro de um pod e verificar restart automático.

---

### P2: Múltiplas réplicas com PDB

**User Story**: Como DevOps, quero múltiplas réplicas com Pod Disruption Budget, para manter disponibilidade durante manutenção do cluster.

**Why P2**: Réplicas sem PDB permitem que manutenção derrube todos os pods simultaneamente.

**Acceptance Criteria**:

1. WHEN backend Deployment é configurado THEN `replicas` SHALL ser ≥ 2
2. WHEN frontend Deployment é configurado THEN `replicas` SHALL ser ≥ 2
3. WHEN PDB é aplicado para backend THEN `minAvailable: 1` SHALL impedir eviction de todos os pods simultaneamente
4. WHEN `kubectl drain` é executado em node THEN pelo menos 1 réplica de cada serviço SHALL permanecer disponível

**Independent Test**: Executar drain em node e verificar que API continua respondendo.

---

### P3: Resource management e persistência

**User Story**: Como DevOps, quero resource requests/limits e volumes persistentes para stateful services, para scheduling previsível e dados preservados.

**Why P3**: Sem resources, pods competem por CPU/memória; sem PVC, dados se perdem ao reiniciar.

**Acceptance Criteria**:

1. WHEN backend pod é schedulado THEN SHALL ter `requests: { cpu: 500m, memory: 512Mi }` e `limits: { cpu: 1000m, memory: 1Gi }`
2. WHEN PostgreSQL é deployado THEN SHALL usar PersistentVolumeClaim com `storageClass` configurável
3. WHEN Kafka é deployado THEN SHALL usar PVC para dados de tópicos
4. WHEN pod é evicted por falta de memória THEN Kubernetes SHALL respeitar limits sem afetar outros pods além do evicted

---

## Edge Cases

- WHEN todos os pods de backend falham readiness simultaneamente THEN Service SHALL retornar erro até pelo menos 1 recuperar (sem tráfego para pods unhealthy)
- WHEN PostgreSQL PVC está cheio THEN readiness do backend SHALL falhar com log indicando problema de disco
- WHEN node é perdido THEN Kubernetes SHALL reschedular pods em nodes saudáveis automaticamente
- WHEN cluster tem apenas 1 node (minikube) THEN PDB e anti-affinity SHALL degradar graciosamente (documentado)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| K8S-09 | P1: Liveness probe backend | Design | Pending |
| K8S-10 | P1: Readiness probe backend | Design | Pending |
| K8S-11 | P1: Probes frontend | Design | Pending |
| K8S-12 | P2: Réplicas mínimas | Design | Pending |
| K8S-13 | P2: Pod Disruption Budget | Design | Pending |
| K8S-14 | P3: Resource requests/limits | Design | Pending |
| K8S-15 | P3: PVC PostgreSQL e Kafka | Design | Pending |
| K8S-16 | Edge: Falha simultânea de pods | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] API responde com sucesso quando 1 de 2 réplicas de backend é derrubada
- [ ] Readiness impede tráfego para pods não prontos (verificável durante startup)
- [ ] Dados PostgreSQL persistem após restart do pod (PVC)
- [ ] `kubectl get pods` mostra todos os deployments com ≥ 2 réplicas ready
