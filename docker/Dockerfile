FROM openjdk:8-jre-alpine

ENV APP_PORT 8083
ENV GB_BACKEND_VERSION 0.4.0-RC1
ENV GB_BACKEND_WAR_URL https://repo.thehyve.nl/content/repositories/releases/nl/thehyve/gb-backend/${GB_BACKEND_VERSION}/gb-backend-${GB_BACKEND_VERSION}.war

# non-overridable environment variables
ENV APP_USR gb_backend_usr
ENV APP_USR_HOME "/home/${APP_USR}"
ENV APP_CONFIG_FILE gb-backend.config.yml

COPY entrypoint.sh /entrypoint.sh

EXPOSE ${APP_PORT}

USER root

RUN adduser -h "${APP_USR_HOME}" -D ${APP_USR} && \
    sed -i 's/\r//' /entrypoint.sh && \
    chown "${APP_USR}" /entrypoint.sh && \
    chmod u+x /entrypoint.sh

# download the war
RUN wget "${GB_BACKEND_WAR_URL}" -O "${APP_USR_HOME}/gb-backend-${GB_BACKEND_VERSION}.war"

USER ${APP_USR}
WORKDIR ${APP_USR_HOME}

ENTRYPOINT ["/entrypoint.sh"]