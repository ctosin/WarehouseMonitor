package monitor.config

import kotlinx.serialization.json.Json

object ConfigLoader {
    private const val CONFIG_FILE_NAME = "config.json"

    val config: AppConfig by lazy { Json.decodeFromString(readResource()) }

    private fun readResource(): String {
        return javaClass
            .classLoader
            .getResourceAsStream(CONFIG_FILE_NAME)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalArgumentException("Resource file not found: $CONFIG_FILE_NAME")
    }
}
