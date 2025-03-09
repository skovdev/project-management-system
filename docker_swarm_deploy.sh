#!/bin/bash

# Check if Docker is installed
if ! command -v docker &> /dev/null
then
    echo "Docker could not be found. Please install Docker first."
    exit 1
fi

# Deploy the stack using docker-compose.yml
echo "Deploying project-management-system stack..."
docker stack deploy -c docker-compose.yml project-management-system

echo "Deployment complete."
