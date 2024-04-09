package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.domain.utils.randomSixCharNumber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RedefinePasswordService(
    private val redefinePasswordRepository: RedefinePasswordRepository,
    private val userService: UserService,
    private val sessionService: SessionService,
    private val emailService: EmailService
) {

    fun sendRedefineCodeEmail(email: String) {
        val user = userService.findByEmail(email)
        val code = randomSixCharNumber()

        redefinePasswordRepository.save(code, user.id)

        logger.info("Generated code to redefine password for user ${user.id}")

        emailService.sendRedefinePasswordEmail(code, user)
    }

    fun redefinePassword(code: String, newPassword: String) {
        val userId = validateCode(code)
        val user = userService.findById(userId)

        sessionService.revokeAll(userId)

        userService.updatePassword(userId, newPassword)

        emailService.sendPasswordUpdatedEmail(user)
    }


    fun validateCode(code: String, isValidation: Boolean = false): String {
        redefinePasswordRepository.findByCode(code)?.let {
            if (!isValidation) redefinePasswordRepository.delete(code)

            return it
        }
        throw InvalidRedefinePasswordException()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}