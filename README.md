# Warehouse Monitor

A warehouse collects readings from environmental sensors over UDP and publishes them to a central
monitoring service, which compares each reading against a configured threshold and raises an alarm
on the console when one is exceeded.

Both services run from the same executable, selected by the first argument.

## Requirements

- JDK 25 (the Gradle toolchain will resolve one if it is not already installed)
- Docker, for the MQTT broker

## Running

Start the broker, then each service in its own terminal:

```bash
docker compose up -d                      # HiveMQ CE on localhost:1883

./gradlew run --args="central"            # central monitoring service
./gradlew run --args="warehouse 1"        # warehouse 1
```

The warehouse id is optional and defaults to the first warehouse in the configuration, so
`--args="warehouse"` is equivalent to `--args="warehouse 1"` here. A second warehouse is
configured, so `--args="warehouse 2"` can be started alongside the first to show the central
service overseeing more than one.

Both services run until interrupted. Stopping them with `Ctrl-C` makes Gradle report
`BUILD FAILED` for the killed process — that is Gradle reporting the signal, not a failure of the
application.

### Simulating a sensor

Any tool that sends a UDP datagram will do:

```bash
echo "sensor_id=t1; value=30" | nc -u -w1 localhost 3344   # within limits
echo "sensor_id=t1; value=42" | nc -u -w1 localhost 3344   # alarm
echo "sensor_id=h1; value=88" | nc -u -w1 localhost 3355   # alarm
```

## Configuration

`src/main/resources/config.json` holds both services' configuration. Warehouses own their sensor
ports; the central service owns the thresholds.

It is validated on load and the process refuses to start on anything invalid.

## Design notes

**No polling, and no thread per sensor.** Both services are event-driven. A warehouse's sensor
listeners are coroutines over ktor's selector-based UDP sockets: `receive()` suspends rather than
holding a thread, so every sensor in a warehouse is served by one event loop instead of one thread
per port. Publishing uses the MQTT client's asynchronous API and returns immediately, with delivery
reported to a callback. The central service never asks the broker for anything — measurements are
pushed to it as they arrive.

Each service blocks in exactly two places, and they are not the same two. Both bound their initial
broker connect with a timeout. After that, the warehouse's `runBlocking` *is* the event loop its
listeners run on, so its main thread is busy serving sensors; the central service's
`runBlocking { awaitCancellation() }` merely parks its main thread, because its work happens on the
MQTT client's own threads.

**One executable, two services.** `monitor.MonitorApp` dispatches on the first argument. The two
services share the measurement contract, the configuration and the MQTT plumbing, so keeping them
in one artifact avoids duplicating all three. In a real deployment they would be separate
processes, which the mode argument already allows.

**MQTT between the services.** Sensor telemetry is what MQTT exists for, and a broker decouples the
two sides: warehouses do not need to know where the central service is, or whether it is running.
Measurements are published to `warehouse/{id}/{type}` at QoS 1.

**The measurement is self-describing.** The payload repeats the warehouse id and sensor type that
the topic already encodes. This is a deliberate duplication: the central service can act on a
message without parsing the topic it arrived on, which keeps the routing and the domain model
independent.

**Publishing never blocks the sensor loop.** All of a warehouse's listeners share one thread, so
publishing is fire-and-forget; a failure is logged and the reading dropped. A slow or unavailable
broker must not stop the warehouse from reading its sensors.

**Bad input is data, not an exception.** `MeasurementParser` returns a `ParseResult` rather than
throwing, and the central service drops payloads it cannot decode. Anything that can reach the UDP
port can send arbitrary bytes, and one malformed datagram must not take a listener — or the whole
process — down.

**The alarm decision is separate from reporting.** `AlarmEvaluator` maps a measurement to an
`AlarmVerdict` with no logging or transport involved, so the rule the brief is actually about can
be tested directly.

## Assumptions

**"Exceeds" is read strictly.** A reading equal to its threshold is within limits; 35 °C does not
raise an alarm, 36 °C does. `AlarmEvaluatorTest` pins this down in both directions, since it is the
one place the wording is open to interpretation.

**One process per role.** MQTT client identifiers are stable (`warehouse-<id>`, `central`). Running
two processes with the same role would make them evict each other from the broker, so each
warehouse id is expected to run once, as is the central service.

**Sensors are trusted.** The listener binds `0.0.0.0` because sensors are remote devices, and there
is no authentication on the UDP port or on the broker. Both would be required outside a local
exercise.

**A measurement's timestamp comes from the warehouse**, taken when the datagram is read rather than
when the central service processes it, so queueing delay is visible in the logs rather than hidden.

## Known gaps

- No graceful shutdown; `Ctrl-C` kills the process rather than closing sockets and the MQTT
  connection cleanly.
- The central service holds no state, so it reports on readings as they arrive but cannot say
  anything about a sensor that has gone silent.
- Test coverage is partial. Only the pure decision logic is covered — `AlarmEvaluatorTest` and
  `MeasurementParserTest`. There are no tests for the MQTT publish/subscribe round trip, the UDP
  `SensorListener`, configuration loading, or the service wiring in `MonitorApp`.
