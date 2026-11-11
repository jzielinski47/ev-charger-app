# Vehicle connector filter

Branch: `feature/vehicle-connector-filter` (based on the branch with the GiST index migration).
Follow CLAUDE.md (atomic lowercase commits, no AI mentions, plan mode first).

## Scope

1. Separate vehicle inlets from station connectors.
2. Filter chargers in proximity by the connectors compatible with a selected vehicle (`vehicleId`).
3. Return chargers sorted by distance, nearest first (filtered and unfiltered).
4. Store `Vehicle.vehicleOriginatingMarket` as a string.

Out of scope: power filtering, power sorting (frontend does it), persisting the active vehicle, changes to ChargerSync.

## 1. `VehicleInlet` enum

New enum in `jz.pk.evcm.entity`. Each constant takes its compatible station connectors through the constructor as `Set.of(...)`. No switch, no map, no service.

| VehicleInlet | Compatible ConnectorType |
|---|---|
| `TYPE_1` | `TYPE_1` |
| `TYPE_2` | `TYPE_2`, `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED` |
| `CCS2` | `CCS2`, `TYPE_2`, `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED` |
| `CHADEMO` | `CHADEMO` |
| `NACS` | `NACS`, `TESLA` |

Rules:
- Only native compatibility, no adapters.
- A CCS2 inlet accepts a Type 2 plug for AC charging, so it includes all Type 2 variants.
- A vehicle with several inlets (e.g. CHAdeMO + Type 2) lists each of them; the compatible set is the union.
- `ConnectorType.UNKNOWN` is never compatible.
- Expose `Set<ConnectorType> compatibleConnectors()` and a static helper that returns the union for a collection of inlets.

## 2. `Vehicle` entity changes

- `supportedConnectorTypes: List<ConnectorType>` → `supportedInlets: Set<VehicleInlet>` (`@ElementCollection`, `@Enumerated(EnumType.STRING)`).
- `vehicleOriginatingMarket`: add `@Enumerated(EnumType.STRING)`. Diagnostic field only, no relation to charger search.
- Request DTO: `supportedInlets` must not be empty (`@NotEmpty`).
- Update vehicle DTOs, mapper and existing vehicle REST tests accordingly.

## 3. Flyway migrations

One migration per concern:
- `vehicle_originating_market`: convert the ordinal integer column to `varchar` using the current enum order (`USING CASE ... END`).
- Vehicle inlets: rename/replace `vehicle_connector_types` with an inlet collection table. Map existing values: `TYPE_2_SOCKET_ONLY`, `TYPE_2_TETHERED` → `TYPE_2`; `TESLA` → `NACS`; drop `UNKNOWN`; keep the rest by name.

Show the migration SQL in the plan before writing it.

## 4. Repository

- Keep the existing unfiltered proximity query.
- Add a second query with the same spatial conditions, ordering and limit, plus:
  `AND EXISTS (SELECT 1 FROM Connection c WHERE c.chargerPoint = cp AND c.connectorType IN :types)`
- Do not filter the fetched `connections` collection; every returned charger keeps all its connections.
- Do not use `(:types IS NULL OR ...)`; choose the method in the service.
- Both queries: `ORDER BY` distance ascending (verify it exists; add it if missing).

## 5. Service

- `getAllChargersInProximity(lat, lon, radiusKm, maxResults, vehicleId, ownerEmail)` (or an equivalent overload).
- `vehicleId == null` → unfiltered query.
- `vehicleId != null` and no authenticated user → reject (unauthorized).
- `vehicleId != null` → `VehicleRepository.findByIdAndOwnerEmail(vehicleId, email)`; not found or not owned → the same not-found error (do not reveal whether the id exists).
- Compatible connector set = union of `compatibleConnectors()` over the vehicle's inlets → filtered query.

## 6. GraphQL

- Add optional `vehicleId: ID` to `allChargerPointsInProximity`. Existing clients keep working.
- Take the email from the security principal (`@AuthenticationPrincipal`), never from the client.
- `/graphql` stays public; the JWT filter must still populate the security context when a token is present.
- Map the unauthorized / not-found cases to GraphQL errors with a clear classification.

## 7. Tests

Unit (`VehicleInlet`):
- every constant has a non-empty compatible set
- CCS2 includes all Type 2 variants
- union for multiple inlets
- `UNKNOWN` is in no set

Integration (Testcontainers, Kraków coordinates as in existing proximity tests):
- without `vehicleId` results are unchanged
- compatible charger returned, incompatible excluded
- CCS2 vehicle sees Type 2 AC chargers
- charger with mixed connectors is returned once with all its connections
- results are ordered by distance in the filtered query
- limit counts chargers, not connections, in the filtered query
- anonymous user with `vehicleId` is rejected
- another user's `vehicleId` is rejected the same way as a missing one

Migration:
- ordinal market values convert to the correct names
- legacy connector values convert to the correct inlets

## Suggested commit order

1. `store vehicle originating market as string`
2. `add vehicle inlet enum with compatible connector types`
3. `add vehicle inlet unit tests`
4. `replace vehicle connector types with supported inlets`
5. `add compatible connector filter to proximity query`
6. `filter chargers in proximity by owned vehicle`
7. `add vehicle id argument to proximity graphql query`
8. `add proximity tests for vehicle connector filter`
