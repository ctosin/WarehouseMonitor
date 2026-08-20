package monitor.messaging

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import monitor.util.fail
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

abstract class MqttConnection(
    identifier: String,
    private val host: String,
    private val port: Int
) : AutoCloseable {

    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    protected val client = Mqtt5Client
        .builder()
        .identifier(identifier)
        .serverHost(host)
        .serverPort(port)
        .automaticReconnectWithDefaultConfig()
        .buildAsync()

    fun connect() {
        try {
            client
                .connectWith()
                .cleanStart(true)
                .send()
                .orTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .join()
        } catch (_: Exception) {
            runCatching { client.disconnect() }
            fail("Cannot connect to the MQTT broker at $host:$port")
        }

        log.info("Connected to the MQTT broker at $host:$port")
    }

    override fun close() {
        client.disconnect()
    }

    private companion object {
        private const val CONNECT_TIMEOUT_SECONDS = 10L
    }
}
