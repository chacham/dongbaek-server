package me.chacham.dongbaek.infra.persistence

import me.chacham.dongbaek.domain.schedule.Schedule
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.domain.schedule.ScheduleRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryScheduleRepository : ScheduleRepository {
    private val counter = AtomicLong()
    private val scheduleMap = mutableMapOf<ScheduleId, Schedule>()

    override fun nextId(): ScheduleId {
        return ScheduleId(counter.getAndIncrement().toString())
    }

    override fun find(id: ScheduleId): Schedule? {
        return scheduleMap[id]
    }

    override fun save(schedule: Schedule): ScheduleId {
        scheduleMap[schedule.id] = schedule
        return schedule.id
    }
}