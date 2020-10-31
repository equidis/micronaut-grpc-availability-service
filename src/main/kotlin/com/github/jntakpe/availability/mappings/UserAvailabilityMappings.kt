package com.github.jntakpe.availability.mappings

import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType
import com.github.jntakpe.availability.proto.UsersAvailability.DeclareAvailabilityRequest
import java.time.LocalDate

fun DeclareAvailabilityRequest.toEntity() = UserAvailability(userId, LocalDate.parse(day), WorkArrangementType.valueOf(arrangement.name))

