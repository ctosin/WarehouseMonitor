package monitor.central

sealed interface AlarmVerdict {

    data class Alarm(val threshold: Int) : AlarmVerdict

    data class WithinLimits(val threshold: Int) : AlarmVerdict

    data object NoThresholdConfigured : AlarmVerdict
}
