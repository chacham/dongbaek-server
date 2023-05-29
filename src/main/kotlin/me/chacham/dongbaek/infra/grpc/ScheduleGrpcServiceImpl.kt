package me.chacham.dongbaek.infra.grpc

import me.chacham.dongbaek.domain.schedule.Schedule
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.domain.schedule.ScheduleRepository
import me.chacham.dongbaek.infra.proto.PbUtils.toGoal
import me.chacham.dongbaek.infra.proto.PbUtils.toInstant
import me.chacham.dongbaek.infra.proto.PbUtils.toPbSchedule
import me.chacham.dongbaek.infra.proto.PbUtils.toRepeatInfo
import me.chacham.dongbaek.infra.proto.PbUtils.toSchedule
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ScheduleGrpcServiceImpl(val scheduleRepository: ScheduleRepository) :
    ScheduleServiceGrpcKt.ScheduleServiceCoroutineImplBase() {
    override suspend fun createSchedule(request: CreateScheduleRequest): CreateScheduleResponse {
        val id = scheduleRepository.nextId()
        val schedule = Schedule(
            id,
            request.title,
            request.startTimestamp.toInstant(),
            request.dueTimestampOrNull?.toInstant(),
            null,
            request.goal.toGoal(),
            request.repeatInfo.toRepeatInfo()
        )
        scheduleRepository.save(schedule)
        return createScheduleResponse { scheduleId = id.value }
    }

    override suspend fun getSchedule(request: GetScheduleRequest): GetScheduleResponse {
        val id = ScheduleId(request.scheduleId)
        return scheduleRepository.find(id)
            ?.toPbSchedule()
            ?.let { getScheduleResponse { schedule = it } }!! // TODO: Add exception
    }

    override suspend fun getSchedules(request: GetSchedulesRequest): GetSchedulesResponse {
        val pbSchedules = scheduleRepository.list().map { it.toPbSchedule() }
        return getSchedulesResponse { schedules.addAll(pbSchedules) }
    }

    override suspend fun replaceSchedule(request: ReplaceScheduleRequest): ReplaceScheduleResponse {
        val schedule = request.schedule.toSchedule()
        scheduleRepository.save(schedule)
        return replaceScheduleResponse { scheduleId = schedule.id.value }
    }

    override suspend fun deleteSchedule(request: DeleteScheduleRequest): DeleteScheduleResponse {
        val scheduleId = ScheduleId(request.scheduleId)
        scheduleRepository.delete(scheduleId)
        return deleteScheduleResponse { }
    }
}
