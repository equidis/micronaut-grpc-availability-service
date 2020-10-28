package com.github.jntakpe.availability.config

import com.github.jntakpe.commons.grpc.GrpcValidator
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcValidatorConfig {

    @Singleton
    fun grpcValidators(): Iterable<GrpcValidator<*>> {
        return listOf()
    }
}
