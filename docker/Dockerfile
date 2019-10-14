FROM openjdk:8-jre-alpine

ARG GB_BACKEND_VERSION="1.0.3"

ENV GB_BACKEND_WAR_URL https://repo.thehyve.nl/content/repositories/releases/nl/thehyve/gb-backend/${GB_BACKEND_VERSION}/gb-backend-${GB_BACKEND_VERSION}.war

ENV APP_PORT 8083
ENV APP_USR gb_backend_usr
ENV APP_USR_HOME "/home/${APP_USR}"
ENV APP_CONFIG_FILE gb-backend.config.yml

COPY entrypoint.sh /entrypoint.sh

EXPOSE ${APP_PORT}

USER root

RUN adduser -h "${APP_USR_HOME}" -D ${APP_USR} && \
    sed -i 's/\r//' /entrypoint.sh && \
    chown "${APP_USR}" /entrypoint.sh && \
    chmod u+x /entrypoint.sh && \
    chown "${APP_USR}" /etc/ssl/certs/java/cacerts && \
    chown "${APP_USR}" /etc/ssl/certs/java && \
    chmod 644 /etc/ssl/certs/java/cacerts

# download the war
RUN wget "${GB_BACKEND_WAR_URL}" -O "${APP_USR_HOME}/gb-backend.war"

USER ${APP_USR}
WORKDIR ${APP_USR_HOME}

ENTRYPOINT ["/entrypoint.sh"]
