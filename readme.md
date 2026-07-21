# EV Charger App - Backend

**Author:** Jakub Zieliński

EV Charger Map is being developed as a Bachelor of Engineering Thesis for the Applied Computer Science program at Cracow University of Technology.

This repository contains the backend service of the application. Currently, it provides an API to fetch EV charging stations, manage system users and their personal vehicles.

## Key Features:
- **Secure Access & Roles:** Implemented JWT authentication with Role-Based Access Control (RBAC) to ensure data privacy and secure admin-only endpoints.
- **Proximity Search:** Built spatial queries (via PostGIS and Hibernate-Spatial) leveraging database spatial indexing to help users instantly find nearby EV charging stations.
- **Personalized Garage Management:** Developed CRUD operations allowing users to easily add, manage, and set an active vehicle.
- **Hybrid API Architecture:** Utilized GraphQL for flexible, high-performance charger data retrieval, alongside REST APIs for authentication and vehicle management.
- **Optimized Data Caching:** Integrated the OpenChargeAPI to automatically fetch and store charger data locally, ensuring fast and reliable query responses without hitting external API limits.
  

## Technology Stack

- **Core:** Java 21 LTS, Spring Boot
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL 16 (via Docker Alpine Based), H2 (for testing)
- **API:** REST (Spring WebMVC), GraphQL
- **Security:** Spring Security, JWT (jjwt) for authentication
- **Mapping & Boilerplate:** MapStruct, Lombok
- **Data Access:** Spring Data JPA
- **Infrastructure:** Docker & Docker Compose
- **Test Coverage:** JUnit 5, Mockito

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
```
### 2. Database Setup
The project uses a PostgreSQL database. You can start the database container using Docker Compose:

```bash
docker-compose up -d
```
(Note: The project includes the spring-boot-docker-compose dependency, which allows Spring Boot to automatically manage the Docker Compose lifecycle during startup).

### 3. Running the Application
Once the .env file is ready and the database is configured, start the application using the Gradle wrapper:

```bash
set -a && source .env && set +a && ./gradlew bootRun
```

### 4. Running Tests
To execute the test suite (which uses an in-memory H2 database):

```bash
./gradlew test
```

## API Documentation
