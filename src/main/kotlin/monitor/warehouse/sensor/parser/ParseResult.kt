package monitor.warehouse.sensor.parser

sealed interface ParseResult {

    data class Success(val sensorId: String, val value: Int) : ParseResult

    data class Failure(val reason: String) : ParseResult
}
