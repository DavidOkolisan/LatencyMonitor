#!/bin/bash

# MVN installation on the server is required
echo "Build project..."
mvn clean install > /dev/null
echo "Build complete..."

echo "Deploy jar with dependencies..."
mkdir -p ~/LatencyMonitor
cp -R ./target/lib  ~/LatencyMonitor/ > /dev/null
echo "Setup input.txt..."
cp ./src/main/resources/input.txt ~/LatencyMonitor/
echo "Setup log4j properties..."
cp ./src/main/resources/log4j.properties ~/LatencyMonitor/
echo "Deploy LatencyMonitor..."
cp ./target/LatencyMonitor.jar ~/LatencyMonitor/
echo "Deploy complete..."
