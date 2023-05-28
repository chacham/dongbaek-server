package me.chacham.dongbaek.infra.grpc

import me.chacham.dongbaek.domain.schedule.*
import me.chacham.dongbaek.infra.proto.PbUtils
import me.chacham.dongbaek.infra.proto.PbUtils.toSchedule
import net.devh.boot.grpc.server.service.GrpcService
import java.time.Instant

@GrpcService
class ScheduleGrpcServiceImpl(val scheduleRepository: ScheduleRepository) :
    ScheduleServiceGrpcKt.ScheduleServiceCoroutineImplBase() {
    override suspend fun createSchedule(request: CreateScheduleRequest): CreateScheduleResponse {
        val id = scheduleRepository.nextId()
        val schedule = Schedule(
            id,
            request.title,
            Instant.now(),
            Instant.now().plusSeconds(3600 * 24 * 7),
            null,
            QuantityGoal(10),
            Unrepeated
        )
        scheduleRepository.save(schedule)
        return CreateScheduleResponse.newBuilder().setScheduleId(id.value).build()
    }

    override suspend fun getSchedule(request: GetScheduleRequest): GetScheduleResponse {
        val id = ScheduleId(request.scheduleId)
        return scheduleRepository.find(id)
            ?.let { PbUtils.toPbSchedule(it) }
            ?.let { GetScheduleResponse.newBuilder().setSchedule(it).build() }!! // TODO: Add exception
    }

    override suspend fun getSchedules(request: GetSchedulesRequest): GetSchedulesResponse {
        val schedules = scheduleRepository.list()
        val pbSchedules = schedules.map { PbUtils.toPbSchedule(it) }
        return GetSchedulesResponse.newBuilder().addAllSchedules(pbSchedules).build()
    }

    override suspend fun replaceSchedule(request: ReplaceScheduleRequest): ReplaceScheduleResponse {
        val schedule = request.schedule.toSchedule()
        scheduleRepository.save(schedule)
        return ReplaceScheduleResponse.newBuilder()
            .setScheduleId(schedule.id.value)
            .build()
    }

    override suspend fun deleteSchedule(request: DeleteScheduleRequest): DeleteScheduleResponse {
        val scheduleId = ScheduleId(request.scheduleId)
        scheduleRepository.delete(scheduleId)
        return DeleteScheduleResponse.getDefaultInstance()
    }
}
