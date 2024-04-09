package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.application.ext.customFormatter
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.NotificationType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmailService(
    private val communicationService: CommunicationService
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun sendEmailUpdatedEmail(user: User, newEmail: String, oldEmail: String) {
        val date = LocalDateTime.now().customFormatter()

        val templateData = mapOf(
            "name" to user.firstName,
            "new_email" to newEmail,
            "date" to date
        )

        communicationService.sendEmail(
            recipient = oldEmail,
            templateData = templateData,
            notificationType = NotificationType.EMAIL_UPDATED_TEMPLATE
        )
    }

    fun sendNewAccessEmail(user: User, device: Device) {
        val date = LocalDateTime.now().customFormatter()

        val templateData = mapOf(
            "device" to device.model,
            "name" to user.firstName,
            "date" to date
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            notificationType = NotificationType.NEW_ACCESS_TEMPLATE
        )
    }

    fun sendRedefinePasswordEmail(code: String, user: User) {
        val date = LocalDateTime.now().customFormatter()

        val templateData = mapOf(
            "code" to code,
            "name" to user.firstName,
            "date" to date
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            notificationType = NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE
        )
    }

    fun sendPasswordUpdatedEmail(user: User) {
        val date = LocalDateTime.now().customFormatter()

        val templateData = mapOf(
            "name" to user.firstName,
            "date" to date
        )

        communicationService.sendEmail(
            recipient = user.email,
            templateData = templateData,
            notificationType = NotificationType.PASSWORD_UPDATED_TEMPLATE
        )
    }

    fun sendWelcomeEmail(user: User) {
        val templateData = mapOf(
            "name" to user.firstName
        )

        try {
            communicationService.sendEmail(
                recipient = user.email,
                templateData = templateData,
                notificationType = NotificationType.WELCOME_TEMPLATE
            )
        } catch (e: Exception) {
            logger.error("Failed to send welcome email to user ${user.id}")
        }
    }
}