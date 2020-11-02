package com.github.jntakpe.availability.common

import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.JDOE_ID
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData.MDOE_ID
import com.github.jntakpe.users.proto.UserResponse
import com.github.jntakpe.users.proto.Users
import com.github.jntakpe.users.proto.UsersServiceGrpc
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import java.util.function.Supplier
import javax.inject.Singleton

@Singleton
class MockUserService : Supplier<BindableService> {

    companion object {

        const val JDOE_USERNAME = "jdoe"
        const val MDOE_USERNAME = "mmoe"
    }

    override fun get() = object : UsersServiceGrpc.UsersServiceImplBase() {
        override fun findById(request: Users.ByIdRequest, responseObserver: StreamObserver<Users.UserResponse>) {
            when (request.id) {
                JDOE_ID -> jdoeResponse()
                MDOE_ID -> mdoeResponse()
                else -> null
            }.commit(responseObserver)
        }

        override fun findByUsername(request: Users.UsersByUsernameRequest, responseObserver: StreamObserver<Users.UserResponse>) {
            when (request.username) {
                JDOE_USERNAME -> jdoeResponse()
                MDOE_USERNAME -> mdoeResponse()
                else -> null
            }.commit(responseObserver)
        }

        private fun jdoeResponse(): Users.UserResponse {
            return UserResponse {
                id = JDOE_ID
                username = JDOE_USERNAME
                email = "jdoe@mail.com"
            }
        }

        private fun mdoeResponse(): Users.UserResponse {
            return UserResponse {
                id = MDOE_ID
                username = MDOE_USERNAME
                email = "mmoe@mail.com"
            }
        }

        private fun Users.UserResponse?.commit(observer: StreamObserver<Users.UserResponse>) {
            with(observer) {
                this@commit?.also {
                    onNext(it)
                    onCompleted()
                } ?: onError(StatusRuntimeException(Status.NOT_FOUND))
            }
        }
    }
}
