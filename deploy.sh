#!/bin/bash
set -e
NEW_TAG=$1
BACKEND_ENV_B64=${2:-}
APP_DIR=${APP_DIR:-/home/ubuntu/amumal}
mkdir -p "$APP_DIR"
cd "$APP_DIR"

DEPLOY_DIR=".deploy"
ENV_DIR="$DEPLOY_DIR/env"
NGINX_DIR="$DEPLOY_DIR/nginx"
NGINX_CONFIG_DIR="nginx"
UPSTREAM_FILE="$NGINX_DIR/app-upstream.inc"
CURRENT_COLOR_FILE="$DEPLOY_DIR/current_color"

mkdir -p "$ENV_DIR" "$NGINX_DIR" "$NGINX_CONFIG_DIR"
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

get_server_name() {
  local env_path="$1"
  local server_url=""

  if [ -f "$env_path" ]; then
    server_url=$(grep -E '^SERVER_URL=' "$env_path" | tail -n 1 | cut -d= -f2- | tr -d '\r')
  fi

  server_url=${server_url#http://}
  server_url=${server_url#https://}
  server_url=${server_url%%/*}
  server_url=${server_url%%:*}

  echo "${server_url:-api.sub.amon.p-e.kr}"
}

ensure_nginx_config() {
  local server_name="$1"
  local cert_file="/etc/letsencrypt/live/${server_name}/fullchain.pem"
  local key_file="/etc/letsencrypt/live/${server_name}/privkey.pem"

  # HTTPS 배포에서 인증서가 없는 상태를 HTTP로 묵시적으로 폴백하지 않는다.
  if [ ! -f "$cert_file" ] || [ ! -f "$key_file" ]; then
    echo "TLS 인증서를 찾을 수 없습니다: $server_name" >&2
    return 1
  fi

  cat > "$NGINX_CONFIG_DIR/default.conf" <<EOF
server {
    listen 80;
    server_name ${server_name};

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name ${server_name};

    ssl_certificate ${cert_file};
    ssl_certificate_key ${key_file};

    location / {
        include /etc/nginx/conf.d/app-upstream.inc;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF
  chmod 600 "$NGINX_CONFIG_DIR/default.conf"
}

reload_nginx() {
  ensure_nginx_config "$1"
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
SERVER_NAME=$(get_server_name "$NEW_ENV_PATH")

export IMAGE_TAG=$NEW_TAG
export APP_ENV_FILE="$NEW_ENV_PATH"
docker compose pull "$TARGET_SERVICE"
if ! docker compose up -d --wait --wait-timeout 60 --remove-orphans "$TARGET_SERVICE"; then
  echo "새 버전 헬스체크 실패: $TARGET_SERVICE"
  docker compose ps || true
  docker compose logs --no-color --tail=200 "$TARGET_SERVICE" || true
  docker compose stop "$TARGET_SERVICE" || true
  exit 1
fi

write_upstream_file "$TARGET_COLOR"
if ! reload_nginx "$SERVER_NAME"; then
  echo "Nginx 트래픽 전환 실패: $TARGET_COLOR"
  if [ -n "$CURRENT_COLOR" ]; then
    write_upstream_file "$CURRENT_COLOR"
    reload_nginx "$SERVER_NAME" || true
  fi
  exit 2
fi

echo "$TARGET_COLOR" > "$CURRENT_COLOR_FILE"
echo "$NEW_TAG" > "$(state_file tag "$TARGET_COLOR")"
echo "$NEW_ENV_PATH" > "$(state_file env "$TARGET_COLOR")"

# 새 컨테이너 헬스체크와 Nginx 트래픽 전환이 모두 성공했으므로 직전 컨테이너는 즉시 stop -> 빠른 복구 가능
if [ -n "$CURRENT_COLOR" ]; then
  docker compose stop "$(service_name "$CURRENT_COLOR")"
fi

# 두 컨테이너가 참조하지 않는 직전의 직전 이미지 정리
docker image prune -a -f

echo "블루-그린 배포 성공: $TARGET_COLOR -> $NEW_TAG"
