package com.github.jntakpe.availability.endpoint

import com.github.jntakpe.availability.mapping.toEntity
import com.github.jntakpe.availability.mapping.toResponse
import com.github.jntakpe.availability.proto.ReactorUsersAvailabilityServiceGrpc.UsersAvailabilityServiceImplBase
import com.github.jntakpe.availability.proto.UsersAvailability.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability.UsersAvailabilityResponse
import com.github.jntakpe.availability.service.UserAvailabilityService
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserAvailabilityEndpoint(private val service: UserAvailabilityService) : UsersAvailabilityServiceImplBase() {

    override fun declareAvailability(request: Mono<DeclareAvailabilityRequest>): Mono<UsersAvailabilityResponse> {
        return request
            .flatMap { service.declareAvailability(it.toEntity()) }
            .map { it.toResponse() }
    }
}
