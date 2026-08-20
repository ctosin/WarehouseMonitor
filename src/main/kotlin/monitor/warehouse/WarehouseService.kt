package monitor.warehouse

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import monitor.config.ConfigLoader
import monitor.messaging.MqttMeasurementPublisher
import monitor.warehouse.sensor.SensorListener
import org.slf4j.LoggerFactory

object WarehouseService {
    private val log = LoggerFactory.getLogger(WarehouseService::class.java)

    fun start(warehouseId: String?) {
        val config = ConfigLoader.config
        val realWarehouseId = warehouseId ?: config.singleWarehouseId()
        val sensors = config.warehouseById(realWarehouseId).sensors

        val publisher = MqttMeasurementPublisher(
            warehouseId = realWarehouseId,
            host = config.broker.host,
            port = config.broker.port
        )
        publisher.connect()

        log.info("Warehouse $realWarehouseId started")

        publisher.use {
            runBlocking {
                sensors.forEach { sensor ->
                    launch {
                        SensorListener(
                            warehouseId = realWarehouseId,
                            type = sensor.type,
                            port = sensor.port,
                            publisher = publisher
                        ).listen()
                    }
                }
            }
        }
    }
}
