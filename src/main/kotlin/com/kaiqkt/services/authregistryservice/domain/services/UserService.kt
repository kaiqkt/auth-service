package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.exceptions.AlreadyInUseException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val authenticationService: AuthenticationService,
    private val sessionService: SessionService
) {

    fun create(device: Device, user: User): Authentication {
        logger.info("Creating user ${user.id}")

       if (userRepository.existsByEmail(user.email)) {
           throw AlreadyInUseException(ErrorType.EMAIL_IN_USE)
       }

        userRepository.save(user).run {
            logger.info("User ${user.id} created successfully")
            emailService.sendWelcomeEmail(this)
            return authenticationService.authenticate(this, device)
        }
    }

    fun updateEmail(userId: String, email: String, sessionId: String) {
        if (userRepository.existsByEmail(email)) {
            throw AlreadyInUseException(ErrorType.EMAIL_IN_USE)
        }

        val user = findById(userId)

        userRepository.updateEmail(userId, email)

        sessionService.revokeAllExceptCurrent(sessionId, userId)

        emailService.sendEmailUpdatedEmail(user, email, user.email)

        logger.info("Email of user $userId updated successfully")
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun findById(userId: String): User = userRepository.findById(userId).getOrNull() ?: throw UserNotFoundException()

    fun findByEmail(email: String): User = userRepository.findByEmail(email) ?: throw UserNotFoundException()

    fun updatePasswordWithActual(
        actualPassword: String,
        newPassword: String,
        userId: String,
        sessionId: String,
        device: Device
    ) {
        val user = findById(userId)

        if (!EncryptUtils.validatePassword(actualPassword, user.password)) {
            throw BadCredentialsException()
        }

        sessionService.revokeAllExceptCurrent(sessionId, userId)

        updatePassword(userId, newPassword)

        emailService.sendPasswordUpdatedEmail(user)
    }

    fun updatePassword(userId: String, password: String) =
        userRepository.updatePassword(userId, EncryptUtils.encryptPassword(password)).also {
            logger.info("User $userId updated password successfully")
        }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}