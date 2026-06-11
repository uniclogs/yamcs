#!/bin/bash
#
# Temporary workstation script to inject configs.
# For use until uniclogs configs are updated.
# 
# Needs `git submodule update --init --recursive` on first run.
# 

set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

YAMCS_DIR="$(cd "${HERE}/.." && pwd)"
ETC="${YAMCS_DIR}/src/main/yamcs/etc"

# src/main/yamcs is a submodule (uniclogs-configs)
if [ ! -d "${ETC}" ]; then
  echo "Missing ${ETC} - run: git submodule update --init --recursive" >&2
  exit 1
fi

COMPUTER_IP="$(ip route get 1.1.1.1 2>/dev/null | sed -n 's/.*src \([0-9.]*\).*/\1/p' | head -1)"

echo "Local IP: - $COMPUTER_IP - for remote groundstation"

cp "${YAMCS_DIR}/configs/oresat0_5-cfdp-overlay.yaml" "${ETC}/yamcs.oresat0_5.yaml"
cp "${YAMCS_DIR}/configs/extra_streams.sql" "${ETC}/extra_streams.sql"
trap 'git -C "${YAMCS_DIR}/src/main/yamcs" checkout -- etc/yamcs.oresat0_5.yaml etc/extra_streams.sql' EXIT

cd "${YAMCS_DIR}"
mvn yamcs:debug -o -DyamcsVersion=5.12.6 "$@"
