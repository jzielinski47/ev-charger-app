# EV Charger Map - Backend

**Author:** Jakub Zieliński

EV Charger Map is being developed as a Bachelor of Engineering Thesis for the Applied Computer Science program at Cracow University of Technology.

This repository contains the backend service of the application. Currently, it provides an API to fetch EV charging stations, manage system users and their personal vehicles. The project is still under heavy development so many changes may occur between versions. Expect updates.

## Key Features:
- **Secure Access & Roles:** Implemented JWT authentication with Role-Based Access Control (RBAC) to ensure data privacy and secure admin-only endpoints.
- **Proximity Search:** Built spatial queries (via PostGIS and Hibernate-Spatial) leveraging database spatial indexing to help users instantly find nearby EV charging stations.
- **Personalized Garage Management:** Developed CRUD operations allowing users to easily add, manage, and set an active vehicle.
- **Hybrid API Architecture:** Utilized GraphQL for flexible, high-performance charger data retrieval, alongside REST APIs for authentication and vehicle management.
- **Rate Limiting:** In-memory per-client limits (Bucket4j + Caffeine) protect the server from request floods: anonymous clients get 5 requests per minute (keyed by IP), authenticated users 20 per minute (keyed by account), admins, `/api/auth/**` and the API docs are exempt. Exceeding the limit returns `429` with a `Retry-After` header. Limits are configurable via the `rate-limit.*` properties in `application.properties`.
- **Scheduled Replication (ChargerSync):** Automated a three-hour background sync of OpenChargeAPI data for major Polish cities. Routing client queries directly to this local database eliminates third-party network latency and completely bypasses external rate limits.

## Technology Stack

- **Core:** Java 21 LTS, Spring Boot 4.1
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL 16 (Alpine Based Docker image), also used for tests via Testcontainers
- **API:** REST (Spring WebMVC), GraphQL
- **Security:** Spring Security, JWT (jjwt) for authentication
- **Mapping & Boilerplate:** MapStruct, Lombok
- **Data Access:** Spring Data JPA
- **Infrastructure:** Docker & Docker Compose
- **Test Coverage:** JUnit 5, Mockito, Testcontainers, Github Actions CI pipeline

## Getting Started

To run the application locally, you need to configure the environment and start the required database services.

### 1. Environment Configuration
Create a `.env` file in the root directory of the project and populate it with the required values:

```dotenv
DB_NAME=evcm
DB_USER=your_postgres_user
DB_PASSWORD=your_postgres_user_password
DB_PORT=5432

OPEN_CHARGE_API_KEY=open_charge_api_generated_key

JWT_SECRET_KEY=jwt_secret_key
JWT_EXP_TIME=3600000
BACKEND_PORT=8080
```
### 2. Running the Application
Once the .env file is ready you can run the entire application by a single command thanks to docker compose. It will start two containers: server and the database.
The project uses a PostgreSQL database with PostGIS extension (delivered via docker image based on alpine). You can start the app with a single command:

```bash
docker-compose up -d
```

### 4. Running Tests
To execute the test suite run the following command:

```bash
./gradlew test
```

## Architecture
<img width="1806" height="980" alt="image" src="https://github.com/user-attachments/assets/e1b687a8-e257-4735-b0b9-b5ff83c29b2f" />

## API Documentation

The server exposes a REST API for authentication, vehicle management and admin tooling, and a GraphQL API for charger search. With the app running locally (default port `8080`):

| Resource | URL |
|---|---|
| Swagger UI (REST) | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI spec | `http://localhost:8080/v3/api-docs` (`/v3/api-docs.yaml` for YAML) |
| GraphQL endpoint | `POST http://localhost:8080/graphql/chargers` |
| GraphiQL playground | `http://localhost:8080/graphiql?path=/graphql/chargers` |

A ready-to-use Postman collection is available in [`postman/`](postman).

### Authentication

Protected endpoints require a JWT obtained from `POST /api/auth/login`, sent as:

```
Authorization: Bearer <token>
```

| Access level | Endpoints |
|---|---|
| Public | `/api/auth/**`, `/graphql/chargers`, Swagger / OpenAPI docs |
| Authenticated (`USER` or `ADMIN`) | `/api/vehicles/**` |
| Admin only (`ADMIN`) | `/api/admin/**` |

### Rate Limiting

All endpoints except `/api/auth/**` and the API docs are rate limited per client. Admins are never limited.

| Client | Key | Default limit |
|---|---|---|
| Anonymous | IP address | 5 requests / minute |
| Authenticated | account | 20 requests / minute |

Every limited response carries an `X-Rate-Limit-Remaining` header. Exceeding the limit returns:

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 42

{ "error": "Too Many Requests", "message": "Rate limit exceeded, retry in 42 seconds" }
```

### REST Endpoints

#### Auth — `/api/auth`

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/signup` | Public | Register a new user account |
| `POST` | `/api/auth/login` | Public | Log in and receive a JWT |

