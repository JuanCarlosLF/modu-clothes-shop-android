#!/usr/bin/env bash

set -euo pipefail

build_file="Project/app/build.gradle.kts"
expected_sha="${MERGE_COMMIT_SHA:?MERGE_COMMIT_SHA is required}"

if [[ "$(git rev-parse HEAD)" != "$expected_sha" ]]; then
  echo "::error::Checked out commit does not match the merged pull request commit"
  exit 1
fi

if [[ ! -f "$build_file" ]]; then
  echo "::error::Merged commit does not contain $build_file"
  exit 1
fi

extract_single_value() {
  local property="$1"
  local matching_lines
  local value

  matching_lines="$(grep -E "^[[:space:]]*$property[[:space:]]*=" "$build_file" || true)"
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

version_code="$(extract_single_value "versionCode")"
version_name="$(extract_single_value "versionName")"

if ! [[ "$version_code" =~ ^[1-9][0-9]*$ ]]; then
  echo "::error::versionCode must be a positive integer"
  exit 1
fi

if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "::error::versionName must use major.minor.patch format"
  exit 1
fi

release_tag="v$version_name"
if git ls-remote --exit-code --tags origin "refs/tags/$release_tag" >/dev/null 2>&1; then
  echo "::error::Release tag $release_tag already exists"
  exit 1
fi

{
  echo "RELEASE_VERSION_NAME=$version_name"
  echo "RELEASE_VERSION_CODE=$version_code"
  echo "RELEASE_TAG=$release_tag"
  echo "RELEASE_NOTES_PATH=Project/docs/releases/v$version_name.md"
} >> "${GITHUB_ENV:?GITHUB_ENV is required}"

{
  echo "version_name=$version_name"
  echo "version_code=$version_code"
  echo "release_tag=$release_tag"
  echo "release_notes_path=Project/docs/releases/v$version_name.md"
} >> "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

echo "Validated merged release $release_tag (versionCode $version_code)"
