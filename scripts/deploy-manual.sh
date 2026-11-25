#!/bin/sh
set -eu
read_setting() {
    if [ -f .env ]; then
        sed -n "s/^$1=//p" .env | tail -n 1
    fi
}
stored_project=$(read_setting COMPOSE_PROJECT_NAME)
stored_registry=$(read_setting REGISTRY_IMAGE)
stored_release=$(read_setting RELEASE_TAG)
export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-${stored_project:-store-yasakovd}}"
export REGISTRY_IMAGE="${REGISTRY_IMAGE:-${stored_registry:-store-yasakovd}}"
export RELEASE_TAG="${RELEASE_TAG:-${stored_release:-manual}}"
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
