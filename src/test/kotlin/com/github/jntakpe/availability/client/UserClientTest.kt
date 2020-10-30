package com.github.jntakpe.availability.client

import com.github.jntakpe.availability.common.MockUserService.Companion.JDOE_USERNAME
import com.github.jntakpe.availability.common.MockUserService.Companion.MDOE_USERNAME
import com.github.jntakpe.availability.dao.UserAvailabilityDao
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.commons.test.expectStatusException
import io.grpc.Status
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.kotlin.test.test

@MicronautTest
internal class UserClientTest(private val client: UserClient) {

    @ParameterizedTest
    @ArgumentsSource(UserAvailabilityDao.PersistedData::class)
    fun `find user by id should find`(availability: UserAvailability) {
        client.findById(availability.userId).test()
            .expectSubscription()
            .consumeNextWith { assertThat(it.id).isEqualTo(availability.userId) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "", "*"])
    fun `find user by id should fail when id does not exists`(userId: String) {
        client.findById(userId).test()
            .expectSubscription()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }

    @ParameterizedTest
    @ValueSource(strings = [JDOE_USERNAME, MDOE_USERNAME])
    fun `find user by username should find`(username: String) {
        client.findByUsername(username).test()
            .expectSubscription()
            .consumeNextWith { assertThat(it.username).isEqualTo(username) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "", "*"])
    fun `find user by username should fail when id does not exists`(username: String) {
        client.findByUsername(username).test()
            .expectSubscription()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }
}
