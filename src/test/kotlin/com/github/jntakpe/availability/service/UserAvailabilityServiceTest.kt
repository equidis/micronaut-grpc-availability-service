package com.github.jntakpe.availability.service

import com.github.jntakpe.availability.client.UserClient
import com.github.jntakpe.availability.common.MockUserService.Companion.JDOE_USERNAME
import com.github.jntakpe.availability.common.MockUserService.Companion.MDOE_USERNAME
import com.github.jntakpe.availability.dao.UserAvailabilityDao
import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData
import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.JDOE_ID
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData.MDOE_ID
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.repository.UserAvailabilityRepository
import com.github.jntakpe.commons.cache.RedisReactiveCache
import com.github.jntakpe.commons.test.expectStatusException
import io.grpc.Status
import io.micronaut.configuration.lettuce.cache.RedisCache
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.kotlin.test.test
import javax.inject.Named

@MicronautTest
internal class UserAvailabilityServiceTest(
    private val service: UserAvailabilityService,
    private val dao: UserAvailabilityDao,
    private val userAvailabilityRepository: UserAvailabilityRepository,
    private val client: UserClient,
    @Named("users-availability") private val usersAvailabilityCache: RedisReactiveCache,
    @Named("users-availability") private val rawCache: RedisCache,
) {

    @BeforeEach
    fun setup() {
        dao.init()
        rawCache.invalidateAll()
    }

    @ParameterizedTest
    @ArgumentsSource(PersistedData::class)
    fun `find by id should return user availability`(userAvailability: UserAvailability) {
        service.findById(userAvailability.id).test()
            .expectNext(userAvailability)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(PersistedData::class)
    fun `find by id should call repository since cache miss`(userAvailability: UserAvailability) {
        val repoSpy = spyk(userAvailabilityRepository)
        UserAvailabilityService(repoSpy, client, usersAvailabilityCache).findById(userAvailability.id).test()
            .expectNext(userAvailability)
            .then {
                verify { repoSpy.findById(userAvailability.id) }
                confirmVerified(repoSpy)
                assertThat(rawCache.get(userAvailability.id, UserAvailability::class.java)).isPresent.get().isEqualTo(userAvailability)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(PersistedData::class)
    fun `find by id should not call repository since retrieved from cache`(userAvailability: UserAvailability) {
        rawCache.put(userAvailability.id, userAvailability)
        val repoSpy = spyk(userAvailabilityRepository)
        UserAvailabilityService(repoSpy, client, usersAvailabilityCache).findById(userAvailability.id).test()
            .expectNext(userAvailability)
            .then {
                verify { repoSpy.findById(userAvailability.id) wasNot Called }
                confirmVerified(repoSpy)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `find by id fail when user availability does not exists`(userAvailability: UserAvailability) {
        service.findById(userAvailability.id).test()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }

    @Test
    fun `find by user id should return multiple availabilities`() {
        service.findByUserId(JDOE_ID).test()
            .recordWith { ArrayList() }
            .expectNextCount(PersistedData.data().size.toLong())
            .consumeRecordedWith { l -> assertThat(l.map { it.userId }).containsOnly(JDOE_ID) }
            .verifyComplete()
    }

    @Test
    fun `find by user id should call repository since cache miss`() {
        val repoSpy = spyk(userAvailabilityRepository)
        UserAvailabilityService(repoSpy, client, usersAvailabilityCache).findByUserId(JDOE_ID).test()
            .recordWith { ArrayList() }
            .expectNextCount(PersistedData.data().size.toLong())
            .consumeRecordedWith { l -> assertThat(l.map { it.userId }).containsOnly(JDOE_ID) }
            .then {
                verify { repoSpy.findByUserId(JDOE_ID) }
                confirmVerified(repoSpy)
                assertThat(rawCache.get(JDOE_ID, List::class.java)).isPresent.get().isEqualTo(PersistedData.data())
            }
            .verifyComplete()
    }

    @Test
    fun `find by user id should not call repository since retrieved from cache`() {
        rawCache.put(JDOE_ID, PersistedData.data())
        val repoSpy = spyk(userAvailabilityRepository)
        UserAvailabilityService(repoSpy, client, usersAvailabilityCache).findByUserId(JDOE_ID).test()
            .recordWith { ArrayList() }
            .expectNextCount(PersistedData.data().size.toLong())
            .then {
                verify { repoSpy.findByUserId(JDOE_ID) wasNot Called }
                confirmVerified(repoSpy)
            }
            .verifyComplete()
    }

    @Test
    fun `find by user id return empty when user id does not exists`() {
        service.findByUserId(MDOE_ID).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `find by user username should return multiple availabilities`() {
        service.findByUsername(JDOE_USERNAME).test()
            .recordWith { ArrayList() }
            .expectNextCount(PersistedData.data().size.toLong())
            .consumeRecordedWith { l -> assertThat(l.map { it.userId }).containsOnly(JDOE_ID) }
            .verifyComplete()
    }

    @Test
    fun `find by username return empty when username does not exists in database`() {
        service.findByUsername(MDOE_USERNAME).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "", "*"])
    fun `find by username return empty when username does not exists in client service`(username: String) {
        service.findByUsername(username).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `declare availability should return created document`(userAvailability: UserAvailability) {
        service.declareAvailability(userAvailability).test()
            .consumeNextWith { assertThat(it).usingRecursiveComparison().ignoringFields("id").isEqualTo(userAvailability) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `declare availability should put item in cache`(userAvailability: UserAvailability) {
        val retrieveWithId = { rawCache.get(userAvailability.id, UserAvailability::class.java) }
        val retrieveWithUserId = { rawCache.get(userAvailability.userId, UserAvailability::class.java) }
        assertThat(retrieveWithId()).isEmpty
        assertThat(retrieveWithUserId()).isEmpty
        service.declareAvailability(userAvailability).test()
            .expectNext(userAvailability)
            .then {
                assertThat(retrieveWithId()).isPresent.get().isEqualTo(userAvailability)
                assertThat(retrieveWithUserId()).isPresent.get().isEqualTo(userAvailability)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(PersistedData::class)
    fun `declare availability should fail with already exists code when integrity constraint violated`(userAvailability: UserAvailability) {
        service.declareAvailability(TransientData.mdoeRemote.copy(userId = userAvailability.userId)).test()
            .expectStatusException(Status.ALREADY_EXISTS)
            .verify()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `declare availability should fail when user id does not exists`(userAvailability: UserAvailability) {
        service.declareAvailability(userAvailability.copy(userId = ObjectId().toString())).test()
            .expectStatusException(Status.INVALID_ARGUMENT)
            .verify()
    }
}
