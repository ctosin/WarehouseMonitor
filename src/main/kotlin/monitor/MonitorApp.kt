package monitor

import monitor.central.CentralService
import monitor.util.fail
import monitor.warehouse.WarehouseService
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private const val WAREHOUSE_MODE = "warehouse"
private const val CENTRAL_MODE = "central"

private val log = LoggerFactory.getLogger("monitor.MonitorApp")

fun main(vararg args: String) {
    try {
        when (val mode = args.firstOrNull()) {
            WAREHOUSE_MODE -> WarehouseService.start(args.getOrNull(1))
            CENTRAL_MODE -> CentralService.start()
            else -> fail("Usage: <$WAREHOUSE_MODE [warehouseId] | $CENTRAL_MODE>, got: ${mode ?: "no arguments"}")
        }
    } catch (e: Exception) {
        log.error(e.message ?: e.toString())
        log.debug("Startup failed", e)
        exitProcess(1)
    }
}
