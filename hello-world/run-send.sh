#!/bin/bash
# Script to run Send (Producer) class

cd "$(dirname "$0")"

echo "Starting RabbitMQ Producer (Send)..."
mvn compile exec:java -Dexec.mainClass="learn.helloworld.producer.Send"
