package me.chacham.dongbaek.domain.progress

import me.chacham.dongbaek.domain.schedule.ScheduleId
import java.time.Instant

interface ProgressRepository {
    suspend fun save(progress: Progress): ProgressId
    suspend fun list(scheduleIds: List<ScheduleId>, instant: Instant): List<Progress>
}
