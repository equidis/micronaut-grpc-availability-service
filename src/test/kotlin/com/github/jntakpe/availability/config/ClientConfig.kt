package com.github.jntakpe.availability.config

import com.github.jntakpe.availability.proto.UsersAvailabilityServiceGrpc
import com.github.jntakpe.availability.proto.UsersAvailabilityServiceGrpc.UsersAvailabilityServiceBlockingStub
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class ClientConfig {

    @Singleton
    fun serverStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): UsersAvailabilityServiceBlockingStub {
        return UsersAvailabilityServiceGrpc.newBlockingStub(channel)
    }
}
