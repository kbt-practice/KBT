#!/bin/bash
set -e
NEW_TAG=$1
BACKEND_ENV_B64=${2:-}
cd /home/ubuntu/amumal

DEPLOY_DIR=".deploy"
ENV_DIR="$DEPLOY_DIR/env"
NGINX_DIR="$DEPLOY_DIR/nginx"
UPSTREAM_FILE="$NGINX_DIR/app-upstream.inc"
CURRENT_COLOR_FILE="$DEPLOY_DIR/current_color"

mkdir -p "$ENV_DIR" "$NGINX_DIR"
umask 077

# --- 상태 파일 헬퍼 (색상별 tag / env 경로를 하나로 통일) ---
state_file() { echo "$DEPLOY_DIR/${2}_${1}"; }   # state_file tag blue -> .deploy/blue_tag
read_state() { [ -f "$1" ] && cat "$1" || echo "$2"; }  # read_state file default

get_current_color() { read_state "$CURRENT_COLOR_FILE" ""; }
get_target_color()  { [ "$1" = "blue" ] && echo "green" || echo "blue"; }
service_name()      { echo "amumal_${1}"; }

write_env_snapshot() {
  local env_path="$ENV_DIR/${1}.env"
  printf '%s' "$BACKEND_ENV_B64" | base64 -d > "$env_path"
  chmod 600 "$env_path"
  echo "$env_path"
}

write_upstream_file() {
  cat > "$UPSTREAM_FILE" <<EOF
proxy_pass http://$(service_name "$1"):8080;
EOF
  chmod 600 "$UPSTREAM_FILE"
}

check_health() {
  local service="$1"
  for i in $(seq 1 12); do
    sleep 5
    local container_id
    container_id=$(docker compose ps -q "$service" 2>/dev/null || true)
    [ -z "$container_id" ] && continue
    local status
    status=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id" 2>/dev/null || echo "missing")
    [ "$status" = "healthy" ] && return 0
  done
  return 1
}

reload_nginx() {
  docker compose up -d nginx
  docker compose exec -T nginx nginx -s reload
}

# --- 배포 시작 ---
CURRENT_COLOR=$(get_current_color)
TARGET_COLOR=$(get_target_color "$CURRENT_COLOR")
TARGET_SERVICE=$(service_name "$TARGET_COLOR")

CURRENT_ENV_PATH=$(read_state "$(state_file env "$CURRENT_COLOR")" ".env")

if [ -n "$BACKEND_ENV_B64" ]; then
  NEW_ENV_PATH=$(write_env_snapshot "$NEW_TAG")
else
  NEW_ENV_PATH="$CURRENT_ENV_PATH"
fi

export IMAGE_TAG=$NEW_TAG
export APP_ENV_FILE="$NEW_ENV_PATH"
docker compose pull "$TARGET_SERVICE"
docker compose up -d "$TARGET_SERVICE"

if ! check_health "$TARGET_SERVICE"; then
  echo "새 버전 헬스체크 실패: $TARGET_SERVICE"
  docker compose stop "$TARGET_SERVICE" || true
  exit 1
fi

write_upstream_file "$TARGET_COLOR"
if ! reload_nginx; then
  echo "Nginx 트래픽 전환 실패: $TARGET_COLOR"
  if [ -n "$CURRENT_COLOR" ]; then
    write_upstream_file "$CURRENT_COLOR"
    reload_nginx || true
  fi
  exit 2
fi

echo "$TARGET_COLOR" > "$CURRENT_COLOR_FILE"
echo "$NEW_TAG" > "$(state_file tag "$TARGET_COLOR")"
echo "$NEW_ENV_PATH" > "$(state_file env "$TARGET_COLOR")"

echo "블루-그린 배포 성공: $TARGET_COLOR -> $NEW_TAG"