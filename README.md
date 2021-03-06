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
As an added bonus, Leeches also come with a simple WebSocket server for generating traffic called Pump. Pump simply waits for inbound connections and then starts pumping simple text messages to the receiving endpoint. It supports a similar set of parameters as Leeches - you can configure the throttle rate, timeouts, and so forth.
```sh
./gradlew pump -Dlwv.port=8080 -Dlwv.path=/
```

The following is a complete list of supported properties and their default values. All times are in milliseconds.
```
lwv.delay.max       : 0
lwv.delay.min       : 0
lwv.idleTimeout     : 600000
lwv.maxBacklog      : 10000
lwv.message         : hello
lwv.path            : /
lwv.pingInterval    : 60000
lwv.port            : 8080
lwv.reportInterval  : 5000
```

# Running from Docker
You can run the gradle build from a JDK-equipped Docker base image, such as `openjdk:8u131-jdk-alpine`. Assuming that leeches is cloned into `~/code/leecheswithvacuums`, run the following:
```sh
docker run -it -p 8080:8080 -v ~/code/leecheswithvacuums:/leeches openjdk:8u131-jdk-alpine
cd leeches
./gradlew pump -Dlwv.port=8080 -Dlwv.path=/ 
```

# YourKit
YourKit is kindly supporting open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications. Take a look at YourKit's leading software products: [YourKit Java Profiler](https://www.yourkit.com/java/profiler/) and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler).

<img src="https://www.yourkit.com/images/yklogo.png"/>
We cannot recommend YourKit enough. It's a superb profiling tool with great Eclipse integration. If you're serious about Java development, you need to be using YourKit.
