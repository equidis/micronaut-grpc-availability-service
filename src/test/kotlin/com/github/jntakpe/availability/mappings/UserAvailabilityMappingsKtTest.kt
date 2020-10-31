package com.github.jntakpe.availability.mappings

import com.github.jntakpe.availability.dao.UserAvailabilityDao.TransientData.MDOE_ID
import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType
import com.github.jntakpe.availability.proto.DeclareAvailabilityRequest
import com.github.jntakpe.availability.proto.UsersAvailability
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UserAvailabilityMappingsKtTest {

    @Test
    fun `to entity should map request`() {
        val request = DeclareAvailabilityRequest {
            userId = MDOE_ID
            day = "2020/10/31"
            arrangement = UsersAvailability.WorkArrangement.ONSITE
        }
        val entity = request.toEntity()
        val expected = UserAvailability(MDOE_ID, LocalDate.of(2020, 10, 31), WorkArrangementType.ONSITE)
        assertThat(entity).usingRecursiveComparison().isEqualTo(expected).ignoringFields("id")
    }
}
