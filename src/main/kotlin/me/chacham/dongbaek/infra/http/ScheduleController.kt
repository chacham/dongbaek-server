package me.chacham.dongbaek.infra.http

import me.chacham.dongbaek.domain.schedule.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/schedules")
class ScheduleController(val scheduleRepository: ScheduleRepository) {
    @PostMapping
    fun create(@RequestBody request: CreateScheduleRequest): ScheduleId {
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
        return scheduleRepository.save(schedule)
    }

    @GetMapping("/{id}")
    fun find(@PathVariable("id") idValue: String): ResponseEntity<Schedule> {
        val id = ScheduleId(idValue)
        return ResponseEntity.ofNullable(scheduleRepository.find(id))
    }

    data class CreateScheduleRequest(val title: String)
}