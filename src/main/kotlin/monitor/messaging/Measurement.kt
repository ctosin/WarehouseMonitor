package monitor.messaging

import kotlinx.serialization.Serializable
import monitor.warehouse.sensor.SensorType

@Serializable
data class Measurement(
    val warehouseId: String,
    val sensorId: String,
    val type: SensorType,
    val value: Int,
    val timestamp: Long
)
