package me.chacham.dongbaek.domain.schedule

import java.time.Instant

interface ProgressRepository {
    suspend fun save(progress: Progress): ProgressId
    suspend fun list(scheduleIds: List<ScheduleId>, instant: Instant): List<Progress>
}
