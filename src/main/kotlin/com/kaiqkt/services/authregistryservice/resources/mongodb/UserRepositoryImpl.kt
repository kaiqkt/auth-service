package com.kaiqkt.services.authregistryservice.resources.mongodb

import com.kaiqkt.commons.crypto.encrypt.Password
import com.kaiqkt.services.authregistryservice.domain.entities.Address
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepositoryCustom
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class UserRepositoryImpl(private val mongoTemplate: MongoTemplate) : UserRepositoryCustom {

    @Transactional
    override fun createOrUpdateAddress(userId: String, address: Address) {
        try {
            val result = updateAddress(userId, address)

            if (result.matchedCount == 0L) {
                val query = Query().addCriteria(Criteria.where("id").`is`(userId))

                val update = Update().apply {
                    addToSet("addresses", address)
                    set("updatedAt", LocalDateTime.now())
                }

                mongoTemplate.updateFirst(query, update, User::class.java)
            }
        } catch (ex: Exception) {
            throw PersistenceException("Error when persisting address for user $userId, error: $ex")
        }
    }

    override fun deleteAddress(userId: String, addressId: String) {
        try {
            val query = Query().addCriteria(Criteria.where("id").`is`(userId))

            val update = Update().apply {
                this.pull("addresses", Query().addCriteria(Criteria.where("id").`is`(addressId)))
                this.set("updatedAt", LocalDateTime.now())
            }

            mongoTemplate.updateFirst(query, update, User::class.java)
        } catch (ex: Exception) {
            throw PersistenceException("Error when deleting address for user $userId, error: $ex")
        }
    }

    override fun findByAddressId(userId: String, addressId: String): User? {
        try {
            val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(addressId))

            return mongoTemplate.findOne(query, User::class.java)
        } catch (ex: Exception) {
            throw PersistenceException("Error when finding address for user $userId, error: $ex")
        }
    }

    override fun updatePassword(userId: String, newPassword: Password) {
        try {
            val query = Query().addCriteria(Criteria.where("id").`is`(userId))
            val update = Update().apply {
                this.set("password", newPassword)
                this.set("updatedAt", LocalDateTime.now())
            }

            mongoTemplate.updateFirst(query, update, User::class.java)
        } catch (ex: Exception) {
            throw PersistenceException("Error when updating password for user $userId, error: $ex")
        }
    }

    override fun updateEmail(userId: String, email: String) {
        try {
            val query = Query().addCriteria(Criteria.where("id").`is`(userId))
            val update = Update().apply {
                this.set("email", email)
                this.set("updatedAt", LocalDateTime.now())
            }

            mongoTemplate.updateFirst(query, update, User::class.java)
        } catch (ex: Exception) {
            throw PersistenceException("Error when updating email for user $userId, error: $ex")
        }
    }

    private fun updateAddress(userId: String, address: Address): UpdateResult {
        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))

        val update = Update().apply {
            this.set("addresses.$.zipCode", address.zipCode)
            this.set("addresses.$.street", address.street)
            this.set("addresses.$.district", address.district)
            this.set("addresses.$.complement", address.complement)
            this.set("addresses.$.number", address.number)
            this.set("addresses.$.city", address.city)
            this.set("addresses.$.state", address.state)
            this.set("updatedAt", LocalDateTime.now())
        }

        return mongoTemplate.updateFirst(query, update, User::class.java)
    }

}