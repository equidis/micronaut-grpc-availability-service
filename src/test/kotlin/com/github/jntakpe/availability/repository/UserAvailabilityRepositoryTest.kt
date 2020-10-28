package com.github.jntakpe.availability.repository

import com.github.jntakpe.availability.dao.UserAvailabilityDao
import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.JDOE_ID
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType.REMOTE
import com.mongodb.MongoWriteException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.kotlin.test.test
import java.time.LocalDate

@MicronautTest
class UserAvailabilityRepositoryTest(private val repository: UserAvailabilityRepository, private val dao: UserAvailabilityDao) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `find by id should find one`(availability: UserAvailability) {
        val id = availability.id
        repository.findById(id).test()
            .consumeNextWith { assertThat(it.id).isEqualTo(id) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `find by id should return empty`(availability: UserAvailability) {
        repository.findById(availability.id).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `find by user id should find multiple`(availability: UserAvailability) {
        val userId = availability.userId
        repository.findByUserId(userId).test()
            .recordWith { ArrayList() }
            .expectNextCount(UserAvailabilityDao.PersistedData.data().size.toLong())
            .consumeRecordedWith { l -> assertThat(l.map { it.userId }).containsOnly(JDOE_ID) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "", "*"])
    fun `find by user id should return empty`(userId: String) {
        repository.findByUserId(userId).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `create should add document`(availability: UserAvailability) {
        val initSize = dao.count()
        repository.create(availability).test()
            .consumeNextWith {
                assertThat(it).isEqualTo(availability)
                assertThat(dao.count()).isNotZero.isEqualTo(initSize + 1)
            }
            .verifyComplete()
    }

    @Test
    fun `create should fail when userid and date already exists`() {
        val initSize = dao.count()
        repository.create(UserAvailability(JDOE_ID, LocalDate.of(2020, 10, 15), REMOTE, ObjectId())).test()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(MongoWriteException::class.java)
                assertThat(dao.count()).isEqualTo(initSize)
            }
            .verify()
    }
}
