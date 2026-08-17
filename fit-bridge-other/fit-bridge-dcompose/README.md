# FitBridge Docker Compose resources

Модуль публикует минимальный E2E-стенд как ZIP-артефакт с classifier
`resources`. E2E-модуль получает его через Gradle composite build и передаёт
распакованный Compose-файл Testcontainers.

Compose orchestration хранится в `compose/docker-compose.yml`. Конфигурации
PostgreSQL, Envoy и Keycloak берутся из канонического `deploy/`, поэтому они не
дублируются. Наблюдаемость, фиксированные host ports, Docker build contexts и
учебные заготовки Kafka/Spring/WireMock в E2E-артефакт не входят: тесты проверяют
публичное API уже собранных backend-образов.

Координата зависимости внутри composite build:

```text
com.github.martyanovav.otuskotlin.fitbridge:fit-bridge-dcompose:0.1.0
```

Сам ZIP публикуется с classifier `resources`; локальному потребителю classifier
указывать не нужно, поскольку Gradle выбирает `runtimeElements` included project.

Сборка и проверка из корня репозитория:

```bash
./gradlew buildInfra
```

`buildInfra` создаёт resource ZIP и проверяет наличие обязательных ресурсов.
Публикация в Maven-репозиторий для локального E2E не требуется: Gradle получает
артефакт напрямую из included build.
