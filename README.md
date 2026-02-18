# ThinkGearReader

## Overview

Legacy ingestion service that connects to the ThinkGear Adapter, parses NeuroSky packets, and publishes brainwave events to Kafka.

## Scope in BrainWaves

- In-scope as compatibility ingestion path
- Main produced topic: `think_gear_reader`
- Primary consumer: `ThinkGearReaderFX`

## Tech Stack

- Java 11
- Spring Boot 2.2.x
- Akka Streams
- Kafka
- Gradle

## Build

```bash
./gradlew clean build
```

## Run

```bash
./gradlew bootRun
```

## Key Configuration / Integration

- Config file: `src/main/resources/application.yml`
- Important keys:
  - `spring.kafka.bootstrap-servers`
  - `think-gear-connector.host`
  - `think-gear-connector.port`
  - `think-gear-connector.raw`
- Cross-repo dependency: `:commons` via `settings.gradle` (legacy path expectation: `../Commons`)

## Status / Notes

- Keep running during migration.
- New architecture should dual-publish to canonical `brainwaves.*` topics while preserving legacy stream behavior.

