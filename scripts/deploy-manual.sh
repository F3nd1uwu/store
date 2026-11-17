#!/bin/sh
set -eu
export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-store-yasakovd}"
export REGISTRY_IMAGE="${REGISTRY_IMAGE:-store-yasakovd}"
export RELEASE_TAG="${RELEASE_TAG:-manual}"
docker compose version
for service in auth-service order-service inventory-service; do
    docker build -f Dockerfile.runtime --build-arg SERVICE="$service" -t "$REGISTRY_IMAGE/$service:$RELEASE_TAG" .
done
if [ "${1:-}" = "--proxy" ]; then
    docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml up -d --wait
    docker compose -f docker-compose.prod.yml -f docker-compose.proxy.yml ps
else
    docker compose -f docker-compose.prod.yml up -d --wait
    docker compose -f docker-compose.prod.yml ps
fi
