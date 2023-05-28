package me.chacham.dongbaek.domain.schedule

import java.time.Duration
import java.time.Instant

data class ProgressId(val value: String)

sealed class Progress(
    open val scheduleId: ScheduleId,
    open val startDate: Instant,
    open val endDate: Instant?,
) {
    fun getId(): ProgressId {
        return ProgressId("${scheduleId.value}_${startDate}")
    }
}

data class QuantityProgress(
    override val scheduleId: ScheduleId,
    override val startDate: Instant,
    override val endDate: Instant? = null,
    val quantity: Int = 0,
) : Progress(scheduleId, startDate, endDate)

data class DurationProgress(
    override val scheduleId: ScheduleId,
    override val startDate: Instant,
    override val endDate: Instant? = null,
    val duration: Duration = Duration.ZERO,
    val ongoingStartTime: Instant? = null,
) : Progress(scheduleId, startDate, endDate)
