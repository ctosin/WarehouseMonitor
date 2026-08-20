package monitor.warehouse.sensor

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import monitor.messaging.Measurement
import monitor.messaging.MeasurementPublisher
import monitor.warehouse.sensor.parser.MeasurementParser
import monitor.warehouse.sensor.parser.ParseResult
import org.slf4j.LoggerFactory

class SensorListener(
    private val warehouseId: String,
    private val type: SensorType,
    private val port: Int,
    private val publisher: MeasurementPublisher,
    private val clock: () -> Long = System::currentTimeMillis
) {

    suspend fun listen() {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager)
            .udp()
            .bind(InetSocketAddress("0.0.0.0", port))

        log.info("Listening for $type sensors on UDP $port")

        try {
            while (true) {
                val parseResult = serverSocket
                    .receive()
                    .packet
                    .readText()
                    .let { MeasurementParser.parse(it) }

                when (parseResult) {
                    is ParseResult.Success -> {
                        log.info("Sensor type $type ($warehouseId) received: $parseResult")
                        publisher.publish(
                            Measurement(
                                warehouseId = warehouseId,
                                sensorId = parseResult.sensorId,
                                type = type,
                                value = parseResult.value,
                                timestamp = clock()
                            )
                        )
                    }
                    is ParseResult.Failure -> log.warn("Sensor type $type ($warehouseId) parse failed: $parseResult")
                }
            }
        } finally {
            selectorManager.use {
                serverSocket.close()
            }
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(SensorListener::class.java)
    }
}
