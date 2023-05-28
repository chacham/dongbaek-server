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
        val startDate = Instant.ofEpochSecond(startDate.seconds)
        val dueDate = if (hasDueDate()) Instant.ofEpochSecond(dueDate.seconds) else null
        val finishDate = if (hasFinishDate()) Instant.ofEpochSecond(finishDate.seconds) else null
        val goal = goal.toGoal()
        val repeatInfo = repeatInfo.toRepeatInfo()
        return Schedule(id, title, startDate, dueDate, finishDate, goal, repeatInfo)
    }

    private fun PbGoal.toGoal(): Goal {
        if (hasQuantityGoal()) {
            return QuantityGoal(quantityGoal)
        }
        if (hasDurationGoal()) {
            val duration = Duration.ofSeconds(durationGoal.seconds)
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
            .setStartDate(PbTimestamp.newBuilder().setSeconds(schedule.startDate.epochSecond))
            .setGoal(toPbGoal(schedule.goal))
            .setRepeatInfo(toPbRepeatInfo(schedule.repeatInfo))
        if (schedule.dueDate != null) {
            builder.setDueDate(PbTimestamp.newBuilder().setSeconds(schedule.dueDate.epochSecond))
        }
        if (schedule.finishDate != null) {
            builder.setFinishDate(PbTimestamp.newBuilder().setSeconds(schedule.finishDate.epochSecond))
        }
        return builder.build()
    }

    private fun toPbGoal(goal: Goal): PbGoal {
        return when (goal) {
            is QuantityGoal -> PbGoal.newBuilder().setQuantityGoal(goal.quantity).build()
            is DurationGoal -> {
                val durationSec = goal.duration.seconds
                val pbDuration = com.google.protobuf.Duration.newBuilder().setSeconds(durationSec).build()
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
                this.startDate.toInstant(),
                this.endDateOrNull?.toInstant(),
                this.quantityProgress.value,
            )
        }
        if (this.hasDurationProgress()) {
            return DurationProgress(
                ScheduleId(this.scheduleId),
                this.startDate.toInstant(),
                this.endDateOrNull?.toInstant(),
                this.durationProgress.value.toDuration(),
                this.durationProgress.ongoingStartTime.toInstant(),
            )
        }
        throw NotImplementedError()
    }

    fun Progress.toPbProgress(): PbProgress {
        val p = this
        val pbQuantityProgress =
            if (p is QuantityProgress) pbQuantityProgress { value = p.quantity } else null
        val pbDurationProgress =
            if (p is DurationProgress) pbDurationProgress {
                value = p.duration.toPbDuration()
            } else null
        return pbProgress {
            scheduleId = p.scheduleId.value
            startDate = p.startDate.toPbTimestamp()
            p.endDate.let { if (it != null) endDate = it.toPbTimestamp() }
            pbQuantityProgress.let { if (it != null) quantityProgress = it }
            pbDurationProgress.let { if (it != null) durationProgress = it }
        }
    }
}
