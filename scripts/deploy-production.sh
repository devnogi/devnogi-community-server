#!/bin/sh
set -eu
umask 077
: "${IMAGE_REF:?missing image digest}" "${DOCKERHUB_USERNAME:?missing image owner}"
case "$IMAGE_REF" in *@sha256:*) ;; *) echo 'Invalid image digest' >&2; exit 1 ;; esac
test -f candidate/docker-compose-prod.yml
test -f .env.candidate
docker pull "$IMAGE_REF"
old_id=$(docker inspect --format '{{.Image}}' devnogi-community-server 2>/dev/null || true)
old_ref=''
if [ -n "$old_id" ]; then old_ref=$(docker image inspect --format '{{index .RepoDigests 0}}' "$old_id" 2>/dev/null || true); fi
case "$old_ref" in *@sha256:*) ;; *) old_ref='' ;; esac
previous_available=false
if [ -f docker-compose-prod.yml ] && [ -f .env.prod ]; then
  cp docker-compose-prod.yml .previous-compose.yml
  cp .env.prod .previous-env
  chmod 600 .previous-env
  previous_available=true
fi
port_from() {
  port=$(awk -F= '$1 == "SERVICE_PORT" { print substr($0, index($0, "=") + 1) }' "$1" | tail -n 1)
  case "$port" in ''|*[!0-9]*) return 1 ;; esac
  printf '%s' "$port"
}
health() {
  port=$(port_from "$1") || return 1
  for attempt in $(seq 1 "${HEALTH_ATTEMPTS:-60}"); do
    if "${CURL_CMD:-curl}" -fsS "http://127.0.0.1:$port/actuator/health" | grep -q '"status":"UP"'; then return 0; fi
    sleep "${HEALTH_DELAY:-5}"
  done
  return 1
}
rollback() {
  if [ "$previous_available" = true ]; then
    cp .previous-compose.yml docker-compose-prod.yml
    cp .previous-env .env.prod
  fi
  if [ -z "$old_ref" ] || [ "$previous_available" != true ]; then
    echo 'No previous immutable image, environment, or compose; automatic rollback unavailable' >&2
    return 1
  fi
  printf 'services:\n  backend:\n    image: %s\n' "$old_ref" > .rollback-image.yml
  if ! IMAGE_REF="$old_ref" DOCKER_IMAGE_TAG=latest docker compose -f .previous-compose.yml -f .rollback-image.yml --env-file .previous-env up -d --no-deps backend; then
    echo 'Rollback application failed' >&2; return 1
  fi
  if ! health .env.prod; then echo 'Rollback health check failed' >&2; return 1; fi
  echo 'Previous deployment restored and healthy'
}
cp candidate/docker-compose-prod.yml docker-compose-prod.yml
cp .env.candidate .env.prod
chmod 600 .env.prod
if ! docker compose -f docker-compose-prod.yml --env-file .env.prod up -d --no-deps backend || ! health .env.prod; then
  rollback || true
  exit 1
fi
if [ -n "$old_ref" ]; then printf '%s\n' "$old_ref" > .previous-image-ref; fi
printf '%s\n' "$IMAGE_REF" > .last-deployed-image-ref
