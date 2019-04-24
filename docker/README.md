[![Docker Build Status](https://img.shields.io/docker/build/thehyve/transmart-core.svg)](https://cloud.docker.com/swarm/thehyve/repository/registry-1.docker.io/thehyve/gb-backend)

Docker image definitions and docker-compose profiles to run gb-backend and its database.
`gb-backend` runs the application server through OpenJDK, and `gb-backend-database` its database through PostgreSQL.

## Usage
Run the full stack with:
```
docker-compose -f docker-compose.yml up
```

Those command will build or download the images, and run the containers.
