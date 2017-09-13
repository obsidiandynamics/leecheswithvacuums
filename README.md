Leeches with Vacuums
===
Simple WebSocket throughput testing tool.

# Usage
Runs entirely through a [Gradle](https://github.com/gradle/gradle) task `runLWV`, passing in the required system properties.

## Simple example
```sh
./gradlew leeches -Dlwv.url="ws://localhost:8080/endpoint" -Dlwv.connections=100
```
Connects to the given URL using 100 connections, consuming messages as fast as the bandwidth allows.

## Rate limiting
```sh
./gradlew leeches -Dlwv.url="ws://localhost:8080/endpoint" -Dlwv.delay.min=5 -Dlwv.delay.max=10 -Dlwv.connections=100
```
Applies a varying degree of rate limiting - a random time penalty ranging from 5 ms per message (best case) to 10 ms (worst case).

## Supported properties
The following is a complete list of supported properties and their default values. All times are in milliseconds.
```
lwv.connections     : 1
lwv.delay.max       : 0
lwv.delay.min       : 0
lwv.idleTimeout     : 600000
lwv.reportInterval  : 5000
lwv.url             : ws://localhost:8080
```

# Pump
Leeches also come with a simple WebSocket server for generating traffic called Pump. Pump simply waits for inbound connections and then starts

The following is a complete list of supported properties and their default values. All times are in milliseconds.
```
lwv.delay.max       : 0
lwv.delay.min       : 0
lwv.idleTimeout     : 600000
lwv.maxBacklog      : 100
lwv.message         : hello
lwv.pingInterval    : 60000
lwv.reportInterval  : 5000
```