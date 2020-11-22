package com.github.jntakpe.availability.service

import com.github.jntakpe.availability.client.UserClient
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.repository.UserAvailabilityRepository
import com.github.jntakpe.commons.cache.RedisReactiveCache
import com.github.jntakpe.commons.context.CommonException
import com.github.jntakpe.commons.context.logger
import com.github.jntakpe.commons.mongo.insertError
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserAvailabilityService(
    private val repository: UserAvailabilityRepository,
    private val client: UserClient,
    @Named("users-availability") private val usersAvailabilityCache: RedisReactiveCache,
) {

    private val log = logger()

    fun findById(id: ObjectId): Mono<UserAvailability> {
        return usersAvailabilityCache.orPutOnCacheMiss(id) { repository.findById(id) }
            .doOnSubscribe { log.debug("Searching user availability by id {}", id) }
            .doOnNext { log.debug("{} retrieved using it's id", it) }
            .switchIfEmpty(missingIdError(id).toMono())
    }

    fun findByUserId(userId: String): Flux<UserAvailability> {
        return usersAvailabilityCache.orPutOnCacheMiss(userId) { repository.findByUserId(userId).collectList() }
            .doOnSubscribe { log.debug("Searching user availability by user id {}", userId) }
            .doOnNext { log.debug("{} availabilities retrieved using user id {}", it.size, userId) }
            .flatMapMany { it.toFlux() }
    }

    fun findByUsername(username: String): Flux<UserAvailability> {
        return client.findByUsername(username)
            .onErrorResume({ it.isNotFoundError() }) { Mono.empty() }
            .doOnSubscribe { log.debug("Searching user availability identifier using it's username {}", username) }
            .doOnNext { log.debug("{} retrieved using it's username", it) }
            .flatMapMany { findByUserId(it.id) }
    }

    fun declareAvailability(userAvailability: UserAvailability): Mono<UserAvailability> {
        return verifyUserIdExists(userAvailability.userId)
            .then(declareVerifiedUserAvailability(userAvailability))
    }

    private fun declareVerifiedUserAvailability(userAvailability: UserAvailability): Mono<UserAvailability> {
        return repository.create(userAvailability)
            .doOnSubscribe { log.debug("Creating {}", userAvailability) }
            .doOnNext { log.info("{} created", it) }
            .onErrorMap { it.insertError(userAvailability, log) }
            .doOnNext {
                usersAvailabilityCache.putAndForget(userAvailability.id, userAvailability)
                usersAvailabilityCache.putAndForget(userAvailability.userId, userAvailability)
            }
    }

    private fun verifyUserIdExists(id: String): Mono<Void> {
        return client.findById(id)
            .doOnSubscribe { log.debug("Checking user id {} is valid", id) }
            .doOnNext { log.debug("User id {} is valid", id) }
            .onErrorMap { CommonException("User id $id does not exists", log::debug, Status.Code.INVALID_ARGUMENT, it) }
            .then()
    }

    private fun missingIdError(id: ObjectId): CommonException {
        return CommonException("No user availability found for id $id", log::debug, Status.Code.NOT_FOUND)
    }

    private fun Throwable.isNotFoundError() = (this as? StatusRuntimeException)?.status == Status.NOT_FOUND
}
