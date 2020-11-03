package com.github.jntakpe.availability.endpoint

import com.github.jntakpe.availability.common.MockUserService.Companion.JDOE_USERNAME
import com.github.jntakpe.availability.common.MockUserService.Companion.MDOE_USERNAME
import com.github.jntakpe.availability.dao.UserAvailabilityDao
import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.JDOE_ID
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData.MDOE_ID
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.proto.ByIdRequest
import com.github.jntakpe.availability.proto.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UserIdentification
import com.github.jntakpe.availability.proto.UsersAvailability
import com.github.jntakpe.availability.proto.UsersAvailabilityServiceGrpc.UsersAvailabilityServiceBlockingStub
import com.github.jntakpe.commons.test.assertStatusException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

@MicronautTest
internal class UserAvailabilityEndpointTest(private val dao: UserAvailabilityDao, private val stub: UsersAvailabilityServiceBlockingStub) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `find by id should return ok response`(userAvailability: UserAvailability) {
        val request = ByIdRequest { id = userAvailability.id.toString() }
        val response = stub.findById(request)
        assertThat(response.id).isNotEmpty.isEqualTo(userAvailability.id.toString())
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.TransientData::class)
    fun `find by id should fail when user does not exist`(userAvailability: UserAvailability) {
        val request = ByIdRequest { id = userAvailability.id.toString() }
        val error = catchThrowable { stub.findById(request) }
        assertThat(error).isInstanceOf(StatusRuntimeException::class.java)
        error as StatusRuntimeException
        assertThat(error.status.code).isEqualTo(Status.NOT_FOUND.code)
    }

    @Test
    fun `find by user should return ok response using user id`() {
        val request = UserIdentification { userId = JDOE_ID }
        val response = stub.findByUser(request)
        assertThat(response.usersAvailabilitiesList.map { it.userId }).isNotEmpty.containsOnly(JDOE_ID)
    }

    @Test
    fun `find by user should return empty when user id does not exist`() {
        val request = UserIdentification { userId = MDOE_ID }
        val response = stub.findByUser(request)
        assertThat(response.usersAvailabilitiesList.map { it.userId }).isEmpty()
    }

    @Test
    fun `find by user should return ok response using username`() {
        val request = UserIdentification { username = JDOE_USERNAME }
        val response = stub.findByUser(request)
        assertThat(response.usersAvailabilitiesList.map { it.userId }).isNotEmpty.containsOnly(JDOE_ID)
    }

    @Test
    fun `find by user should return empty when username does not exist`() {
        val request = UserIdentification { username = MDOE_USERNAME }
        val response = stub.findByUser(request)
        assertThat(response.usersAvailabilitiesList.map { it.userId }).isEmpty()
    }

    @Test
    fun `find by user should not set both user id and username`() {
        val request = UserIdentification { username = JDOE_USERNAME; userId = JDOE_ID }
        assertThat(request.username.isNotEmpty() && request.userId.isNotEmpty()).isFalse
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.TransientData::class)
    fun `declare availability should return ok response`(userAvailability: UserAvailability) {
        val initSize = dao.count()
        val response = stub.declareAvailability(userAvailabilityRequestMapping(userAvailability))
        assertThat(response.id).isNotEmpty
        assertThat(dao.count()).isEqualTo(initSize + 1)
    }

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `declare availability should fail when user availability already exists`(userAvailability: UserAvailability) {
        val initSize = dao.count()
        catchThrowable { stub.declareAvailability(userAvailabilityRequestMapping(userAvailability)) }
            .assertStatusException(Status.ALREADY_EXISTS)
        assertThat(dao.count()).isEqualTo(initSize)
    }

    @Test
    fun `declare availability should fail when missing user id`() {
        val request = DeclareAvailabilityRequest {
            day = "2020-11-01"
            arrangement = UsersAvailability.WorkArrangement.ONSITE
        }
        catchThrowable { stub.declareAvailability(request) }.assertStatusException(Status.INVALID_ARGUMENT)
    }

    @Test
    fun `create should fail when invalid day`() {
        val request = DeclareAvailabilityRequest {
            userId = MDOE_ID
            day = "01/11/2020"
            arrangement = UsersAvailability.WorkArrangement.ONSITE
        }
        catchThrowable { stub.declareAvailability(request) }.assertStatusException(Status.INVALID_ARGUMENT)
    }

    private fun userAvailabilityRequestMapping(userAvailability: UserAvailability) = DeclareAvailabilityRequest {
        userId = userAvailability.userId
        day = userAvailability.day.toString()
        arrangement = UsersAvailability.WorkArrangement.valueOf(userAvailability.arrangement.name)
    }
}
