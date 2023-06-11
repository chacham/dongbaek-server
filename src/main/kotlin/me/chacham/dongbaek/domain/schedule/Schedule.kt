package me.chacham.dongbaek.domain.schedule

import java.time.Instant

data class Schedule(
    val id: ScheduleId,
    val title: String,
    val startInstant: Instant,
    val dueInstant: Instant?,
    val finishInstant: Instant?,
    val goal: Goal,
    val repeatInfo: RepeatInfo,
)
