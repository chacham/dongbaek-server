package me.chacham.dongbaek.domain.schedule

interface ScheduleRepository {
    fun nextId(): ScheduleId
    fun save(schedule: Schedule): ScheduleId
    fun find(id: ScheduleId): Schedule?
    fun list(): List<Schedule>
    fun delete(scheduleId: ScheduleId): ScheduleId
}
