# Product Service

This is a Spring Boot microservice for managing products and categories, configured with a MongoDB replica set for local development.

## Verification & Testing Guide

Follow these steps to fully start up the infrastructure, run the service, seed the database with 1 million records, and run the performance load test.

### 1. Start the Database Infrastructure

First, spin up the Docker container which includes the MongoDB replica set.

```bash
docker-compose up -d
```

Wait a few moments to ensure the MongoDB replica set is fully initialized and healthy. You can check the logs with:
```bash
docker-compose logs -f
```

### 2. Start the Spring Boot Application

Start the Spring Boot application. It will connect to the MongoDB replica set.

```bash
./gradlew bootRun
```

### 3. Generate 1 Million Dummy Records

Run the provided script to seed the database with 1,000,000 product records and 100 categories. This script connects directly to the MongoDB container and uses `insertMany` in batches for fast insertion.

Open a new terminal window and run:

```bash
chmod +x generate_data.sh
./generate_data.sh
```

### 4. Accessing Swagger UI
Assuming your application is running on the default port 8080 (you currently have ./gradlew bootRun running in the background), you can view your API documentation right now by opening this URL in your browser:

Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

