# Create Product Service

We will create a new Spring Boot microservice named `product-service` that manages `Product` and `Category` entities with a Many-to-Many relationship using MongoDB Document References. It will use a MongoDB Replica Set with resource limits in the local docker environment. Finally, we will provide a separate script to populate 1 million dummy records and a K6 load testing script to measure performance.

## Proposed Changes

### 1. Spring Boot Project Initialization
Generate a Spring Boot project via `start.spring.io` in a new `product-service` directory with the following dependencies:
- Spring Web (`web`)
- Spring Data MongoDB (`data-mongodb`)

### 2. Entity Models (Many-to-Many)
Create MongoDB Document entities for `Product` and `Category`:
#### [NEW] `Category.java`
Fields: `id`, `name`, `description`. Mapped as `@Document`.
#### [NEW] `Product.java`
Fields: `id`, `name`, `description`, `price`, `stock`. Mapped as `@Document`. Contains a `Set<Category>` mapped with `@DocumentReference(lazy = true)`.

### 3. Separate Data Generation Script
#### [NEW] `generate_data.sh` and `insert_1m_records.js`
Create a separate bash script that executes a Javascript mongosh script directly against the MongoDB container to insert 100 categories and 1,000,000 products, and their references. This prevents the generation from running automatically on app startup.

### 4. Docker Infrastructure (MongoDB Cluster)
#### [NEW] `docker-compose.yml`
Define the following infrastructure for local development:
- **MongoDB Replica Set**: A MongoDB cluster configured as a replica set (`rs0`), including Docker resource limits (`cpus` and `memory` restrictions).

### 5. Application Configuration
#### [NEW] `application.yml`
Configure database connection properties pointing to the MongoDB replica set using the `spring.data.mongodb.uri`.

### 6. API & Repository
#### [NEW] `ProductRepository.java`
Spring Data MongoRepository for Products.
#### [NEW] `CategoryRepository.java`
Spring Data MongoRepository for Categories.
#### [NEW] `ProductController.java`
REST controller providing a paginated endpoint `/api/products` to fetch products and their associated categories.

### 7. Performance Testing
#### [NEW] `k6-test.js`
Create a K6 script to test the `/api/products` endpoint under concurrent load (e.g., 50 Virtual Users for 1 minute).

## Verification Plan
1. Start the databases via `docker-compose up -d`.
2. Wait for the Mongo Replica Set to initialize.
3. Run the Spring Boot application.
4. Execute `./generate_data.sh` to insert the 1 million records.
5. Execute the K6 script `k6 run k6-test.js` to benchmark the API response time and throughput.
