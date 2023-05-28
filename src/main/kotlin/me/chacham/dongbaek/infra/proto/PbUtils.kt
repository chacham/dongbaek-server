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
        return Instant.ofEpochSecond(this.seconds)
    }

    fun Instant.toPbTimestamp(): PbTimestamp {
        return pbTimestamp { seconds = this.seconds }
    }

    fun PbDuration.toDuration(): Duration {
        return Duration.ofSeconds(this.seconds, this.nanos.toLong())
    }

    fun Duration.toPbDuration(): PbDuration {
        return pbDuration {
            seconds = this.seconds
            nanos = this.nanos
        }
    }

    fun PbSchedule.toSchedule(): Schedule {
        val id = ScheduleId(id)
        val startDate = Instant.ofEpochSecond(startTimestamp.seconds)
        val dueDate = dueTimestampOrNull?.toInstant()
        val finishDate = finishTimestampOrNull?.toInstant()
        val goal = goal.toGoal()
        val repeatInfo = repeatInfo.toRepeatInfo()
        return Schedule(id, title, startDate, dueDate, finishDate, goal, repeatInfo)
    }

    private fun PbGoal.toGoal(): Goal {
        if (hasQuantityGoal()) {
            return QuantityGoal(quantityGoal)
        }
        if (hasDurationGoal()) {
            val duration = durationGoal.toDuration()
            return DurationGoal(duration)
        }
        throw NotImplementedError()
    }

    private fun PbRepeatInfo.toRepeatInfo(): RepeatInfo {
        if (hasUnrepeated()) {
            return Unrepeated
        }
        if (hasPeriodicRepeat()) {
            return Periodic(periodicRepeat.periodDays, periodicRepeat.offsetDays)
        }
        throw NotImplementedError()
    }

    fun toPbSchedule(schedule: Schedule): PbSchedule {
        val builder = PbSchedule.newBuilder()
            .setId(schedule.id.value)
            .setTitle(schedule.title)
            .setStartTimestamp(PbTimestamp.newBuilder().setSeconds(schedule.startInstant.epochSecond))
            .setGoal(toPbGoal(schedule.goal))
            .setRepeatInfo(toPbRepeatInfo(schedule.repeatInfo))
        if (schedule.dueInstant != null) {
            builder.setDueTimestamp(PbTimestamp.newBuilder().setSeconds(schedule.dueInstant.epochSecond))
        }
        if (schedule.finishInstant != null) {
            builder.setFinishTimestamp(PbTimestamp.newBuilder().setSeconds(schedule.finishInstant.epochSecond))
        }
        return builder.build()
    }

    private fun toPbGoal(goal: Goal): PbGoal {
        return when (goal) {
            is QuantityGoal -> PbGoal.newBuilder().setQuantityGoal(goal.quantity).build()
            is DurationGoal -> {
                val pbDuration = goal.duration.toPbDuration()
                PbGoal.newBuilder().setDurationGoal(pbDuration).build()
            }
        }
    }

    private fun toPbRepeatInfo(repeatInfo: RepeatInfo): PbRepeatInfo {
        return when (repeatInfo) {
            is Unrepeated -> PbRepeatInfo.newBuilder().setUnrepeated(PbRepeatInfo.newBuilder().unrepeated).build()
            is Periodic -> {
                val (periodDays, offsetDays) = repeatInfo
                val pbPeriodic = PbPeriodic.newBuilder().setPeriodDays(periodDays).setOffsetDays(offsetDays).build()
                PbRepeatInfo.newBuilder().setPeriodicRepeat(pbPeriodic).build()
            }
        }
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
        val p = this
        val pbQuantityProgress =
            if (p is QuantityProgress) pbQuantityProgress { quantity = p.quantity } else null
        val pbDurationProgress =
            if (p is DurationProgress) pbDurationProgress {
                duration = p.duration.toPbDuration()
            } else null
        return pbProgress {
            scheduleId = p.scheduleId.value
            startTimestamp = p.startInstant.toPbTimestamp()
            p.endInstant.let { if (it != null) endTimestamp = it.toPbTimestamp() }
            pbQuantityProgress.let { if (it != null) quantityProgress = it }
            pbDurationProgress.let { if (it != null) durationProgress = it }
        }
    }
}
