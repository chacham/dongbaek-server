package me.chacham.dongbaek.domain.schedule

import java.time.Duration

sealed class RepeatInfo
object Unrepeated : RepeatInfo()
data class Periodic(val periodDuration: Duration, val offsetDuration: Duration) : RepeatInfo()
