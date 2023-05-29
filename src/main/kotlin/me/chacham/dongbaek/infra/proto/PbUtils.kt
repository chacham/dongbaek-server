package me.chacham.dongbaek.infra.proto

import me.chacham.dongbaek.domain.schedule.*
import java.time.Duration
import java.time.Instant
import com.google.protobuf.Duration as PbDuration
import com.google.protobuf.Timestamp as PbTimestamp
import com.google.protobuf.duration as pbDuration
import com.google.protobuf.timestamp as pbTimestamp

object PbUtils {
    fun PbTimestamp.toInstant(): Instant {
        return Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
    }

    fun Instant.toPbTimestamp(): PbTimestamp {
        return pbTimestamp {
            seconds = this@toPbTimestamp.epochSecond
            nanos = this@toPbTimestamp.nano
        }
    }

    private fun PbDuration.toDuration(): Duration {
        return Duration.ofSeconds(this.seconds, this.nanos.toLong())
    }

    private fun Duration.toPbDuration(): PbDuration {
        return pbDuration {
            seconds = this@toPbDuration.seconds
            nanos = this@toPbDuration.nano
        }
    }

    fun PbSchedule.toSchedule(): Schedule {
        val id = ScheduleId(id)
        val startDate = startTimestamp.toInstant()
        val dueDate = dueTimestampOrNull?.toInstant()
        val finishDate = finishTimestampOrNull?.toInstant()
        val goal = goal.toGoal()
        val repeatInfo = repeatInfo.toRepeatInfo()
        return Schedule(id, title, startDate, dueDate, finishDate, goal, repeatInfo)
    }

    fun Schedule.toPbSchedule(): PbSchedule {
        return pbSchedule {
            id = this@toPbSchedule.id.value
            title = this@toPbSchedule.title
            startTimestamp = this@toPbSchedule.startInstant.toPbTimestamp()
            this@toPbSchedule.dueInstant.let { if (it != null) dueTimestamp = it.toPbTimestamp() }
            this@toPbSchedule.finishInstant.let { if (it != null) finishTimestamp = it.toPbTimestamp() }
            goal = this@toPbSchedule.goal.toPbGoal()
            repeatInfo = this@toPbSchedule.repeatInfo.toPbRepeatInfo()
        }
    }

    fun Goal.toPbGoal(): PbGoal {
        return when (this) {
            is QuantityGoal -> PbGoal.newBuilder().setQuantityGoal(quantity).build()
            is DurationGoal -> {
                val pbDuration = duration.toPbDuration()
                pbGoal { durationGoal = pbDuration }
            }
        }
    }

    fun PbGoal.toGoal(): Goal {
        if (hasQuantityGoal()) {
            return QuantityGoal(quantityGoal)
        }
        if (hasDurationGoal()) {
            val duration = durationGoal.toDuration()
            return DurationGoal(duration)
        }
        throw NotImplementedError()
    }

    fun RepeatInfo.toPbRepeatInfo(): PbRepeatInfo {
        return when (this) {
            is Unrepeated -> pbRepeatInfo { unrepeated = pbUnrepeated { } }
            is Periodic -> pbRepeatInfo {
                periodicRepeat = pbPeriodic {
                    periodDays = this@toPbRepeatInfo.periodDays
                    offsetDays = this@toPbRepeatInfo.offsetDays
                }
            }
        }
    }

    fun PbRepeatInfo.toRepeatInfo(): RepeatInfo {
        if (hasUnrepeated()) {
            return Unrepeated
        }
        if (hasPeriodicRepeat()) {
            return Periodic(periodicRepeat.periodDays, periodicRepeat.offsetDays)
        }
        throw NotImplementedError()
    }

    fun PbProgress.toProgress(): Progress {
        if (this.hasQuantityProgress()) {
            return QuantityProgress(
                ScheduleId(this.scheduleId),
                this.startTimestamp.toInstant(),
                this.endTimestampOrNull?.toInstant(),
                this.quantityProgress.quantity,
            )
        }
        if (this.hasDurationProgress()) {
            return DurationProgress(
                ScheduleId(this.scheduleId),
                this.startTimestamp.toInstant(),
                this.endTimestampOrNull?.toInstant(),
                this.durationProgress.duration.toDuration(),
                this.durationProgress.ongoingStartTimestampOrNull?.toInstant(),
            )
        }
        throw NotImplementedError()
    }

    fun Progress.toPbProgress(): PbProgress {
        val pbQuantityProgress =
            if (this is QuantityProgress) pbQuantityProgress { quantity = this@toPbProgress.quantity } else null
        val pbDurationProgress =
            if (this is DurationProgress) pbDurationProgress {
                duration = this@toPbProgress.duration.toPbDuration()
            } else null
        return pbProgress {
            scheduleId = this@toPbProgress.scheduleId.value
            startTimestamp = this@toPbProgress.startInstant.toPbTimestamp()
            this@toPbProgress.endInstant.let { if (it != null) endTimestamp = it.toPbTimestamp() }
            pbQuantityProgress.let { if (it != null) quantityProgress = it }
            pbDurationProgress.let { if (it != null) durationProgress = it }
        }
    }
}
