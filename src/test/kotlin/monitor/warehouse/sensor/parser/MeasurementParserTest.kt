package monitor.warehouse.sensor.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MeasurementParserTest {

    @Test
    fun `parses the measurement syntax from the specification`() {
        assertEquals(
            MeasurementParser.parse("sensor_id=t1; value=30"),
            ParseResult.Success(sensorId = "t1", value = 30)
        )
        assertEquals(
            MeasurementParser.parse("sensor_id=h1; value=40"),
            ParseResult.Success(sensorId = "h1", value = 40)
        )
    }

    @Test
    fun `tolerates surrounding whitespace and missing separator spaces`() {
        val expected = ParseResult.Success(sensorId = "t1", value = 30)

        assertEquals(expected, MeasurementParser.parse("sensor_id=t1;value=30"))
        assertEquals(expected, MeasurementParser.parse("  sensor_id = t1 ;  value = 30  "))
    }

    @Test
    fun `accepts fields in any order and ignores unknown fields`() {
        assertEquals(
            MeasurementParser.parse("value=30; sensor_id=t1; battery=87"),
            ParseResult.Success(sensorId = "t1", value = 30)
        )
    }

    @Test
    fun `reports a missing sensor_id`() {
        assertEquals(MeasurementParser.parse("value=30"), ParseResult.Failure("sensor_id is missing"))
    }

    @Test
    fun `reports a missing value`() {
        assertEquals(MeasurementParser.parse("sensor_id=t1"), ParseResult.Failure("value is missing"))
    }

    @Test
    fun `reports a non-integer value and echoes it back`() {
        assertEquals(
            MeasurementParser.parse("sensor_id=t1; value=abc"),
            ParseResult.Failure("value is not an integer: abc")
        )

        assertEquals(
            MeasurementParser.parse("sensor_id=t1; value=21.5"),
            ParseResult.Failure("value is not an integer: 21.5")
        )
    }

    @Test
    fun `reports malformed input rather than throwing`() {
        listOf("", "   ", "rubbish", "sensor_id", "=;=", "sensor_id=t1; value").forEach { input ->
            assertIs<ParseResult.Failure>(MeasurementParser.parse(input), "input was: '$input'")
        }
    }

    @Test
    fun `accepts negative and zero readings`() {
        assertEquals(
            MeasurementParser.parse("sensor_id=t1; value=-12"),
            ParseResult.Success(sensorId = "t1", value = -12)
        )
        assertEquals(
            MeasurementParser.parse("sensor_id=t1; value=0"),
            ParseResult.Success(sensorId = "t1", value = 0)
        )
    }
}
