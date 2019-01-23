# gb-backend
[![Build Status](https://travis-ci.org/thehyve/gb-backend.svg?branch=master)](https://travis-ci.org/thehyve/gb-backend)

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

## Usage

Available API calls:

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

- POST `/queries/sets/scan`
   - Scans for changes in entries of the stored queries and updates stored sets. Only for administrators.
      Optional parameters:
      - maxNumberOfSets - maximal number of sets to be returned

- GET `/queries/<$queryId>/sets`
   - Gets a list of query result change entries by `queryId`. History of data changes for specific query.


All calls require an Authorization header.


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
