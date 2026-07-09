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
CURRENT_TAG_FILE="$DEPLOY_DIR/current_tag"
CURRENT_ENV_FILE="$DEPLOY_DIR/current_env"

mkdir -p "$ENV_DIR" "$NGINX_DIR"
umask 077

get_current_color() {
  if [ -f "$CURRENT_COLOR_FILE" ]; then
    cat "$CURRENT_COLOR_FILE"
  else
    echo ""
  fi
}

get_target_color() {
  local current_color="$1"
  if [ "$current_color" = "blue" ]; then
    echo "green"
  else
    echo "blue"
  fi
}

service_name() {
  local color="$1"
  echo "amumal_${color}"
}

color_tag_file() {
  local color="$1"
  echo "$DEPLOY_DIR/${color}_tag"
}

color_env_file() {
  local color="$1"
  echo "$DEPLOY_DIR/${color}_env"
}

get_color_tag() {
  local color="$1"
  local file
  file=$(color_tag_file "$color")
  if [ -f "$file" ]; then
    cat "$file"
  else
    echo ""
  fi
}

get_color_env_file() {
  local color="$1"
  local file
  file=$(color_env_file "$color")
  if [ -f "$file" ]; then
    cat "$file"
  else
    echo ".env"
  fi
}

write_env_snapshot() {
  local tag="$1"
  local env_path="$ENV_DIR/${tag}.env"
  printf '%s' "$BACKEND_ENV_B64" | base64 -d > "$env_path"
  chmod 600 "$env_path"
  echo "$env_path"
}

write_upstream_file() {
  local color="$1"
  local target_service
  target_service=$(service_name "$color")
  cat > "$UPSTREAM_FILE" <<EOF
proxy_pass http://${target_service}:8080;
EOF
  chmod 600 "$UPSTREAM_FILE"
}

check_health() {
  local service="$1"
  for i in $(seq 1 12); do
    sleep 5
    local container_id
    container_id=$(docker compose ps -q "$service" 2>/dev/null || true)
    if [ -z "$container_id" ]; then
      continue
    fi

    local status
    status=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id" 2>/dev/null || echo "missing")
    if [ "$status" = "healthy" ]; then
      return 0
    fi
  done
  return 1
}

reload_nginx() {
  docker compose up -d nginx
  docker compose exec -T nginx nginx -s reload
}

CURRENT_COLOR=$(get_current_color)
TARGET_COLOR=$(get_target_color "$CURRENT_COLOR")
TARGET_SERVICE=$(service_name "$TARGET_COLOR")

CURRENT_TAG=$(get_color_tag "$CURRENT_COLOR")
CURRENT_ENV_PATH=$(get_color_env_file "$CURRENT_COLOR")
if [ -n "$BACKEND_ENV_B64" ]; then
  NEW_ENV_PATH=$(write_env_snapshot "$NEW_TAG")
else
  NEW_ENV_PATH="$CURRENT_ENV_PATH"
fi

# 비활성 색상에 새 버전 배포
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
echo "$NEW_TAG" > "$(color_tag_file "$TARGET_COLOR")"
echo "$NEW_ENV_PATH" > "$(color_env_file "$TARGET_COLOR")"
echo "$NEW_TAG" > "$CURRENT_TAG_FILE"
echo "$NEW_ENV_PATH" > "$CURRENT_ENV_FILE"

echo "블루-그린 배포 성공: $TARGET_COLOR -> $NEW_TAG"