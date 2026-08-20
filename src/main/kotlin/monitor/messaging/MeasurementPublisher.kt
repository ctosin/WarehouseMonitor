package monitor.messaging

fun interface MeasurementPublisher {

    fun publish(measurement: Measurement)
}
