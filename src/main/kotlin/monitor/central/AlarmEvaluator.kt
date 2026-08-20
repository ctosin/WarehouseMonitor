package monitor.central

import monitor.messaging.Measurement
import monitor.warehouse.sensor.SensorType

class AlarmEvaluator(
    private val thresholds: Map<SensorType, Int>
) {

    fun evaluate(measurement: Measurement): AlarmVerdict {
        val threshold = thresholds[measurement.type] ?: return AlarmVerdict.NoThresholdConfigured

        return if (measurement.value > threshold) {
            AlarmVerdict.Alarm(threshold)
        } else {
            AlarmVerdict.WithinLimits(threshold)
        }
    }

    fun describeThresholds(): String {
        return thresholds
            .entries
            .joinToString { (type, threshold) -> "$type=$threshold${type.unit}" }
    }
}
