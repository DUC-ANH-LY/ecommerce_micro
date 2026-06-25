#!/bin/bash

echo "Initializing Config Server Replica Set..."
docker exec -i $(docker compose ps -q configsvr) mongosh --port 27019 --quiet --eval 'rs.initiate({_id: "configRS", configsvr: true, members: [{_id: 0, host: "configsvr:27019"}]})'

echo "Initializing Shard 1 Replica Set..."
docker exec -i $(docker compose ps -q shard1) mongosh --port 27018 --quiet --eval 'rs.initiate({_id: "shard1RS", members: [{_id: 0, host: "shard1:27018"}]})'

echo "Initializing Shard 2 Replica Set..."
docker exec -i $(docker compose ps -q shard2) mongosh --port 27018 --quiet --eval 'rs.initiate({_id: "shard2RS", members: [{_id: 0, host: "shard2:27018"}]})'

echo "Waiting for replica sets to initialize..."
sleep 15

echo "Adding shards to the cluster..."
docker exec -i $(docker compose ps -q mongos) mongosh --port 27017 --quiet --eval 'sh.addShard("shard1RS/shard1:27018")'
docker exec -i $(docker compose ps -q mongos) mongosh --port 27017 --quiet --eval 'sh.addShard("shard2RS/shard2:27018")'

echo "Enabling sharding for product_db..."
docker exec -i $(docker compose ps -q mongos) mongosh --port 27017 --quiet --eval 'sh.enableSharding("product_db")'

echo "Sharding the products collection by _id (hashed)..."
docker exec -i $(docker compose ps -q mongos) mongosh --port 27017 --quiet --eval '
  db.getSiblingDB("product_db").products.createIndex({ _id: "hashed" }); 
  sh.shardCollection("product_db.products", { _id: "hashed" })
'

echo "Sharding setup complete."
