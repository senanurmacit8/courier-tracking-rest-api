# Courier Tracking REST API

This is a Spring Boot REST application for streaming courier geolocations.

## Features

- Accepts courier location events as streaming input: time, courierId, lat, lng.
- Logs courier-store entrances when a courier enters within 100 meters of a Migros store.
- Ignores re-entries to the same store within 60 seconds.
- Calculates total travel distance per courier.
- Uses two design patterns:
  - Strategy for distance calculation.
  - Observer for reacting to incoming courier locations.
- Persists courier events, travel distance and store visit logs in an H2
  database via Spring Data JPA, so data survives application restarts.

## Run

Prerequisite: Java 21+ and Maven.

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## API

### Post a location event

`POST /api/locations`

Example body:

```json
{
  "time": "2026-07-18T10:00:00Z",
  "courierId": "courier-1",
  "lat": 41.0082,
  "lng": 28.9784
}
```

### Get courier distance

`GET /api/couriers/{courierId}/distance`

Example response:

```json
{
  "courierId": "courier-1",
  "totalDistanceMeters": 1532.42
}
```

### Get store visit logs

`GET /api/store-visits`

### Get store catalog

`GET /api/stores`

## Notes

- Store locations are loaded from `src/main/resources/stores.json`.
- Courier location events, computed travel distances and store visit logs
  are persisted through Spring Data JPA into a file-based H2 database
  (`./data/courier-tracking`), so state is no longer lost on restart.
- H2 was chosen instead of a full PostgreSQL/MySQL setup because it requires
  no external installation or container - the project still runs with a
  single `mvn spring-boot:run` while behaving like a real relational
  database (JDBC driver, SQL dialect, JPA entities, transactions).
- The repository layer is abstracted behind the `CourierEventRepository`
  interface, so the underlying store (`JpaCourierEventRepository`) could be
  swapped for another database (e.g. PostgreSQL) by only changing the
  datasource configuration and dependency - the service layer does not
  depend on JPA directly.