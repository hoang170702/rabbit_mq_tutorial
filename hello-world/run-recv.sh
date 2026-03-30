#!/bin/bash
# Script to run Recv (Consumer) class

cd "$(dirname "$0")"

echo "Starting RabbitMQ Consumer (Recv)..."
mvn compile exec:java -Dexec.mainClass="learn.helloworld.consumer.Recv"
