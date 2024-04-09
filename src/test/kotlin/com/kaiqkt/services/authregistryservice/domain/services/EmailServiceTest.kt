package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.NotificationType
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class EmailServiceTest {
    private val communicationService: CommunicationService = mockk(relaxed = true)
    private val emailService: EmailService = EmailService(communicationService)

    @Test
    fun `given email updated email, should send with successfully`() {
        val user = UserSampler.sample()
        val newEmail = "gmail@shinji.com"
        val oldEmail = "shnji@c1.com"

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendEmailUpdatedEmail(user, newEmail, oldEmail)

        verify { communicationService.sendEmail(oldEmail, any(), NotificationType.EMAIL_UPDATED_TEMPLATE) }
    }

    @Test
    fun `given email updated email, when fail, should throw a exception`() {
        val user = UserSampler.sample()
        val newEmail = "gmail@shinji.com"
        val oldEmail = "shnji@c1.com"

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException("")

        assertThrows<ResourceException> {
            emailService.sendEmailUpdatedEmail(user, newEmail, oldEmail)
        }

        verify { communicationService.sendEmail(oldEmail, any(), NotificationType.EMAIL_UPDATED_TEMPLATE) }
    }

    @Test
    fun `given welcome email, should send with successfully`() {
        val user = UserSampler.sample()
        val templateData = mapOf("name" to user.firstName)

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendWelcomeEmail(user)

        verify { communicationService.sendEmail(user.email, templateData, NotificationType.WELCOME_TEMPLATE) }
    }

    @Test
    fun `given welcome email, when fail to send the email, should throw a ResourceException`() {
        val user = UserSampler.sample()
        val templateData = mapOf("name" to user.firstName)

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        emailService.sendWelcomeEmail(user)

        verify { communicationService.sendEmail(user.email, templateData, NotificationType.WELCOME_TEMPLATE) }
    }

    @Test
    fun `given new access email, should send with successfully`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendNewAccessEmail(user, device)

        verify { communicationService.sendEmail(user.email, any(), NotificationType.NEW_ACCESS_TEMPLATE) }
    }

    @Test
    fun `given new access email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendNewAccessEmail(user, device)
        }

        verify { communicationService.sendEmail(user.email, any(), NotificationType.NEW_ACCESS_TEMPLATE) }
    }

    @Test
    fun `given redefine password email, should send with successfully`() {
        val user = UserSampler.sample()
        val code = Random.nextInt().toString()

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendRedefinePasswordEmail(code, user)

        verify { communicationService.sendEmail(user.email, any(), NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE) }
    }

    @Test
    fun `given redefine password email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()
        val code = Random.nextInt().toString()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendRedefinePasswordEmail(code, user)
        }

        verify { communicationService.sendEmail(user.email, any(), NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE) }
    }

    @Test
    fun `given password updated email, should send with successfully`() {
        val user = UserSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } just runs

        emailService.sendPasswordUpdatedEmail(user)

        verify { communicationService.sendEmail(user.email, any(), NotificationType.PASSWORD_UPDATED_TEMPLATE) }
    }

    @Test
    fun `given password updated email,when fail to send the email, should throw ResourceException`() {
        val user = UserSampler.sample()

        every { communicationService.sendEmail(any(), any(), any()) } throws ResourceException(
            "Fail to send email ${user.email}, status 500, result: null"
        )

        assertThrows<ResourceException> {
            emailService.sendPasswordUpdatedEmail(user)
        }

        verify { communicationService.sendEmail(user.email, any(), NotificationType.PASSWORD_UPDATED_TEMPLATE) }
    }
}