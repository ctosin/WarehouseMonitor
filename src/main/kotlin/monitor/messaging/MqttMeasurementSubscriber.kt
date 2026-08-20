package monitor.messaging

import kotlinx.serialization.json.Json

class MqttMeasurementSubscriber(
    host: String,
    port: Int
) : MqttConnection("central", host, port) {

    fun subscribe(onMeasurement: (Measurement) -> Unit) {
        client
            .subscribeWith()
            .topicFilter(TOPIC_FILTER)
            .callback { publish ->
                val payload = String(publish.payloadAsBytes)

                try {
                    Json.decodeFromString<Measurement>(payload)
                } catch (_: Exception) {
                    /* One warehouse publishing something unreadable must not stop the subscription,
                       so decoding failures are logged and dropped instead of reaching the handler. */
                    log.warn("Discarding unreadable message on ${publish.topic}: $payload")
                    null
                }
                ?.let(onMeasurement)
            }
            .send()
            .join()

        log.info("Subscribed to $TOPIC_FILTER")
    }

    private companion object {
        private const val TOPIC_FILTER = "warehouse/+/+"
    }
}
