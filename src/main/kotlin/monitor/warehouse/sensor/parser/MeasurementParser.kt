package monitor.warehouse.sensor.parser

import monitor.util.fail

object MeasurementParser {

    fun parse(input: String): ParseResult {
        return try {
            input
                .split(";")
                .associate { entry ->
                    val parts = entry.split("=").map { it.trim() }
                    parts[0] to parts[1]
                }
                .let { fields ->
                    val sensorId = fields["sensor_id"] ?: fail("sensor_id is missing")
                    val rawValue = fields["value"] ?: fail("value is missing")

                    ParseResult.Success(
                        sensorId = sensorId,
                        value = rawValue.toIntOrNull() ?: fail("value is not an integer: $rawValue"),
                    )
                }
        } catch (_: IndexOutOfBoundsException) {
            ParseResult.Failure("Input format is invalid")
        } catch (e: Exception) {
            ParseResult.Failure(e.message ?: "Unknown error")
        }
    }
}
