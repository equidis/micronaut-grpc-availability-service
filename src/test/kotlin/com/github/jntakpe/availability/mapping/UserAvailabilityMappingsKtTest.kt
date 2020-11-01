package com.github.jntakpe.availability.mapping

import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.JDOE_ID
import com.github.jntakpe.availability.dao.UserAvailabilityDao.PersistedData.jdoeOnsite
import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData.MDOE_ID
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType
import com.github.jntakpe.availability.proto.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability.WorkArrangement
import com.github.jntakpe.availability.proto.UsersAvailabilityResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UserAvailabilityMappingsKtTest {

    @Test
    fun `to entity should map request`() {
        val date = LocalDate.of(2020, 10, 31)
        val request = DeclareAvailabilityRequest {
            userId = MDOE_ID
            day = date.toString()
            arrangement = WorkArrangement.ONSITE
        }
        val entity = request.toEntity()
        val expected = UserAvailability(MDOE_ID, date, WorkArrangementType.ONSITE)
        assertThat(entity).usingRecursiveComparison()
            .ignoringFields(UserAvailability::id.name, UserAvailability::arrangement.name)
            .isEqualTo(expected)
        assertThat(entity.id).isNotNull
        assertThat(entity.arrangement.name).isEqualTo(expected.arrangement.name)
    }

    @Test
    fun `to response should map entity`() {
        val expected = UsersAvailabilityResponse {
            userId = JDOE_ID
            day = jdoeOnsite.day.toString()
            arrangement = WorkArrangement.valueOf(jdoeOnsite.arrangement.name)
            id = jdoeOnsite.id.toString()
        }
        val response = jdoeOnsite.toResponse()
        assertThat(response).usingRecursiveComparison().ignoringFields(UserAvailability::arrangement.name).isEqualTo(expected)
        assertThat(response.arrangement.name).isEqualTo(expected.arrangement.name)
    }
}
