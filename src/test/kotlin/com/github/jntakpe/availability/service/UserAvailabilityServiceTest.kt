package com.github.jntakpe.availability.service

import com.github.jntakpe.availability.dao.UserAvailabilityDao
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.commons.test.expectStatusException
import io.grpc.Status
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import reactor.kotlin.test.test

@MicronautTest
internal class UserAvailabilityServiceTest(private val service: UserAvailabilityService, private val dao: UserAvailabilityDao) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `find by id should return user availability`(userAvailability: UserAvailability) {
        service.findById(userAvailability.id).test()
            .expectNext(userAvailability)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.TransientData::class)
    fun `find by id fail when user availability does not exists`(userAvailability: UserAvailability) {
        service.findById(userAvailability.id).test()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.TransientData::class)
    fun `declare availability should return created document`(userAvailability: UserAvailability) {
        service.declareAvailability(userAvailability).test()
            .consumeNextWith { assertThat(it).usingRecursiveComparison().ignoringFields("id").isEqualTo(userAvailability) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `declare availability should fail with already exists code when integrity constraint violated`(userAvailability: UserAvailability) {
        service.declareAvailability(UserAvailabilityDao.TransientData.mdoeRemote.copy(userId = userAvailability.userId)).test()
            .expectStatusException(Status.ALREADY_EXISTS)
            .verify()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.TransientData::class)
    fun `declare availability should fail when user id does not exists`(userAvailability: UserAvailability) {
        service.declareAvailability(userAvailability.copy(userId = ObjectId().toString())).test()
            .expectStatusException(Status.INVALID_ARGUMENT)
            .verify()
    }
}
