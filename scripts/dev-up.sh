#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"${ROOT_DIR}/scripts/compose.sh" up -d postgres kafka kafka-ui

echo "Infrastructure ready:"
echo "  PostgreSQL: localhost:5432 (financial/financial)"
echo "  Kafka:      localhost:9092"
echo "  Kafka UI:   http://localhost:8081"
