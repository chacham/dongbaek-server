package me.chacham.dongbaek.infra.proto

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
}