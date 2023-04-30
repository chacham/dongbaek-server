package me.chacham.dongbaek.domain.schedule

interface ScheduleRepository {
    fun nextId(): ScheduleId
    fun find(id: ScheduleId): Schedule?
    fun save(schedule: Schedule): ScheduleId
}
