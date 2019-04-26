[![Docker Build Status](https://img.shields.io/docker/pulls/thehyve/glowing-bear-backend.svg)](https://hub.docker.com/r/thehyve/glowing-bear-backend)

Docker image definitions and docker-compose profiles to run gb-backend and its database.
`gb-backend` runs the application server through OpenJDK, and `gb-backend-database` its database through PostgreSQL.

## Build
Build the image with:
```bash
docker build -t thehyve/glowing-bear-backend . --no-cache
```

## Usage
Run the full stack with:
```bash
export TRANSMART_API_SERVER_URL=https://transmart.example.com
export KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth
export KEYCLOAK_REALM=transmart-dev
docker-compose -f docker-compose.yml up
```

Those command will build or download the images, and run the containers.
