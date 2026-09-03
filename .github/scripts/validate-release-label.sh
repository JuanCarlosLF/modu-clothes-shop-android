#!/usr/bin/env bash

set -euo pipefail

if [[ "${HAS_RELEASE_LABEL:-false}" == "true" ]]; then
  echo "Release label found"
  exit 0
fi

echo "::error::The pull request must have the release label"
exit 1
