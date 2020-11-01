package com.github.jntakpe.availability.mapping

import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType
import com.github.jntakpe.availability.proto.UsersAvailability.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability.WorkArrangement
import com.github.jntakpe.availability.proto.UsersAvailabilityResponse
import java.time.LocalDate

fun DeclareAvailabilityRequest.toEntity() = UserAvailability(userId, LocalDate.parse(day), WorkArrangementType.valueOf(arrangement.name))

fun UserAvailability.toResponse() = UsersAvailabilityResponse {
    val entity = this@toResponse
    userId = entity.userId
    day = entity.day.toString()
    arrangement = WorkArrangement.valueOf(entity.arrangement.name)
    id = entity.id.toString()
}
