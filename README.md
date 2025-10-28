# athletix.mx

Event ticketing system for athletic races

## 🚀 Tech Stack

- Java 21
- Spring Boot 3.4.4
- Spring Security & JWT
- Spring Data JPA
- PostgreSQL
- Maven

## 🛠 Pre-requisites

- [Java JDK 21](https://openjdk.org/projects/jdk/21/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Docker](https://docs.docker.com/get-docker/) y [Docker Compose](https://docs.docker.com/compose/install/)
- [Git](https://git-scm.com/downloads)


- [Manual de Arquitectura](docs/manual-arquitectura.md)


## 🔧 Environment Configuration

### Docker compose and PostgreSQL

1. Start up the database

```bash
docker compose up -d
```

This command will start PostgreSQL on port 5432 and PgAdmin on port 5050:

- PostgreSQL:
    - Host: localhost
    - Port: 5432
    - DB: carreras_db
    - User: postgres
    - Password: password

- PgAdmin:
    - URL: http://localhost:5050
    - Email: admin@carreras.com
    - Password: admin

To stop containers:

```bash
docker compose down
```

### Local configuration

1. Compile the project:

```bash
mvn clean install
```

2. Execute the app:

```bash
# Usando perfil de desarrollo
mvn spring-boot:run
```

## 💯 Main features

### For organizers

- Register and manage athletic races
- Display data of registered runners
- Event analytics

### For runners

- View upcoming events
- Register for careers
- View registration history
- Manage payments

