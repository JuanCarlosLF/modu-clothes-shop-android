#!/usr/bin/env bash

set -euo pipefail

build_file="Project/app/build.gradle.kts"
base_sha="${BASE_SHA:?BASE_SHA is required}"

if [[ ! -f "$build_file" ]]; then
  echo "::error::The pull request does not contain $build_file"
  exit 1
fi

pr_build_content="$(cat "$build_file")"
if ! base_build_content="$(git show "$base_sha:$build_file")"; then
  echo "::error::Unable to read $build_file from the base commit $base_sha"
  exit 1
fi

extract_single_value() {
  local content="$1"
  local property="$2"
  local matching_lines
  local value

  matching_lines="$(printf '%s\n' "$content" | grep -E "^[[:space:]]*$property[[:space:]]*=" || true)"
  if [[ "$(printf '%s\n' "$matching_lines" | sed '/^[[:space:]]*$/d' | wc -l)" -ne 1 ]]; then
    echo "::error::$property must be declared exactly once"
    exit 1
  fi

  if [[ "$property" == "versionCode" ]]; then
    value="$(printf '%s\n' "$matching_lines" | sed -E 's/.*=[[:space:]]*([0-9]+).*/\1/')"
  else
    value="$(printf '%s\n' "$matching_lines" | sed -E 's/.*=[[:space:]]*"([^"]+)".*/\1/')"
  fi

  if [[ -z "$value" ]]; then
    echo "::error::Unable to extract $property"
    exit 1
  fi

  printf '%s' "$value"
}

pr_version_code="$(extract_single_value "$pr_build_content" "versionCode")"
base_version_code="$(extract_single_value "$base_build_content" "versionCode")"
pr_version_name="$(extract_single_value "$pr_build_content" "versionName")"
base_version_name="$(extract_single_value "$base_build_content" "versionName")"

if ! [[ "$pr_version_code" =~ ^[1-9][0-9]*$ ]]; then
  echo "::error::PR versionCode must be a positive integer"
  exit 1
fi

if ! [[ "$base_version_code" =~ ^[1-9][0-9]*$ ]]; then
  echo "::error::main versionCode must be a positive integer"
  exit 1
fi

if (( pr_version_code <= base_version_code )); then
  echo "::error::PR versionCode ($pr_version_code) must be greater than main versionCode ($base_version_code)"
  exit 1
fi

if ! [[ "$pr_version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "::error::PR versionName must use major.minor.patch format"
  exit 1
fi

if ! [[ "$base_version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "::error::main versionName must use major.minor.patch format"
  exit 1
fi

highest_version="$(printf '%s\n' "$base_version_name" "$pr_version_name" | sort -V | tail -n 1)"
if [[ "$pr_version_name" == "$base_version_name" || "$highest_version" != "$pr_version_name" ]]; then
  echo "::error::PR versionName ($pr_version_name) must be greater than main versionName ($base_version_name)"
  exit 1
fi

release_tag="v$pr_version_name"
if git ls-remote --exit-code --tags origin "refs/tags/$release_tag" >/dev/null 2>&1; then
  echo "::error::Release tag $release_tag already exists"
  exit 1
fi

{
  echo "RELEASE_VERSION_NAME=$pr_version_name"
  echo "RELEASE_VERSION_CODE=$pr_version_code"
  echo "RELEASE_TAG=$release_tag"
  echo "RELEASE_NOTES_PATH=Project/docs/releases/v$pr_version_name.md"
} >> "${GITHUB_ENV:?GITHUB_ENV is required}"

echo "Validated release $release_tag (versionCode $pr_version_code)"
