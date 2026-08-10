#!/usr/bin/env bash

set -Eeuo pipefail

script_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repository_root="$(cd "${script_directory}/.." && pwd)"
training_service_root="${repository_root}/fit-bridge-be/training-service"
deploy_root="${repository_root}/deploy"
compose_file="${deploy_root}/docker-compose.yml"
gradle_wrapper="${repository_root}/gradlew"

if [[ -x "${gradle_wrapper}" ]]; then
    gradle_command=("${gradle_wrapper}")
else
    gradle_command=(bash "${gradle_wrapper}")
fi

run_step() {
    local description="$1"
    shift

    printf '\n==> %s\n' "${description}"
    "$@"
}

find_fat_jar() {
    local service_name="$1"
    local libraries_directory="$2"
    local candidates=()
    local newest_artifact
    local candidate
    local artifact_size

    shopt -s nullglob
    candidates=("${libraries_directory}"/*-all.jar)
    shopt -u nullglob

    if (( ${#candidates[@]} == 0 )); then
        printf 'The shadowJar artifact for %s was not found in %s.\n' \
            "${service_name}" "${libraries_directory}" >&2
        return 1
    fi

    newest_artifact="${candidates[0]}"
    for candidate in "${candidates[@]:1}"; do
        if [[ "${candidate}" -nt "${newest_artifact}" ]]; then
            newest_artifact="${candidate}"
        fi
    done

    artifact_size="$(stat --format='%s' "${newest_artifact}")"
    if (( artifact_size < 1048576 )); then
        printf 'The artifact selected for %s is too small to be a fat JAR: %s.\n' \
            "${service_name}" "${newest_artifact}" >&2
        return 1
    fi

    if (( ${#candidates[@]} > 1 )); then
        printf 'WARNING: Several fat JARs exist for %s; the newest one is selected: %s.\n' \
            "${service_name}" "$(basename "${newest_artifact}")" >&2
    fi

    printf '%s\n' "${newest_artifact}"
}

copy_service_artifact() {
    local artifact="$1"
    local destination="$2"
    local artifact_size
    local artifact_hash="unavailable"

    cp --force "${artifact}" "${destination}"
    artifact_size="$(stat --format='%s' "${destination}")"
    if command -v sha256sum >/dev/null 2>&1; then
        artifact_hash="$(sha256sum "${destination}" | awk '{print $1}')"
    elif command -v shasum >/dev/null 2>&1; then
        artifact_hash="$(shasum --algorithm 256 "${destination}" | awk '{print $1}')"
    fi

    printf 'Staged %s: %s bytes, SHA256=%s\n' \
        "$(basename "${destination}")" "${artifact_size}" "${artifact_hash}"
}

show_failure_context() {
    local exit_code=$?
    trap - ERR

    printf '\nE2E workflow failed with exit code %s.\n' "${exit_code}" >&2
    printf 'Current Docker Compose status:\n' >&2
    docker compose --file "${compose_file}" ps || true
    printf "The local stack was not removed. Inspect it before running 'docker compose down'.\n" >&2
    exit "${exit_code}"
}

trap show_failure_context ERR

if [[ ! -f "${gradle_wrapper}" ]]; then
    printf 'Gradle wrapper was not found: %s\n' "${gradle_wrapper}" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    printf 'Docker CLI was not found in PATH.\n' >&2
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    printf 'curl was not found in PATH.\n' >&2
    exit 1
fi

run_step \
    "Validate Docker Compose configuration" \
    docker compose --file "${compose_file}" config --quiet

run_step \
    "Build training-service fat JAR" \
    "${gradle_command[@]}" -p "${training_service_root}" :app-ktor:shadowJar --console=plain

training_artifact="$(find_fat_jar \
    "training-service" \
    "${training_service_root}/app-ktor/build/libs")"

copy_service_artifact "${training_artifact}" "${deploy_root}/training-service.jar"

run_step \
    "Start storage services" \
    docker compose --file "${compose_file}" up --detach --wait --wait-timeout 180 \
        postgresql greptimedb

run_step \
    "Start the logging service" \
    docker compose --file "${compose_file}" up --detach --wait --wait-timeout 180 \
        fluent-bit

run_step \
    "Recreate Keycloak with the current mounted realm configuration" \
    docker compose --file "${compose_file}" up --detach --force-recreate --no-deps \
        --wait --wait-timeout 180 keycloak

run_step \
    "Recreate Envoy with the current mounted routing configuration" \
    docker compose --file "${compose_file}" up --detach --force-recreate --no-deps \
        --wait --wait-timeout 180 envoy

run_step \
    "Build and start application services" \
    docker compose --file "${compose_file}" up --detach --build --no-deps \
        --wait --wait-timeout 180 training-service

run_step \
    "Show the local E2E stack status" \
    docker compose --file "${compose_file}" ps

printf '\n==> Verify public health endpoints\n'
health_urls=(
    "http://localhost:8080/health"
    "http://localhost:8080/health/training/ready"
)
for health_url in "${health_urls[@]}"; do
    status_code="$(curl \
        --fail \
        --silent \
        --show-error \
        --max-time 15 \
        --output /dev/null \
        --write-out '%{http_code}' \
        "${health_url}")"
    printf '%s -> HTTP %s\n' "${health_url}" "${status_code}"
done

run_step \
    "Run FitBridge E2E tests" \
    "${gradle_command[@]}" -p "${repository_root}" e2eTests --rerun-tasks --console=plain

printf '\nE2E verification completed successfully. The local stack remains running.\n'
