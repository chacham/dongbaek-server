package me.chacham.dongbaek.domain.schedule

import java.time.Duration
import java.time.Instant

data class ProgressId(val value: String)

sealed class Progress(
    open val scheduleId: ScheduleId,
    open val startInstant: Instant,
    open val endInstant: Instant?,
) {
    fun getId(): ProgressId {
        return ProgressId("${scheduleId.value}_${startInstant}")
    }
}

data class QuantityProgress(
    override val scheduleId: ScheduleId,
    override val startInstant: Instant,
    override val endInstant: Instant? = null,
    val quantity: Int = 0,
) : Progress(scheduleId, startInstant, endInstant)

data class DurationProgress(
    override val scheduleId: ScheduleId,
    override val startInstant: Instant,
    override val endInstant: Instant? = null,
    val duration: Duration = Duration.ZERO,
    val ongoingStartTime: Instant? = null,
) : Progress(scheduleId, startInstant, endInstant)
