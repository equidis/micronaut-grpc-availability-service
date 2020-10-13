package com.github.jntakpe.availability.model.entity

import com.github.jershell.kbson.ObjectIdSerializer
import com.github.jntakpe.commons.Identifiable
import com.github.jntakpe.commons.Identifiable.Companion.DB_ID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.Data
import org.litote.kmongo.serialization.LocalDateSerializer
import java.time.LocalDate

@Data
@Serializable
data class UserAvailability(
    val userId: String,
    @Serializable(with = LocalDateSerializer::class) val day: LocalDate,
    val arrangement: WorkArrangementType,
    @SerialName(DB_ID) @Serializable(ObjectIdSerializer::class) override val id: ObjectId = ObjectId()
) : Identifiable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAvailability

        if (userId != other.userId) return false
        if (day != other.day) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + day.hashCode()
        return result
    }

    override fun toString(): String {
        return "Availability(userId='$userId', day=$day, arrangement=$arrangement)"
    }
}
