#!/usr/bin/env bash

here=$(dirname "${0}")
pushd "${here}/../gb-backend"
GB_BACKEND_VERSION=$(gradle properties | grep '^version: ' - | awk '{print $2}')
popd

docker build --build-arg "GB_BACKEND_VERSION=${GB_BACKEND_VERSION}" -t "thehyve/glowing-bear-backend:${GB_BACKEND_VERSION}" "${here}/../docker" && \
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD" && \
docker push "thehyve/glowing-bear-backend:${GB_BACKEND_VERSION}"
