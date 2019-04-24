FROM openjdk:8-jre-alpine

ENV APP_PORT=8083
ENV GB_BACKEND_VERSION=0.4.0-RC1
ENV GB_BACKEND_WAR_URL=https://repo.thehyve.nl/content/repositories/releases/nl/thehyve/gb-backend/${GB_BACKEND_VERSION}/gb-backend-${GB_BACKEND_VERSION}.war

# non-overridable environment variables
ENV APP_CONFIG_DIR=gb-backend.config.yml

COPY entrypoint.sh /entrypoint.sh

# download the war
RUN wget "${GB_BACKEND_WAR_URL}" -O "gb-backend-${GB_BACKEND_VERSION}.war" && \
    sed -i 's/\r//' /entrypoint.sh && \
    chmod +x /entrypoint.sh

EXPOSE ${APP_PORT}

ENTRYPOINT ["/entrypoint.sh"]