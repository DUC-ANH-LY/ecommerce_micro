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

### 4. Run K6 Performance Tests

After the data has been inserted, you can test the application's performance using the included K6 load testing script. This script tests the paginated `/api/products` endpoint under concurrent load.

Ensure you have [k6 installed](https://k6.io/docs/get-started/installation/), then run:

```bash
k6 run k6-test.js
```

This will run 50 virtual users concurrently for 1 minute and output the latency, throughput, and error metrics in your terminal.


Bug
I've updated your Spring Boot service to connect to the new MongoDB cluster and resolved the Gradle build issues you were seeing!

Here's exactly what I updated:

Gradle Build Fix (build.gradle): The error you encountered running ./gradlew bootRun was caused by using an older version of the Spring Boot dependency-management plugin with Gradle 9+. I've updated the Spring Boot plugin to 3.3.0 and io.spring.dependency-management to 1.1.4, which are fully compatible with your Gradle 10 setup.

MongoDB Connection (application.yml): I updated the spring.data.mongodb.uri so that it now connects to the cluster: mongodb://localhost:27017,localhost:27018,localhost:27019/product_db?replicaSet=rs0

⚠️ One Important Step Left
I ran ./gradlew bootRun and the Spring application now successfully starts up!

However, there is one networking quirk with MongoDB Replica Sets: When your Spring Boot app connects to the cluster, the cluster tells Spring Boot the hostnames of its nodes (mongo1, mongo2, mongo3 inside Docker). Spring Boot then tries to resolve these names locally and will throw an UnknownHostException in the background because your computer doesn't know what mongo1 is.

To completely fix this, please run the following command in your terminal to map those hostnames to your localhost (you will be prompted for your sudo password):

bash
`echo "127.0.0.1 mongo1 mongo2 mongo3" | sudo tee -a /etc/hosts`