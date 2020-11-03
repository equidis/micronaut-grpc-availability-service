package com.github.jntakpe.availability.endpoint

import com.github.jntakpe.availability.mapping.toEntity
import com.github.jntakpe.availability.mapping.toResponse
import com.github.jntakpe.availability.proto.ReactorUsersAvailabilityServiceGrpc.UsersAvailabilityServiceImplBase
import com.github.jntakpe.availability.proto.UsersAvailability
import com.github.jntakpe.availability.proto.UsersAvailability.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability.UserIdentification
import com.github.jntakpe.availability.proto.UsersAvailability.UsersAvailabilitiesResponse
import com.github.jntakpe.availability.proto.UsersAvailability.UsersAvailabilityResponse
import com.github.jntakpe.availability.service.UserAvailabilityService
import org.bson.types.ObjectId
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserAvailabilityEndpoint(private val service: UserAvailabilityService) : UsersAvailabilityServiceImplBase() {

    override fun findById(request: Mono<UsersAvailability.ByIdRequest>): Mono<UsersAvailabilityResponse> {
        return request
            .flatMap { service.findById(ObjectId(it.id)) }
            .map { it.toResponse() }
    }

    override fun findByUser(request: Mono<UserIdentification>): Mono<UsersAvailabilitiesResponse> {
        return request
            .flatMapMany { u -> if (u.username.isEmpty()) service.findByUserId(u.userId) else service.findByUsername(u.username) }
            .map { it.toResponse() }
            .reduce(UsersAvailabilitiesResponse.newBuilder(), { b, c -> b.addUsersAvailabilities(c) })
            .map { it.build() }
    }

    override fun declareAvailability(request: Mono<DeclareAvailabilityRequest>): Mono<UsersAvailabilityResponse> {
        return request
            .flatMap { service.declareAvailability(it.toEntity()) }
            .map { it.toResponse() }
    }
}