**`POST /api/auth/signup`**

```json
{
  "name": "Jan",
  "surname": "Kowalski",
  "email": "jan@example.com",
  "password": "secret123"
}
```

| Field | Rules |
|---|---|
| `name`, `surname` | required, max 255 chars |
| `email` | required, valid email, max 100 chars |
| `password` | required, 8–255 chars |

Response `200 OK`:

```json
{ "id": 1, "name": "Jan", "surname": "Kowalski", "email": "jan@example.com", "roles": ["USER"] }
```

Errors: `400` validation failed, `409` email already registered.

**`POST /api/auth/login`**

```json
{ "email": "jan@example.com", "password": "secret123" }
```

Response `200 OK` (`expiresIn` in milliseconds):

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "expiresIn": 3600000 }
```

Errors: `400` validation failed, `401` bad credentials.

#### Vehicles — `/api/vehicles`

All vehicle endpoints require authentication. Regular users can only see and modify their own vehicles; admins can access every vehicle.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/vehicles` | List vehicles |
| `GET` | `/api/vehicles/{vehicleId}` | Get a single vehicle |
| `POST` | `/api/vehicles` | Add a vehicle |
| `PUT` | `/api/vehicles/{vehicleId}` | Replace a vehicle's data |
| `DELETE` | `/api/vehicles/{vehicleId}` | Delete a vehicle |

Optional query parameter `targetEmail` on `GET /api/vehicles` and `POST /api/vehicles`:

- **Admin:** `GET` returns the vehicles of that user (all vehicles when omitted); `POST` creates the vehicle on that user's behalf.
- **User:** ignored on `POST`; on `GET` any email other than your own returns `403`.

**Request body** (`POST` and `PUT`):

```json
{
  "brand": "Tesla",
  "model": "Model 3",
  "yearOfProduction": 2023,
  "supportedInlets": ["CCS2", "TYPE_2"],
  "vehicleOriginatingMarket": "EUROPE",
  "batteryCapacityKwh": 75.0,
  "maxAcPowerKw": 11.0,
  "maxDcPowerKw": 250.0,
  "acPhases": 3
}
```

| Field | Type | Rules |
|---|---|---|
| `brand`, `model` | string | optional |
| `yearOfProduction` | integer (year) | optional |
| `supportedInlets` | array of `VehicleInlet` | **required**, non-empty |
| `vehicleOriginatingMarket` | `EUROPE` \| `NORTH_AMERICA` \| `CHINA` \| `JAPAN` \| `OTHER` | optional |
| `batteryCapacityKwh`, `maxAcPowerKw`, `maxDcPowerKw` | number | optional, > 0 |
| `acPhases` | integer | optional, 1–3 |

`PUT` replaces every field, so omitted fields are cleared.

**Response** (single object, or an array for `GET /api/vehicles`):

```json
{
  "id": 7,
  "brand": "Tesla",
  "model": "Model 3",
  "yearOfProduction": 2023,
  "supportedInlets": ["CCS2", "TYPE_2"],
  "vehicleOriginatingMarket": "EUROPE",
  "batteryCapacityKwh": 75.0,
  "maxAcPowerKw": 11.0,
  "maxDcPowerKw": 250.0,
  "acPhases": 3,
  "ownerId": 1
}
```

`DELETE` returns `200 OK` with an empty body.

Errors: `400` validation failed, `403` missing/expired token or `targetEmail` used by a non-admin, `404` vehicle does not exist or belongs to another user.

**Vehicle inlets** and the charger connectors they are compatible with (used by the GraphQL `vehicleId` filter):

| `VehicleInlet` | Compatible connector types |
|---|---|
| `TYPE_1` | `TYPE_1` |
| `TYPE_2` | `TYPE_2`, `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED` |
| `CCS2` | `CCS2`, `TYPE_2`, `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED` |
| `CHADEMO` | `CHADEMO` |
| `NACS` | `NACS`, `TESLA` |

#### Admin — `/api/admin/chargers`

