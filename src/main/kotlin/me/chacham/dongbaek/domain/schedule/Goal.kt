package me.chacham.dongbaek.domain.schedule

import java.time.Duration

sealed class Goal
data class QuantityGoal(val quantity: Int) : Goal()
data class DurationGoal(val duration: Duration) : Goal()
