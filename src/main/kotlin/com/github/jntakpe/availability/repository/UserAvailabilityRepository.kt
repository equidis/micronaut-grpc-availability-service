package com.github.jntakpe.availability.repository

import com.github.jntakpe.availability.model.entity.UserAvailability
import com.github.jntakpe.availability.model.entity.UserAvailability_.Companion.Day
import com.github.jntakpe.availability.model.entity.UserAvailability_.Companion.UserId
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.MongoDatabase
import com.mongodb.reactor.client.toReactor
import org.bson.types.ObjectId
import org.litote.kmongo.ascending
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.findOneById
import org.litote.kmongo.reactivestreams.getCollection
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.inject.Singleton

@Singleton
class UserAvailabilityRepository(database: MongoDatabase) {

    private val collection = database.getCollection<UserAvailability>().toReactor()

    init {
        collection.createIndex(Indexes.compoundIndex(ascending(UserId, Day)), IndexOptions().unique(true)).toMono().subscribe()
    }

    fun findById(id: ObjectId): Mono<UserAvailability> = collection.findOneById(id).toMono()

    fun findByUserId(userId: String): Flux<UserAvailability> = collection.find(UserId eq userId)

    fun create(availability: UserAvailability): Mono<UserAvailability> = collection.insertOne(availability).thenReturn(availability)
}
