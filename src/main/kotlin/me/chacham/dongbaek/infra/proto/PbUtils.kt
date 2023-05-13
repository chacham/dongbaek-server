package me.chacham.dongbaek.infra.proto

import com.google.protobuf.Timestamp
import me.chacham.dongbaek.domain.schedule.*
import java.time.Duration
import java.time.Instant

object PbUtils {
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
