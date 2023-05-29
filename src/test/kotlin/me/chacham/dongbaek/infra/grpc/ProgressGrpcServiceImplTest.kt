package me.chacham.dongbaek.infra.grpc

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import me.chacham.dongbaek.domain.schedule.DurationProgress
import me.chacham.dongbaek.domain.schedule.ProgressRepository
import me.chacham.dongbaek.domain.schedule.QuantityProgress
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.infra.proto.PbUtils.toPbProgress
import me.chacham.dongbaek.infra.proto.PbUtils.toPbTimestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class ProgressGrpcServiceImplTest {
    @Test
    fun getProgressesTest(@MockK progressRepositoryMock: ProgressRepository) {
        val impl = ProgressGrpcServiceImpl(progressRepositoryMock)

        val sId1 = ScheduleId("sid1")
        val sId2 = ScheduleId("sid2")
        val progress1 = QuantityProgress(sId1, Instant.now())
        val progress2 = DurationProgress(sId2, Instant.now())
        every { progressRepositoryMock.list(listOf(sId1, sId2), any()) } returns listOf(progress1, progress2)

        val time = Instant.now()
        val request = getProgressesRequest {
            scheduleIds.addAll(listOf(sId1.value, sId2.value))
            timestamp = time.toPbTimestamp()
        }
        val response = runBlocking { impl.getProgresses(request) }

        verify { progressRepositoryMock.list(listOf(sId1, sId2), time) }
        assertEquals(getProgressesResponse {
            progresses.addAll(listOf(progress1.toPbProgress(), progress2.toPbProgress()))
        }, response)
    }

    @Test
    fun replaceProgressTest(@MockK progressRepositoryMock: ProgressRepository) {
        val impl = ProgressGrpcServiceImpl(progressRepositoryMock)

        val sId = ScheduleId("sid")
        val p = QuantityProgress(sId, Instant.now())
        every { progressRepositoryMock.save(p) } returns p.getId()

        val request = replaceProgressRequest {
            progress = p.toPbProgress()
        }
        val response = runBlocking { impl.replaceProgress(request) }

        verify { progressRepositoryMock.save(p) }
        assertEquals(replaceProgressResponse {
            scheduleId = p.scheduleId.value
            startTimestamp = p.startInstant.toPbTimestamp()
        }, response)
    }
}
