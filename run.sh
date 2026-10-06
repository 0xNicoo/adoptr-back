#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

usage() {
  printf 'Uso: ./run.sh [local|develop]\n'
  printf '  local   Docker + PostgreSQL del host (Linux), con .env.local. Por defecto.\n'
  printf '  develop Docker + PostgreSQL remoto, con .env.develop. No despliega a Render.\n'
}

if [[ ${1:-} == --help || ${1:-} == -h ]]; then
  usage
  exit 0
fi
if (( $# > 1 )); then
  usage >&2
  exit 1
fi

environment=${1:-local}
case "$environment" in
  local|develop) ;;
  *) usage >&2; exit 1 ;;
esac

env_file=".env.$environment"
if [[ ! -f "$env_file" ]]; then
  printf 'Falta %s. Copiá %s.example a %s y completá sus valores.\n' \
    "$env_file" "$env_file" "$env_file" >&2
  exit 1
fi

# Read only PORT, never execute/source an env file containing credentials.
port=$(awk '/^PORT=/ { sub(/^PORT=/, ""); sub(/\r$/, ""); value=$0 } END { print value }' "$env_file")
port=${port:-8081}
if [[ ! "$port" =~ ^[0-9]{1,5}$ ]] || (( 10#$port < 1 || 10#$port > 65535 )); then
  printf 'PORT debe ser un número entre 1 y 65535.\n' >&2
  exit 1
fi
port=$((10#$port))

network_args=()
if [[ "$environment" == local ]]; then
  if [[ $(uname -s) != Linux ]]; then
    printf 'El modo local usa --network host en Linux. En otros sistemas adaptá la conexión al host.\n' >&2
    exit 1
  fi
  network_args=(--network host)
else
  # Remote database: only expose the backend on the local machine.
  network_args=(-p "127.0.0.1:$port:$port")
fi

command -v docker >/dev/null || { printf 'Docker no está instalado.\n' >&2; exit 1; }
image="adoptr-back:$environment"
docker build -t "$image" .
printf 'Iniciando %s en http://localhost:%s (Ctrl+C para detener).\n' "$environment" "$port"
exec docker run --rm --name "adoptr-back-$environment" \
  --env-file "$env_file" --env "PORT=$port" \
  "${network_args[@]}" "$image"
