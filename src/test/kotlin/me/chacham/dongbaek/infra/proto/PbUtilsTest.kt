package me.chacham.dongbaek.infra.proto

import me.chacham.dongbaek.domain.progress.DurationProgress
import me.chacham.dongbaek.domain.progress.QuantityProgress
import me.chacham.dongbaek.domain.schedule.QuantityGoal
import me.chacham.dongbaek.domain.schedule.Schedule
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.domain.schedule.Unrepeated
import me.chacham.dongbaek.infra.proto.PbUtils.toPbProgress
import me.chacham.dongbaek.infra.proto.PbUtils.toPbSchedule
import me.chacham.dongbaek.infra.proto.PbUtils.toProgress
import me.chacham.dongbaek.infra.proto.PbUtils.toSchedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class PbUtilsTest {
    @Test
    fun schedulePbConversionTest() {
        val schedule = Schedule(
            ScheduleId("testId"),
            "testTitle",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            null,
            QuantityGoal(10),
            Unrepeated
        )

        assertEquals(schedule, schedule.toPbSchedule().toSchedule())
    }

    @Test
    fun progressPbConversionTest() {
        val progress1 = QuantityProgress(ScheduleId("testScheduleId1"), Instant.now(), null, 10)
        assertEquals(progress1, progress1.toPbProgress().toProgress())

        val progress2 = DurationProgress(
            ScheduleId("testScheduleId2"),
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Duration.ofSeconds(60),
            null
        )
        assertEquals(progress2, progress2.toPbProgress().toProgress())
    }
}
