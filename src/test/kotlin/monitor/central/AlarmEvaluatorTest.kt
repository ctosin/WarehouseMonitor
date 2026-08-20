package monitor.central

import monitor.messaging.Measurement
import monitor.warehouse.sensor.SensorType
import kotlin.test.Test
import kotlin.test.assertEquals

class AlarmEvaluatorTest {

    private val evaluator = AlarmEvaluator(
        mapOf(
            SensorType.TEMPERATURE to 35,
            SensorType.HUMIDITY to 50
        )
    )

    private fun reading(type: SensorType, value: Int) = Measurement(
        warehouseId = "1",
        sensorId = "s1",
        type = type,
        value = value,
        timestamp = 1_700_000_000_000L
    )

    @Test
    fun `raises an alarm above the temperature threshold`() {
        assertEquals(
            AlarmVerdict.Alarm(threshold = 35),
            evaluator.evaluate(reading(SensorType.TEMPERATURE, 36))
        )
    }

    @Test
    fun `raises an alarm above the humidity threshold`() {
        assertEquals(
            AlarmVerdict.Alarm(threshold = 50),
            evaluator.evaluate(reading(SensorType.HUMIDITY, 88))
        )
    }

    @Test
    fun `stays within limits below the threshold`() {
        assertEquals(
            AlarmVerdict.WithinLimits(threshold = 35),
            evaluator.evaluate(reading(SensorType.TEMPERATURE, 30))
        )
    }

    @Test
    fun `a reading exactly on the threshold does not raise an alarm`() {
        // 'exceed' is read strictly: 35 C is the limit, not a breach of it
        assertEquals(
            AlarmVerdict.WithinLimits(threshold = 35),
            evaluator.evaluate(reading(SensorType.TEMPERATURE, 35))
        )
        assertEquals(
            AlarmVerdict.WithinLimits(threshold = 50),
            evaluator.evaluate(reading(SensorType.HUMIDITY, 50))
        )
    }

    @Test
    fun `one over the threshold is the first alarm`() {
        assertEquals(AlarmVerdict.WithinLimits(35), evaluator.evaluate(reading(SensorType.TEMPERATURE, 35)))
        assertEquals(AlarmVerdict.Alarm(35), evaluator.evaluate(reading(SensorType.TEMPERATURE, 36)))
    }

    @Test
    fun `each sensor type is judged against its own threshold`() {
        // 40 is under the humidity limit but over the temperature one
        assertEquals(AlarmVerdict.Alarm(35), evaluator.evaluate(reading(SensorType.TEMPERATURE, 40)))
        assertEquals(AlarmVerdict.WithinLimits(50), evaluator.evaluate(reading(SensorType.HUMIDITY, 40)))
    }

    @Test
    fun `readings below zero stay within limits`() {
        assertEquals(
            AlarmVerdict.WithinLimits(threshold = 35),
            evaluator.evaluate(reading(SensorType.TEMPERATURE, -12))
        )
    }

    @Test
    fun `reports when no threshold is configured for the sensor type`() {
        val temperatureOnly = AlarmEvaluator(mapOf(SensorType.TEMPERATURE to 35))

        assertEquals(
            AlarmVerdict.NoThresholdConfigured,
            temperatureOnly.evaluate(reading(SensorType.HUMIDITY, 88))
        )
    }

    @Test
    fun `describes its thresholds with their units`() {
        assertEquals("TEMPERATURE=35C, HUMIDITY=50%", evaluator.describeThresholds())
    }
}
