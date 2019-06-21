# gb-backend
[![Build Status](https://travis-ci.org/thehyve/gb-backend.svg?branch=master)](https://travis-ci.org/thehyve/gb-backend)
[![Known vulnerabilities](https://snyk.io//test/github/thehyve/gb-backend/badge.svg?targetFile=gb-backend/build.gradle)](https://snyk.io//test/github/thehyve/gb-backend?targetFile=gb-backend/build.gradle)

Backend application for Glowing Bear UI, supporting user queries management, subscription and notification functionality.

## Development

### Configure PostgreSQL database
```bash
sudo -u postgres psql
```

```bash
create user gb with password 'gb';
create database gb_backend;
grant all privileges on database gb_backend to gb;
```

### Run

```bash
# Change to the application directory
cd gb-backend

# Fetch the Gradle wrapper
gradle wrapper

# Start the application, http://localhost:8083
./gradlew bootRun
```

## Keycloak settings

### Identity server

Keycloak settings have to be set for the application to work.
They have to match with settings frontend glowing bear application uses.

| yaml key | description |
|-----------|-------------|
| `keycloak.auth-server-url` | keycloak url that used. e.g. `https://keycloak.example.com/auth` |
| `keycloak.realm` | keycloak realm. |
| `keycloak.resource` | keycloak client id. |
| `keycloakOffline.offlineToken` | keycloak offline token. |

### Offline token

The application requires an offline token to be able to run batch jobs.

Below is `curl` command to generate an offline token for `USERNAME` user.
To get the token the user needs to have the role mapping for the realm-level: `"offline_access"`.
Before using the command you have to substitute words in uppercase with proper ones.

**NOTE:** The offline user (`USERNAME` in example below) has to have following `realm-management` roles:

- `impersonation` - to support running queries on behalf of queries owners.
- `view-users` - to fetch list of users with the keycloak API. Used by the queries processing and sending emails.

```bash
    curl \
      -d 'client_id=CLIENT_ID' \
      -d 'username=USERNAME' \
      -d 'password=PASSWORD' \
      -d 'grant_type=password' \
      -d 'scope=offline_access' \
      'https://YOUR_KEYCLOAK_SERVER_HOST/auth/realms/YOUR_REALM/protocol/openid-connect/token'
```

The value of the `refresh_token` field in the response is the offline token.

## API calls:

- GET `/queries`
   - Gets a list of all queries for current user.
- GET `/queries/<id>`
   - Gets the user query with the given `<id>`.
- POST `/queries`
   - Adds a new user query with the properties provided in the body.
   Request body:
   ```
   {
             "name": "string",
             "queryConstraint": {},
             "bookmarked": true,
             "subscribed": true,
             "subscriptionFreq": "string",
             "queryBlob": {}
   }
   ```
- PUT `/queries/<id>`
   - Updates the user query with given `<id>` with the values in the body.
      Request body:
      ```
      {
            "name": "string",
            "bookmarked": true,
            "subscribed": true,
            "subscriptionFreq": "string",
      }
      ```
- DELETE `/queries/<id>`
   - Deletes the user query with the given `<id>`.

- GET `/queries/<$queryId>/sets`
   - Gets a list of query result change entries by `queryId`. History of data changes for specific query.

### Only for administrators.

- POST `/queries/sets/scan`
   - Scans for changes in entries of the stored queries and updates stored sets.
      Optional parameters:
      - `maxNumberOfSets` - maximal number of sets to be returned

- GET `/notifications/notify`
   - Triggers sending of emails to users that subscribed for updates regarding queries they have created.
     This endpoint can be disabled in the configuration (see the configuration description below).

     Required parameters:
     - `frequency` - `DAILY`|`WEEKLY `- determines whether the email should be sent to users with a DAILY or WEEKLY subscription.

All calls require an Authorization header.

## Email notifications

Gb-backend provides a notifications functionality that sends automated emails to subscribers with a daily or weekly digest.

Currently it supports a query subscription and notifies users when data linked to a query they subscribed to changes.


### Configuration of the cron job scheduler:

```
# To disable the email sending job, change this to false
quartz:
    autoStartup: true
```

### Configuration an the email server:

```
grails:
    ...
    mail:
        host: localhost:25
        'default':
            from: <default_email>
        port: 465
        username: <username>
        password: <password>
        props:
           "mail.smtp.auth": true
           "mail.smtp.ssl.enable": true
           "mail.smtp.socketFactory.port": 465
           "mail.smtp.socketFactory.class": javax.net.ssl.SSLSocketFactory
           "mail.smtp.socketFactory.fallback": false
```

For more information check [Grails Email Plugin documentation.](http://gpc.github.io/grails-mail/guide/2.%20Configuration.html)

### Other settings:

```
nl.thehyve.gb.backend.notifications:
    # enable daily and weekly notification jobs and endpoint to trigger notifications email sending
    # (this endpoint is intended for test purposes)
    enabled: true
    # max number of query sets returned in a subscription email
    maxNumberOfSets: 20
    # daily cron job trigger time in format: hh-mm
    # hh: Hour, range: 0-23;
    # mm: Minute, range: 0-59;
    dailyJobTriggerTime: 0-0
    # Name of the client application on behalf of which gb-backend will send notification email.
    clientApplicationName: Glowing Bear
```

## License
Copyright &copy; 2019 The Hyve B.V.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the [GNU Affero General Public License](LICENSE)
along with this program. If not, see https://www.gnu.org/licenses/.
