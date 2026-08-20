package monitor.messaging

import com.hivemq.client.mqtt.datatypes.MqttQos
import kotlinx.serialization.json.Json

class MqttMeasurementPublisher(
    warehouseId: String,
    host: String,
    port: Int
) : MqttConnection("warehouse-$warehouseId", host, port), MeasurementPublisher {

    override fun publish(measurement: Measurement) {
        val topic = topicOf(measurement)

        client
            .publishWith()
            .topic(topic)
            .payload(Json.encodeToString(measurement).toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete { _, error ->
                if (error != null) {
                    log.warn("Failed to publish to $topic: ${error.message}")
                }
            }
    }

    private fun topicOf(measurement: Measurement): String {
        return "warehouse/${measurement.warehouseId}/${measurement.type.name.lowercase()}"
    }
}
