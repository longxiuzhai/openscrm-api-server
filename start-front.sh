#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
FRONT_DIR="${SCRIPT_DIR}/front/openscrm-dashboard"

if ! command -v npm >/dev/null 2>&1; then
  echo "错误：未找到 npm，请先安装 Node.js 和 npm。" >&2
  exit 1
fi

if [[ ! -f "${FRONT_DIR}/package.json" ]]; then
  echo "错误：未找到前端项目：${FRONT_DIR}" >&2
  exit 1
fi

if [[ ! -d "${FRONT_DIR}/node_modules" ]]; then
  echo "错误：前端依赖尚未安装，请先运行：" >&2
  echo "  cd \"${FRONT_DIR}\" && npm install" >&2
  exit 1
fi

cd "${FRONT_DIR}"

case " ${NODE_OPTIONS:-} " in
  *" --openssl-legacy-provider "*) ;;
  *) export NODE_OPTIONS="${NODE_OPTIONS:+${NODE_OPTIONS} }--openssl-legacy-provider" ;;
esac

echo "启动 OpenSCRM 前端：http://localhost:9000"
echo "后端代理地址：http://127.0.0.1:9001"
exec npm run dev
