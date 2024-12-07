#!/bin/bash

# Start Kafka
start-kafka.sh &
KAFKA_PID=$!

# Function to check if Kafka is ready
is_kafka_ready() {
    kafka-topics.sh --list --bootstrap-server localhost:9092 > /dev/null 2>&1
    return $?
}

# Function to create a Kafka topic if it does not already exist
create_topic_if_not_exists() {
    local topic_name=$1
    local replication_factor=$2
    local partitions=$3

    # Check if the topic already exists
    if kafka-topics.sh --list --bootstrap-server localhost:9092 | grep -q "^${topic_name}$"; then
        echo "Topic ${topic_name} already exists."
    else
        kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor $replication_factor --partitions $partitions --topic ${topic_name}
        echo "Topic ${topic_name} created with replication factor ${replication_factor} and partitions ${partitions}."
    fi
}

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
while ! is_kafka_ready; do
    echo "Kafka is not ready yet. Waiting..."
    sleep 5
done
echo "Kafka is ready."

# Define topics along with their desired replication factors and partitions
topics=(
    "user-details-created:1:1"
    "profile-completed:1:1"
    "user-details-failed:1:1"
    "profile-completed-failed:1:1"
)

# Loop through the list and create each topic with its specified settings
for topic_info in "${topics[@]}"; do
    IFS=':' read -r topic_name replication_factor partitions <<< "$topic_info"
    create_topic_if_not_exists $topic_name $replication_factor $partitions
done

# Wait for Kafka to exit (if running in foreground)
wait $KAFKA_PID