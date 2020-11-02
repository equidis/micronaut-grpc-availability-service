package com.github.jntakpe.availability.dao

import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.WorkArrangementType.OFF
import com.github.jntakpe.availability.model.entity.WorkArrangementType.ONSITE
import com.github.jntakpe.availability.model.entity.WorkArrangementType.REMOTE
import com.github.jntakpe.commons.mongo.test.MongoDao
import com.github.jntakpe.commons.test.TestDataProvider
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.reactivestreams.getCollection
import java.time.LocalDate
import javax.inject.Singleton

@Singleton
class UserAvailabilityDao(database: MongoDatabase) : MongoDao<UserAvailability>(database.getCollection(), PersistedData) {

    object PersistedData : TestDataProvider<UserAvailability> {

        val JDOE_ID = ObjectId().toString()
        val jdoeOnsite = UserAvailability(JDOE_ID, LocalDate.of(2020, 10, 15), ONSITE, ObjectId())
        val jdoeRemote = UserAvailability(JDOE_ID, LocalDate.of(2020, 10, 16), REMOTE, ObjectId())
        val jdoeOff = UserAvailability(JDOE_ID, LocalDate.of(2020, 10, 19), OFF, ObjectId())

        override fun data() = listOf(jdoeOnsite, jdoeRemote, jdoeOff)
    }

    object TransientData : TestDataProvider<UserAvailability> {

        val MDOE_ID = ObjectId().toString()
        val mdoeOnsite = UserAvailability(MDOE_ID, LocalDate.of(2020, 10, 15), ONSITE, ObjectId())
        val mdoeRemote = UserAvailability(MDOE_ID, LocalDate.of(2020, 10, 16), REMOTE, ObjectId())
        val mdoeOff = UserAvailability(MDOE_ID, LocalDate.of(2020, 10, 19), OFF, ObjectId())

        override fun data() = listOf(mdoeOnsite, mdoeRemote, mdoeOff)
    }
}
