package com.kaiqkt.services.authregistryservice.resources.cache

import com.kaiqkt.services.authregistryservice.domain.entities.Session
import com.kaiqkt.services.authregistryservice.domain.repositories.SessionRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SessionRepositoryImpl(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${session-expiration}")
    private val sessionExpiration: String
) : SessionRepository {

    private val hashOperations = redisTemplate.opsForHash<String, String>()

    override fun save(session: Session) {
        val key = generateSessionKey(session.userId, session.id)
        try {
            hashOperations.put(key, session.id, session.toJson())
            redisTemplate.expire(key, sessionExpiration.toLong(), TimeUnit.DAYS)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to create session ${session.id}"
            )
        }
    }

    override fun delete(sessionId: String, userId: String) {
        val key = generateSessionKey(userId, sessionId)
        try {
            hashOperations.delete(key, sessionId)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete session $sessionId"
            )
        }
    }

    override fun findByIdAndUserId(sessionId: String, userId: String): Session? {
        val key = generateSessionKey(userId, sessionId)
        try {
            val json = hashOperations.get(key, sessionId) ?: return null
            return Session.toSession(json)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to find session $sessionId"
            )
        }
    }

    override fun findAllByUserId(userId: String): List<Session>? {
        val pattern = "*$userId*"
        val sessions = mutableListOf<Session>()

        try {
            val keys = redisTemplate.keys(pattern)

            keys.forEach { key ->
                val sessionId = key.substringAfterLast(":")
                hashOperations.get(key, sessionId)?.let {
                    sessions.add(Session.toSession(it))
                }
            }

            return if (sessions.isEmpty()) { null } else { sessions }
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to get sessions"
            )
        }
    }

    override fun deleteAllByUserId(userId: String) {
        val pattern = "*$userId*"
        try {
            val keys = redisTemplate.keys(pattern)

            redisTemplate.delete(keys)
        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete sessions"
            )
        }
    }

    override fun deleteAllByUserIdExceptCurrent(sessionId: String, userId: String) {
        val pattern = "*$userId*"

        try {
            val keys = redisTemplate.keys(pattern).filterNot { it.contains(sessionId) }

            redisTemplate.delete(keys)

        } catch (ex: Exception) {
            throw PersistenceException(
                "Unable to delete sessions"
            )
        }
    }

    private fun generateSessionKey(userId: String, sessionId: String) = "USER_SESSION:$userId:$sessionId"
}