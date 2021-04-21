[![Docker Build Status](https://img.shields.io/docker/pulls/thehyve/glowing-bear-backend.svg)](https://hub.docker.com/r/thehyve/glowing-bear-backend)

Docker image definitions and docker-compose profiles to run gb-backend and its database.
`gb-backend` runs the application server through OpenJDK, and `gb-backend-database` its database through PostgreSQL.

## Usage

Run the full stack with:
```bash
export TRANSMART_API_SERVER_URL=https://transmart.example.com
export KEYCLOAK_SERVER_URL=https://keycloak.example.com/auth
export KEYCLOAK_REALM=transmart-dev
export KEYCLOAK_CLIENT_ID=transmart-client
docker-compose -f docker-compose.yml up
```

Those commands will download the images and start the containers.

### Configuration

Name                       | Description
:------------------------- |:------------------------------------------------------
`TRANSMART_API_SERVER_URL` | URL of the TranSMART API server, used to receive data for notifications.
`KEYCLOAK_SERVER_URL`      | URL of the Keycloak server, used to verify user tokens.
`KEYCLOAK_REALM`           | The Keycloak realm to use.
`KEYCLOAK_CLIENT_ID`       | The Keycloak client.

If a certificate file is mounted as volume at `/home/gb_backend_usr/extra_certs.pem`,
the file is imported to the default certificate store of Java at startup. 


## Development

### Build

```bash
docker build -t glowing-bear-backend . --no-cache
```

### Publish

Publish the image to [Docker Hub](https://hub.docker.com/r/thehyve/glowing-bear-backend):

```bash
docker login
GB_BACKEND_VERSION="1.0.6"
docker tag glowing-bear-backend "thehyve/glowing-bear-backend:${GB_BACKEND_VERSION}"
docker push "thehyve/glowing-bear-backend:${GB_BACKEND_VERSION}"
```
