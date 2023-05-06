package me.chacham.dongbaek.infra.grpc

import me.chacham.dongbaek.domain.schedule.*
import me.chacham.dongbaek.infra.proto.PbConverter
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
            ?.let { PbConverter.toPbSchedule(it) }
            ?.let { GetScheduleResponse.newBuilder().setSchedule(it).build() }!! // TODO: Add exception
    }

    override suspend fun listSchedule(request: ListScheduleRequest): ListScheduleResponse {
        val schedules = scheduleRepository.list()
        val pbSchedules = schedules.map { PbConverter.toPbSchedule(it) }
        return ListScheduleResponse.newBuilder().addAllSchedules(pbSchedules).build()
    }
}
