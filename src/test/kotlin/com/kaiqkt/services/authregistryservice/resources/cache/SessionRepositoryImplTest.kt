package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

class SessionRepositoryImplTest {
    private val redisTemplate: StringRedisTemplate = mockk(relaxed = true)
    private val hashOperations = redisTemplate.opsForHash<String, String>()
    private val expiration = "1"
    private val sessionRepositoryImpl: SessionRepositoryImpl =
        SessionRepositoryImpl(redisTemplate, expiration)

    @Test
    fun `given an session with expiration, should persist in database successfully`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every { hashOperations.put(any(), any(), any()) } just runs
        every { redisTemplate.expire(any(), any(), any()) } returns true

        sessionRepositoryImpl.save(session)

        verify { hashOperations.put(key, session.id, session.toJson()) }
        verify { redisTemplate.expire(key, expiration.toLong(), TimeUnit.DAYS) }
    }

    @Test
    fun `given an session with expiration,when fail to persist, should throw PersistenceException`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every { hashOperations.put(any(), any(), any()) } throws Exception()
        every { redisTemplate.expire(any(), any(), any()) } returns true

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.save(session)
        }

        verify { hashOperations.put(key, session.id, session.toJson()) }
        verify(exactly = 0) { redisTemplate.expire(any(), any(), any()) }
    }

    @Test
    fun `given an session with expiration,when fail to persist the expiration, should throw PersistenceException`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every { hashOperations.put(any(), any(), any()) } just runs
        every { redisTemplate.expire(any(), any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.save(session)
        }

        verify { hashOperations.put(key, session.id, session.toJson()) }
        verify { redisTemplate.expire(key, expiration.toLong(), TimeUnit.DAYS) }
    }

    @Test
    fun `given an sessionId and userId, should delete his session`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every { hashOperations.delete(any(), any()) } returns 1L

        sessionRepositoryImpl.delete(session.id, session.userId)

        verify { hashOperations.delete(key, session.id) }
    }

    @Test
    fun `given an sessionId and userId, when fail to delete, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.delete(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.delete(session.id, session.userId)
        }
    }

    @Test
    fun `given an userId, should return his session`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } returns session.toJson()

        sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)

        verify { hashOperations.get(any(), any()) }
    }

    @Test
    fun `given an userId, when not exist, should return null`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } returns null

        sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)

        verify { hashOperations.get(any(), any()) }
    }

    @Test
    fun `given an userId, when fail to get the session, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { hashOperations.get(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.findByIdAndUserId(session.id, session.userId)
        }
    }

    @Test
    fun `given a userId, should return his sessions`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every {
            redisTemplate.keys(any())
        } returns setOf(key)

        every { hashOperations.get(any(), any()) } returns session.toJson()

        sessionRepositoryImpl.findAllByUserId(session.userId)

        verify { redisTemplate.keys("*${session.userId}*") }
        verify { hashOperations.get(key, session.id) }
    }

    @Test
    fun `given a userId, when not exist, should return null`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every {
            redisTemplate.keys(any())
        } returns setOf(key)

        every { hashOperations.get(any(),any()) } returns null

        val sessions = sessionRepositoryImpl.findAllByUserId(session.userId)

        verify { redisTemplate.keys("*${session.userId}*") }
        verify { hashOperations.get(any(), any()) }
        Assertions.assertNull(sessions)
    }

    @Test
    fun `given a userId, when fail to get the sessions, should throw PersistenceException`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every {
            redisTemplate.keys(any())
        } returns setOf(key)

        every { hashOperations.get(any(), any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.findAllByUserId(session.userId)
        }
        verify { redisTemplate.keys("*${session.userId}*") }
    }

    @Test
    fun `given a userId, when fail to get the keys of sessions, should throw PersistenceException`() {
        val session = SessionSampler.sample()

        every { redisTemplate.keys(any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.findAllByUserId(session.userId)
        }
    }


    @Test
    fun `given a user id, should delete all sessions based on userid successfully`() {
        val userId = ULID.random()
        val sessionId = ULID.random()
        val pattern = "*$userId*"
        val key = generateSessionKey(userId, sessionId)

        every { redisTemplate.keys(any()) } returns setOf(key)

        sessionRepositoryImpl.deleteAllByUserId(userId)

        verify { redisTemplate.keys(pattern) }
        verify { redisTemplate.delete(setOf(key)) }
    }

    @Test
    fun `given a user id, when fail to delete all sessions, should throw PersistenceException`() {
        val userId = ULID.random()
        val sessionId = ULID.random()
        val key = generateSessionKey(userId, sessionId)

        every { redisTemplate.keys(any()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.deleteAllByUserId(userId)
        }

        verify(exactly = 0) { redisTemplate.delete(key) }
    }

    @Test
    fun `given a user id, when not exists sessions, should do nothing`() {
        val userId = ULID.random()
        val sessionId = ULID.random()

        every {
            redisTemplate.keys(any())
        } returns setOf()
        every { hashOperations.delete(any(), any()) } returns 0L

        sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(sessionId, userId)

        verify { redisTemplate.keys("*$userId*") }
        verify(exactly = 0) { hashOperations.delete(any(), any()) }
    }

    @Test
    fun `given a user id, should delete a sessions based on user id, except the actual successfully`() {
        val userId = ULID.random()
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())
        val key1 = generateSessionKey(userId, session1.id)
        val key2 = generateSessionKey(userId, session2.id)
        val keys = setOf(key1, key2)

        every {
            redisTemplate.keys(any())
        } returns keys

        every { redisTemplate.delete(keys) } returns 1L

        sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session1.id, userId)

        verify { redisTemplate.keys("*$userId*") }
        verify { redisTemplate.delete(listOf(key2)) }
    }

    @Test
    fun `given a user id, when there is no session other than the current one, should do nothing`() {
        val session = SessionSampler.sample()
        val key = generateSessionKey(session.userId, session.id)

        every {
            redisTemplate.keys(any())
        } returns setOf(key)

        sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session.id, session.userId)

        verify { redisTemplate.keys("*${session.userId}*") }
        verify(exactly = 0) { hashOperations.delete(key, session.id) }
    }


    @Test
    fun `given a user id, when fail to delete all sessions except the current, should throw PersistenceException`() {
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())
        val key1 = generateSessionKey(session1.userId, session1.id)
        val key2 = generateSessionKey(session2.userId, session2.id)
        val keys = setOf(key1, key2)

        every {
            redisTemplate.keys(any())
        } returns keys

        every { redisTemplate.delete(any<Set<String>>()) } throws Exception()

        assertThrows<PersistenceException> {
            sessionRepositoryImpl.deleteAllByUserIdExceptCurrent(session1.id, session1.userId)
        }

        verify { redisTemplate.keys("*${session1.userId}*") }
        verify { redisTemplate.delete(listOf(key2)) }
    }

    private fun generateSessionKey(userId: String, sessionId: String) = "USER_SESSION:$userId:$sessionId"
}