[< Back](../README.md)
---

## Building

To use the same version of Java locally as is used in CI and production, follow [these notes](sdkman.md).


Firstly build the project (without tests) by running:
```
./gradlew clean build -x test
```

To rebuild the docker image locally after building the project (perhaps after some new changes), run:
```
docker build -t quay.io/hmpps/hmpps-health-and-medication-api:latest .
```

## Testing
```
./gradlew test 
```

## Running Locally

The API currently depends on HMPPS Auth, Prisoner Search and Prison API.
When using the DEV profile the API points to the dependant services deployed in the
development environment.

The API also requires a PostgreSQL database which can be started via docker compose with:

```shell
docker compose up health-and-medication-data-db
```

The service can then be run in the following ways:

### Running in the command line with gradle
```
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Running in IntelliJ
Run the main class with the following VM options:
```
-Dspring.profiles.active=dev
```

### Running with docker-compose
```
docker-compose up
```

## Common gradle tasks

To list project dependencies, run:

```
./gradlew dependencies
```

To check for dependency updates, run:
```
./gradlew dependencyUpdates --warning-mode all
```

To run an OWASP dependency check, run:
```
./gradlew clean dependencyCheckAnalyze --info
```

To upgrade the gradle wrapper version, run:
```
./gradlew wrapper --gradle-version=<VERSION>
```

To automatically update project dependencies, run:
```
./gradlew useLatestVersions
```

#### Ktlint Gradle Tasks

To run Ktlint check:
```
./gradlew ktlintCheck
```

To run Ktlint format:
```
./gradlew ktlintFormat
```

To register pre-commit check to run Ktlint format:
```
./gradlew addKtlintFormatGitPreCommitHook 
```

...or to register pre-commit check to only run Ktlint check:
```
./gradlew addKtlintCheckGitPreCommitHook
```