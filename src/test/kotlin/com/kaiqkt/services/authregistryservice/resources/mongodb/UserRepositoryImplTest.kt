package com.kaiqkt.services.authregistryservice.resources.mongodb

import com.kaiqkt.services.authregistryservice.domain.entities.AddressSampler
import com.kaiqkt.services.authregistryservice.domain.entities.PasswordSampler
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import com.mongodb.client.result.UpdateResult
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class UserRepositoryImplTest {
    private val mongoTemplate: MongoTemplate = mockk(relaxed = true)
    private val repository = UserRepositoryImpl(mongoTemplate)

    @Test
    fun `given address to create, should create successfully`() {
        val address = AddressSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))
        val queryWithAddress =
            Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            0,
            null,
            null
        ) andThen UpdateResult.acknowledged(
            1L,
            1L,
            null
        )

        repository.createOrUpdateAddress(userId, address)

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
        verify { mongoTemplate.updateFirst(queryWithAddress, any(), User::class.java) }
    }

    @Test
    fun `given a address to update, should update successfully`() {
        val address = AddressSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            1,
            null,
            null
        )

        repository.createOrUpdateAddress(userId, address)

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given address to create or update, when fail, should throws a exception`() {
        val address = AddressSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId).and("addresses.id").`is`(address.id))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } throws Exception()

        assertThrows<PersistenceException> {
            repository.createOrUpdateAddress(userId, address)
        }

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a address to delete, should delete successfully`() {
        val addressId = ULID.random()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            1,
            null,
            null
        )

        repository.deleteAddress(userId, addressId)

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a address to delete, when fail, should throws a exception`() {
        val addressId = ULID.random()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } throws Exception()

        assertThrows<PersistenceException> {
            repository.deleteAddress(userId, addressId)
        }

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a password to update, should update the password and the updatedAt field successfully`() {
        val newPassword = PasswordSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            1,
            null,
            null
        )

        repository.updatePassword(userId, newPassword)

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a password to update, when fail, should throws a exception`() {
        val newPassword = PasswordSampler.sample()
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } throws Exception()

        assertThrows<PersistenceException> {
            repository.updatePassword(userId, newPassword)
        }

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a email to update, should update the email and the updatedAt field successfully`() {
        val email = "shinji@gmail.com"
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } returns UpdateResult.acknowledged(
            1,
            null,
            null
        )

        repository.updateEmail(userId, email)

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a email to update, when fail, should throws a exception`() {
        val email = "shinji@gmail.com"
        val userId = ULID.random()

        val query = Query().addCriteria(Criteria.where("id").`is`(userId))

        every { mongoTemplate.updateFirst(any(), any(), User::class.java) } throws Exception()

        assertThrows<PersistenceException> {
            repository.updateEmail(userId, email)
        }

        verify { mongoTemplate.updateFirst(query, any(), User::class.java) }
    }

    @Test
    fun `given a user id and a address id, when exists, should return the user`() {
        val user = UserSampler.sample()
        val addressId = "01GKNAXJG7T7QPRC8JT87DVHT8"
        val query = Query().addCriteria(Criteria.where("id").`is`(user.id).and("addresses.id").`is`(addressId))

        every { mongoTemplate.findOne(any(), User::class.java) } returns user

        val response = repository.findByAddressId(user.id, addressId)

        verify { mongoTemplate.findOne(query, User::class.java) }

        Assertions.assertNotNull(response)
    }

    @Test
    fun `given a user id and a address id, when not exists, should return null`() {
        val user = UserSampler.sample()
        val addressId = "01GKNAXJG7T7QPRC8JT87DVHT8"
        val query = Query().addCriteria(Criteria.where("id").`is`(user.id).and("addresses.id").`is`(addressId))

        every { mongoTemplate.findOne(any(), User::class.java) } returns null

        val response = repository.findByAddressId(user.id, addressId)

        verify { mongoTemplate.findOne(query, User::class.java) }

        Assertions.assertNull(response)
    }

    @Test
    fun `given a user id and a address id, when fail, should throws a exception`() {
        val user = UserSampler.sample()
        val addressId = "01GKNAXJG7T7QPRC8JT87DVHT8"
        val query = Query().addCriteria(Criteria.where("id").`is`(user.id).and("addresses.id").`is`(addressId))

        every { mongoTemplate.findOne(any(), User::class.java) } throws Exception()

        assertThrows<PersistenceException> {
            repository.findByAddressId(user.id, addressId)
        }

        verify { mongoTemplate.findOne(query, User::class.java) }
    }
}