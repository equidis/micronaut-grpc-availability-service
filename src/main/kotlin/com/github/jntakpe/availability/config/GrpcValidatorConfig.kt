package com.github.jntakpe.availability.config

import com.github.jntakpe.availability.proto.UsersAvailability.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability.UsersAvailabilityResponse
import com.github.jntakpe.availability.proto.UsersAvailabilityValidator.DeclareAvailabilityRequestValidator
import com.github.jntakpe.availability.proto.UsersAvailabilityValidator.UsersAvailabilityResponseValidator
import com.github.jntakpe.commons.grpc.GrpcValidator
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcValidatorConfig {

    @Singleton
    fun grpcValidators(): Iterable<GrpcValidator<*>> {
        return listOf(
            GrpcValidator(DeclareAvailabilityRequest::class, DeclareAvailabilityRequestValidator()),
            GrpcValidator(UsersAvailabilityResponse::class, UsersAvailabilityResponseValidator()),
        )
    }
}
