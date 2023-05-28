package me.chacham.dongbaek.domain.schedule

import java.time.Duration
import java.time.Instant

data class ScheduleId(val value: String)

data class Schedule(
    val id: ScheduleId,
    val title: String,
    val startInstant: Instant,
    val dueInstant: Instant?,
    val finishInstant: Instant?,
    val goal: Goal,
    val repeatInfo: RepeatInfo,
)

sealed class Goal
data class QuantityGoal(val quantity: Int) : Goal()
data class DurationGoal(val duration: Duration) : Goal()

sealed class RepeatInfo
object Unrepeated : RepeatInfo()
data class Periodic(val periodDays: Int, val offsetDays: Int) : RepeatInfo()
