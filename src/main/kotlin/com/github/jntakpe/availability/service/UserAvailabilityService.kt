package com.github.jntakpe.availability.service

import com.github.jntakpe.availability.client.UserClient
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.repository.UserAvailabilityRepository
import com.github.jntakpe.commons.context.CommonException
import com.github.jntakpe.commons.context.logger
import com.github.jntakpe.commons.mongo.insertError
import io.grpc.Status
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserAvailabilityService(private val repository: UserAvailabilityRepository, private val client: UserClient) {

    private val log = logger()

    fun declareAvailability(userAvailability: UserAvailability): Mono<UserAvailability> {
        return verifyUserIdExists(userAvailability.userId)
            .then(declareVerifiedUserAvailability(userAvailability))
    }

    private fun declareVerifiedUserAvailability(userAvailability: UserAvailability): Mono<UserAvailability> {
        return repository.create(userAvailability)
            .doOnSubscribe { log.debug("Creating {}", userAvailability) }
            .doOnNext { log.info("{} created", it) }
            .onErrorMap { it.insertError(userAvailability, log) }
    }

    private fun verifyUserIdExists(id: String): Mono<Void> {
        return client.findById(id)
            .doOnSubscribe { log.debug("Checking user id {} is valid", id) }
            .doOnNext { log.debug("User id {} is valid", id) }
            .onErrorMap { CommonException("User id $id does not exists", log::debug, Status.Code.INVALID_ARGUMENT, it) }
            .then()
    }
}
