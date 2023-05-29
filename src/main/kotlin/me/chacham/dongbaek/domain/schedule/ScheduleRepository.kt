package me.chacham.dongbaek.domain.schedule

interface ScheduleRepository {
    suspend fun nextId(): ScheduleId
    suspend fun save(schedule: Schedule): ScheduleId
    suspend fun find(id: ScheduleId): Schedule?
    suspend fun list(): List<Schedule>
    suspend fun delete(scheduleId: ScheduleId): ScheduleId
}
