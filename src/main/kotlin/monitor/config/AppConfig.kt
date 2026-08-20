package monitor.config

import kotlinx.serialization.Serializable
import monitor.util.fail
import monitor.warehouse.sensor.SensorType

@Serializable
data class AppConfig(
    val warehouses: List<WarehouseConfig> = emptyList(),
    val central: CentralConfig = CentralConfig(),
    val broker: BrokerConfig = BrokerConfig()
) {

    init {
        if (warehouses.isEmpty()) {
            fail("Warehouses are empty")
        }

        val ids = warehouses.map { it.id }.toSet()
        if (warehouses.size != ids.size) {
            fail("Warehouse IDs must be unique")
        }
    }

    fun warehouseById(id: String): WarehouseConfig {
        return warehouses
            .find { it.id == id }
            ?: throw IllegalArgumentException("Warehouse $id not found")
    }

    fun singleWarehouseId(): String {
        return warehouses.first().id
    }
}

@Serializable
data class BrokerConfig(
    val host: String = "localhost",
    val port: Int = 1883
) {

    init {
        if (host.isBlank()) {
            fail("Invalid broker host")
        }

        if (port !in (1..65535)) {
            fail("Invalid broker port: $port")
        }
    }
}

@Serializable
data class WarehouseConfig(
    val id: String,
    val sensors: List<WarehouseSensor> = emptyList()
) {

    init {
        if (id.isBlank()) {
            fail("Invalid warehouse id")
        }

        if (sensors.isEmpty()) {
            fail("Sensors in the warehouse are empty")
        }

        val ports = sensors.map { it.port }.toSet()
        if (sensors.size != ports.size) {
            fail("Sensor ports must be unique")
        }
    }
}

@Serializable
data class WarehouseSensor(
    val type: SensorType,
    val port: Int
) {

    init {
        if (port !in (1..65535)) {
            fail("Invalid port number: $port")
        }
    }
}

@Serializable
data class CentralConfig(
    val sensors: List<SensorSpec> = emptyList()
) {

    init {
        if (sensors.isEmpty()) {
            fail("Sensors are empty")
        }

        val specTypes = sensors.map { it.type }.toSet()
        if (sensors.size != specTypes.size) {
            fail("Sensor spec types must be unique")
        }
    }
}

@Serializable
data class SensorSpec(
    val type: SensorType,
    val threshold: Int
) {

    init {
        if (threshold !in (1..500)) {
            fail("Invalid sensor threshold: $threshold")
        }
    }
}

