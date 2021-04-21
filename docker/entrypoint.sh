#!/bin/sh
set -e

cat > "${APP_CONFIG_FILE}" <<EndOfMessage
---
transmart:
  server-url: ${TRANSMART_API_SERVER_URL}
  api-version: v2

keycloak:
  auth-server-url: ${KEYCLOAK_SERVER_URL}
  realm: ${KEYCLOAK_REALM}
  resource: ${KEYCLOAK_CLIENT_ID}

dataSource:
  driverClassName: org.postgresql.Driver
  dialect: org.hibernate.dialect.PostgreSQLDialect
  username: ${DB_USER}
  password: ${DB_PASSWORD}
  url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}

# query subscription functionality is currently not supported on docker
nl.thehyve.gb.backend.notifications.enabled: false
quartz.autoStartup: false
keycloakOffline.offlineToken: <offline_token>
EndOfMessage

CERTS_PATH="${APP_USR_HOME}/extra_certs.pem"

[[ -f "${CERTS_PATH}" ]] && \
  keytool -import -trustcacerts -file "${CERTS_PATH}" -alias certificate-alias -keystore "${APP_USR_HOME}/cacerts" -storepass password -noprompt && \
  mv "${APP_USR_HOME}/cacerts" /etc/ssl/certs/java/cacerts

exec java -jar -server \
        "-Djava.awt.headless=true" \
        "-Dmail.mime.decodeparameters=true" \
        "-Djava.security.egd=file:///dev/urandom" \
        "-Dserver.port=${APP_PORT}" \
        "-Dspring.config.location=${APP_CONFIG_FILE}" \
    "gb-backend.war"
