#!/bin/sh
set -e

# Map *_FILE env vars to regular env vars (Docker secrets support)
for var in DB_PASSWORD MINIO_SECRET_KEY JWT_SECRET; do
  file_var="${var}_FILE"
  eval "file_path=\"\$$file_var\""
  if [ -n "$file_path" ] && [ -f "$file_path" ]; then
    export "$var"=$(cat "$file_path")
  fi
done

exec "$@"
