#!/usr/bin/env bash

set -euo pipefail

notes_path="${RELEASE_NOTES_PATH:?RELEASE_NOTES_PATH is required}"

if [[ ! -f "$notes_path" ]]; then
  echo "::error::Release notes file does not exist: $notes_path"
  exit 1
fi

if ! grep -q '[^[:space:]]' "$notes_path"; then
  echo "::error::Release notes file is empty: $notes_path"
  exit 1
fi

echo "Release notes found: $notes_path"
