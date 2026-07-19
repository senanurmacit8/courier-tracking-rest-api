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
- Given the scope and evaluation setup of this project, in-memory data
  structures were chosen over a persistent database. This lets the project
  be run and evaluated instantly without any extra infrastructure dependency
  (DB setup, migrations, connection configuration).
- The repository layer is abstracted behind the `CourierEventRepository`
  interface specifically to isolate this choice - moving to a persistent
  store (e.g. PostgreSQL via Spring Data JPA) later would only require a new
  repository implementation; the service layer would not need to change.