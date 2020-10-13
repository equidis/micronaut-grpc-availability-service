package com.github.jntakpe.users.config

import com.github.jntakpe.commons.IdentifiableIdController
import com.mongodb.WriteConcern.W1
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import io.micronaut.context.annotation.Factory
import org.litote.kmongo.reactivestreams.withKMongo
import org.litote.kmongo.serialization.changeIdController
import javax.inject.Singleton

@Factory
class MongoConfig {

    init {
        changeIdController(IdentifiableIdController())
    }

    @Singleton
    fun databaseClient(client: MongoClient): MongoDatabase = client.getDatabase("micronaut_availability").withWriteConcern(W1).withKMongo()
}
