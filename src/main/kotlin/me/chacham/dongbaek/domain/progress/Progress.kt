package me.chacham.dongbaek.domain.progress

import me.chacham.dongbaek.domain.schedule.ScheduleId
import java.time.Duration
import java.time.Instant

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
    override val endInstant: Instant?,
    val quantity: Int,
) : Progress(scheduleId, startInstant, endInstant)

data class DurationProgress(
    override val scheduleId: ScheduleId,
    override val startInstant: Instant,
    override val endInstant: Instant?,
    val duration: Duration,
    val ongoingStartTime: Instant?,
) : Progress(scheduleId, startInstant, endInstant)
