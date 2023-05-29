package me.chacham.dongbaek.infra.grpc

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import me.chacham.dongbaek.domain.schedule.*
import me.chacham.dongbaek.infra.proto.PbUtils.toPbGoal
import me.chacham.dongbaek.infra.proto.PbUtils.toPbRepeatInfo
import me.chacham.dongbaek.infra.proto.PbUtils.toPbSchedule
import me.chacham.dongbaek.infra.proto.PbUtils.toPbTimestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class ScheduleGrpcServiceImplTest {
    @Test
    fun createScheduleTest(@MockK scheduleRepositoryMock: ScheduleRepository) {
        val impl = ScheduleGrpcServiceImpl(scheduleRepositoryMock)

        val s = Schedule(
            ScheduleId("testScheduleId"),
            "testTitle",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            null,
            QuantityGoal(50),
            Periodic(10, 3)
        )
        coEvery { scheduleRepositoryMock.nextId() } returns s.id
        coEvery { scheduleRepositoryMock.save(s) } returns s.id

        val request = createScheduleRequest {
            title = s.title
            startTimestamp = s.startInstant.toPbTimestamp()
            dueTimestamp = s.dueInstant!!.toPbTimestamp()
            goal = s.goal.toPbGoal()
            repeatInfo = s.repeatInfo.toPbRepeatInfo()
        }
        val response = runBlocking { impl.createSchedule(request) }

        coVerify { scheduleRepositoryMock.nextId() }
        coVerify { scheduleRepositoryMock.save(s) }
        assertEquals(createScheduleResponse { scheduleId = s.id.value }, response)
    }

    @Test
    fun getScheduleTest(@MockK scheduleRepositoryMock: ScheduleRepository) {
        val impl = ScheduleGrpcServiceImpl(scheduleRepositoryMock)

        val id = ScheduleId("testScheduleId")
        val s = Schedule(
            id,
            "testTitle",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            null,
            QuantityGoal(50),
            Periodic(10, 3)
        )
        coEvery { scheduleRepositoryMock.find(id) } returns s

        val request = getScheduleRequest { scheduleId = id.value }
        val response = runBlocking { impl.getSchedule(request) }

        coVerify { scheduleRepositoryMock.find(id) }
        assertEquals(getScheduleResponse { schedule = s.toPbSchedule() }, response)
    }

    @Test
    fun getSchedulesTest(@MockK scheduleRepositoryMock: ScheduleRepository) {
        val impl = ScheduleGrpcServiceImpl(scheduleRepositoryMock)

        val id1 = ScheduleId("testScheduleId1")
        val s1 = Schedule(
            id1,
            "testTitle1",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            null,
            QuantityGoal(50),
            Periodic(10, 3)
        )
        val id2 = ScheduleId("testScheduleId1")
        val s2 = Schedule(
            id2,
            "testTitle2",
            Instant.now(),
            Instant.now().plusSeconds(7200),
            null,
            DurationGoal(Duration.ofSeconds(3600)),
            Unrepeated
        )
        coEvery { scheduleRepositoryMock.list() } returns listOf(s1, s2)

        val request = getSchedulesRequest { }
        val response = runBlocking { impl.getSchedules(request) }

        coVerify { scheduleRepositoryMock.list() }
        assertEquals(getSchedulesResponse { schedules.addAll(listOf(s1.toPbSchedule(), s2.toPbSchedule())) }, response)
    }

    @Test
    fun replaceScheduleTest(@MockK scheduleRepositoryMock: ScheduleRepository) {
        val impl = ScheduleGrpcServiceImpl(scheduleRepositoryMock)

        val id = ScheduleId("testScheduleId")
        val s = Schedule(
            id,
            "testTitle",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Instant.now().plusSeconds(1800),
            QuantityGoal(10),
            Periodic(7, 3)
        )
        coEvery { scheduleRepositoryMock.save(s) } returns id

        val request = replaceScheduleRequest { schedule = s.toPbSchedule() }
        val response = runBlocking { impl.replaceSchedule(request) }

        coVerify { scheduleRepositoryMock.save(s) }
        assertEquals(replaceScheduleResponse { scheduleId = id.value }, response)
    }

    @Test
    fun deleteScheduleTest(@MockK scheduleRepositoryMock: ScheduleRepository) {
        val impl = ScheduleGrpcServiceImpl(scheduleRepositoryMock)

        val id = ScheduleId("testScheduleId")
        coEvery { scheduleRepositoryMock.delete(id) } returns id

        val request = deleteScheduleRequest { scheduleId = id.value }
        val response = runBlocking { impl.deleteSchedule(request) }

        coVerify { scheduleRepositoryMock.delete(id) }
        assertEquals(deleteScheduleResponse { }, response)
    }
}
