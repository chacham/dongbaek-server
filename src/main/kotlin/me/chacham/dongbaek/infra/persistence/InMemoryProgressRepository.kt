package me.chacham.dongbaek.infra.persistence

import me.chacham.dongbaek.domain.schedule.Progress
import me.chacham.dongbaek.domain.schedule.ProgressId
import me.chacham.dongbaek.domain.schedule.ProgressRepository
import me.chacham.dongbaek.domain.schedule.ScheduleId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class InMemoryProgressRepository : ProgressRepository {
    private val progressMap = mutableMapOf<ProgressId, Progress>()

    override fun save(progress: Progress): ProgressId {
        val id = progress.getId()
        progressMap[id] = progress
        return id
    }

    override fun list(scheduleIds: List<ScheduleId>, instant: Instant): List<Progress> {
        return progressMap.values.filter {
            it.scheduleId in scheduleIds
                    && !it.startInstant.isAfter(instant)
                    && (it.endInstant?.isAfter(instant) ?: true)
        }
    }
}
