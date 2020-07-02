# gb-backend-e2e
## How to run
These tests are designed to run against a live environment. By default it is pointed at `http://localhost:8083`. The test suite
uses [spock](http://spockframework.org/) and [http-builder-ng](https://github.com/http-builder-ng/http-builder-ng).

To start the server, run the following in the `gb-backend` directory:
```bash
# Start the server
pushd ..
export TRANSMART_API_SERVER_URL=https://transmart-dev.thehyve.net
export KEYCLOAK_REALM=transmart-dev
export KEYCLOAK_SERVER_URL=https://keycloak-dwh-test.thehyve.net
export KEYCLOAK_CLIENT_ID=transmart-client
export GB_BACKEND_NOTICATIONS_ENABLED=true
./gradlew -Dgrails.env=test bootRun
popd
```

While the server is running, the tests can be run from the `gb-backend-e2e` directory:

```bash
# Running all tests:
gradle test

# Run one test class: 
gradle test --tests '*namespec'

# Change base url:
gradle -DbaseUrl=https://some-gb-backend-server.com/ test
```

To enable testing triggering of notification emails, configure an offline token for
Keycloak (`export KEYCLOAK_OFFLINE_TOKEN=<offline token>`) and run:
```
EMAIL_ENABLED=true gradle test
```
Note that as a side effect, emails are sent!
