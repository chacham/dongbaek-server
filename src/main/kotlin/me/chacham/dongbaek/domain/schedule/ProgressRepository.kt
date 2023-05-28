package me.chacham.dongbaek.domain.schedule

import java.time.Instant

interface ProgressRepository {
    fun save(progress: Progress): ProgressId
    fun list(scheduleIds: List<ScheduleId>, instant: Instant): List<Progress>
}
