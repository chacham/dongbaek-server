package me.chacham.dongbaek.infra.proto

import com.google.protobuf.Timestamp
import me.chacham.dongbaek.domain.schedule.*
import java.time.Duration
import java.time.Instant

object PbConverter {
    fun fromPbSchedule(pbSchedule: PbSchedule): Schedule {
        val id = ScheduleId(pbSchedule.id)
        val startDate = Instant.ofEpochSecond(pbSchedule.startDate.seconds)
        val dueDate = if (pbSchedule.hasDueDate()) Instant.ofEpochSecond(pbSchedule.dueDate.seconds) else null
        val finishDate =
            if (pbSchedule.hasFinishDate()) Instant.ofEpochSecond(pbSchedule.finishDate.seconds) else null
        val goal = fromPbGoal(pbSchedule.goal)
        val repeatInfo = fromPbRepeatInfo(pbSchedule.repeatInfo)
        return Schedule(id, pbSchedule.title, startDate, dueDate, finishDate, goal, repeatInfo)
    }

    private fun fromPbGoal(pbGoal: PbGoal): Goal {
        if (pbGoal.hasQuantityGoal()) {
            return QuantityGoal(pbGoal.quantityGoal)
        }
        if (pbGoal.hasDurationGoal()) {
            val duration = Duration.ofSeconds(pbGoal.durationGoal.seconds)
            return DurationGoal(duration)
        }
        throw NotImplementedError()
    }

    private fun fromPbRepeatInfo(pbRepeatInfo: PbRepeatInfo): RepeatInfo {
        if (pbRepeatInfo.hasUnrepeated()) {
            return Unrepeated
        }
        if (pbRepeatInfo.hasPeriodicRepeat()) {
            return Periodic(pbRepeatInfo.periodicRepeat.periodDays, pbRepeatInfo.periodicRepeat.offsetDays)
        }
        throw NotImplementedError()
    }

    fun toPbSchedule(schedule: Schedule): PbSchedule {
        val builder = PbSchedule.newBuilder()
            .setId(schedule.id.value)
            .setTitle(schedule.title)
            .setStartDate(Timestamp.newBuilder().setSeconds(schedule.startDate.epochSecond))
            .setGoal(toPbGoal(schedule.goal))
            .setRepeatInfo(toPbRepeatInfo(schedule.repeatInfo))
        if (schedule.dueDate != null) {
            builder.setDueDate(Timestamp.newBuilder().setSeconds(schedule.dueDate.epochSecond))
        }
        if (schedule.finishDate != null) {
            builder.setFinishDate(Timestamp.newBuilder().setSeconds(schedule.finishDate.epochSecond))
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
}
