#!/bin/bash
echo "Running MongoDB seed script..."
docker exec -i $(docker compose ps -q mongos) mongosh product_db --quiet < insert_1m_records.js
echo "Data generation complete!"