Admin-only tools for working directly with [Open Charge Map](https://openchargemap.org). Regular charger data is replicated automatically by the scheduled ChargerSync job.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/chargers` | Preview raw OCM chargers within 10 km of `50.0, 20.0`; nothing is saved |
| `POST` | `/api/admin/chargers/save?latitude={lat}&longitude={lon}&distance={km}` | Fetch up to 100 OCM chargers around a point and save them to the local database |

Both return the raw OCM charger list. Errors: `403` when not logged in as an admin, `500` when OCM returns no data.

### GraphQL API

Endpoint: `POST /graphql/chargers`. Public, but the `vehicleId` filter requires a Bearer token.

#### Schema

```graphql
type Query {
    chargerPointById(id: ID!): ChargerPoint
    allChargerPointsInProximity(lat: Float!, lon: Float!, proximity: Float!, vehicleId: ID,
                                filter: ChargerFilter, sort: ChargerSort = DISTANCE,
                                limit: Int = 100, offset: Int = 0): [ChargerPoint!]!
    allChargerPointsInBounds(north: Float!, south: Float!, east: Float!, west: Float!, vehicleId: ID,
                             filter: ChargerFilter, sort: ChargerSort = DISTANCE,
                             limit: Int = 100, offset: Int = 0): [ChargerPoint!]!
}

input ChargerFilter {
    currentType: CurrentKind
    operationalOnly: Boolean
}

enum CurrentKind {
    AC
    DC
}

enum ChargerSort {
    DISTANCE
    POWER
    POINTS
}

type ChargerPoint {
    id: ID!
    addressInfo: AddressInfo!
    connections: [Connection!]!
    operator: Operator
    dataProvider: DataProvider
    isOperational: Boolean
    usageCost: String
    numberOfPoints: Int
    dateLastVerified: String
    dateLastConfirmed: String
    distanceKm: Float
}

type DataProvider {
    id: ID!
    license: String
}

type Operator {
    id: ID!
    name: String
    websiteUrl: String
    phone: String
    email: String
    faultReportEmail: String
}

type AddressInfo {
    id: ID!
    title: String
    addressLine1: String
    town: String
    latitude: Float
    longitude: Float
    accessComments: String
}

type Connection {
    id: ID!
    amps: Int
    voltage: Int
    powerKW: Float
    quantity: Int
    connectorType: String
    currentType: String
}
```

#### Queries

**`chargerPointById(id)`** — a single charger. Returns a `NOT_FOUND` error when the id does not exist.

**`allChargerPointsInProximity(lat, lon, proximity, ...)`** — chargers within `proximity` **kilometres** of the given WGS84 coordinates. `distanceKm` is measured from `lat`/`lon`.

**`allChargerPointsInBounds(north, south, east, west, ...)`** — chargers inside a map viewport. `distanceKm` and the `DISTANCE` sort use the centre of the box. Boxes crossing the antimeridian (`west > east`) are rejected; split them into two queries.

Both searches share these arguments:

- `vehicleId` — only chargers with at least one connector compatible with that vehicle's inlets (see the inlet table above). Requires authentication (`UNAUTHORIZED` otherwise) and must belong to the caller (`NOT_FOUND` otherwise).
- `filter.currentType` — `AC` (single- or three-phase) or `DC`; keeps chargers with at least one connection of that current. Combined with `vehicleId`, the same connection must match both.
- `filter.operationalOnly` — `true` keeps only chargers whose `isOperational` is `true` (unknown status is excluded).
- `sort` — `DISTANCE` (nearest first, default), `POWER` (strongest connection first; with `vehicleId` or `currentType` only matching connections count) or `POINTS` (most `numberOfPoints` first). Unknown values go last; ties are broken by distance.
- `limit` (1–500, default 100) and `offset` (default 0) page through the sorted results.

Invalid arguments (coordinates out of range, `proximity <= 0`, `south > north`, `west > east`, `limit` or `offset` out of range) return a `BAD_REQUEST` error. `distanceKm` is `null` in `chargerPointById`.

#### Field values

- `connectorType`: `TYPE_1`, `TYPE_2`, `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED`, `CCS2`, `CHADEMO`, `TESLA`, `NACS`, `UNKNOWN`
- `currentType`: `AC_1P`, `AC_3P`, `DC`, `UNKNOWN`
- `dateLastVerified`, `dateLastConfirmed`: ISO-8601 UTC timestamps, e.g. `2025-03-14T09:26:53Z`

#### Example

```graphql
query {
  allChargerPointsInProximity(lat: 50.0647, lon: 19.9450, proximity: 5, vehicleId: 7,
                              filter: { currentType: DC, operationalOnly: true },
                              sort: POWER, limit: 50, offset: 0) {
    id
    distanceKm
    isOperational
    addressInfo { title addressLine1 town latitude longitude }
    operator { name websiteUrl }
    connections { connectorType currentType powerKW quantity }
  }
}
```

```bash
curl -X POST http://localhost:8080/graphql/chargers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query":"{ allChargerPointsInProximity(lat: 50.0647, lon: 19.9450, proximity: 5, vehicleId: 7) { id addressInfo { title town } connections { connectorType powerKW } } }"}'
```

## Copyright & Licensing
Copyright (c) 2026 Jakub Zieliński. All Rights Reserved.

This repository and its source code are published for portfolio demonstration and professional evaluation.

You are granted permission to download, review, and run the code locally strictly for personal, non-commercial purposes.

However, this remains a proprietary application. You may not use, distribute, deploy, or modify this code (in whole or in part) for any commercial purpose or financial gain without my explicit written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE..
