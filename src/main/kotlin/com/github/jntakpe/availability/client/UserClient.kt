package com.github.jntakpe.availability.client

import com.github.jntakpe.users.proto.ByIdRequest
import com.github.jntakpe.users.proto.ReactorUsersServiceGrpc
import com.github.jntakpe.users.proto.Users.UserResponse
import com.github.jntakpe.users.proto.UsersByUsernameRequest
import io.grpc.ManagedChannel
import io.micronaut.grpc.annotation.GrpcChannel
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserClient(@GrpcChannel("users") channel: ManagedChannel) {

    private val stub = ReactorUsersServiceGrpc.newReactorStub(channel)

    fun findById(id: String): Mono<UserResponse> = stub.findById(ByIdRequest { this.id = id })

    fun findByUsername(username: String): Mono<UserResponse> = stub.findByUsername(UsersByUsernameRequest { this.username = username })
}
