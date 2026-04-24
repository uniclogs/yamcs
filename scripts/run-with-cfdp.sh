#!/usr/bin/env bash
set -euo pipefail
REPO="$(git rev-parse --show-toplevel)"
ETC="$REPO/src/main/yamcs/etc"

cp "$REPO/configs/oresat0_5-cfdp-overlay.yaml" "$ETC/yamcs.oresat0_5.yaml"
cp "$REPO/configs/extra_streams.sql" "$ETC/extra_streams.sql"
trap 'git -C "$REPO/src/main/yamcs" checkout -- etc/yamcs.oresat0_5.yaml etc/extra_streams.sql' EXIT

cd "$REPO"
mvn yamcs:debug -o -DyamcsVersion=5.12.6 "$@"
