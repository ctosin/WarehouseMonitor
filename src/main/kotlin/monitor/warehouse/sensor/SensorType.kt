package monitor.warehouse.sensor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SensorType(
    val unit: String
) {
    @SerialName("temperature") TEMPERATURE("C"),
    @SerialName("humidity") HUMIDITY("%");
}
