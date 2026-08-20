package monitor.central

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import monitor.config.ConfigLoader
import monitor.messaging.Measurement
import monitor.messaging.MqttMeasurementSubscriber
import monitor.util.formatTimestamp
import org.slf4j.LoggerFactory

object CentralService {
    private val log = LoggerFactory.getLogger(CentralService::class.java)

    fun start() {
        val config = ConfigLoader.config
        val broker = config.broker
        val evaluator = AlarmEvaluator(config.central.sensors.associate { it.type to it.threshold })

        val subscriber = MqttMeasurementSubscriber(
            host = broker.host,
            port = broker.port
        )
        subscriber.connect()

        log.info("Central monitoring service started, thresholds: ${evaluator.describeThresholds()}")

        subscriber.use {
            it.subscribe { measurement -> report(measurement, evaluator) }
            runBlocking { awaitCancellation() }
        }
    }

    private fun report(measurement: Measurement, evaluator: AlarmEvaluator) {
        val unit = measurement.type.unit
        val reading = "warehouse=${measurement.warehouseId} sensor=${measurement.sensorId} " +
            "${measurement.type}=${measurement.value}$unit at ${measurement.timestamp.formatTimestamp()}"

        when (val verdict = evaluator.evaluate(measurement)) {
            is AlarmVerdict.Alarm ->
                log.warn("ALARM $reading exceeds threshold ${verdict.threshold}$unit")

            is AlarmVerdict.WithinLimits ->
                log.info("OK $reading (threshold ${verdict.threshold}$unit)")

            AlarmVerdict.NoThresholdConfigured ->
                log.warn("No threshold configured for ${measurement.type}, ignoring $measurement")
        }
    }
}
