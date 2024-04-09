package com.kaiqkt.services.authregistryservice.resources.communication

import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.EmailRequestSampler
import com.kaiqkt.services.authregistryservice.resources.communication.entities.NotificationType
import com.kaiqkt.services.authregistryservice.resources.communication.provider.EmailTemplateProvider
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommunicationServiceImplementationTest {
    private val communicationClient: CommunicationClient = mockk(relaxed = true)
    private val emailTemplateProvider: EmailTemplateProvider = EmailTemplateProvider(
        "s3://communication-d-1/emails/",
        "email-updated.html",
        "redefine-password.html",
        "password-updated.html",
        "welcome.html",
        "new-access.html"
    )

    private val communicationService: CommunicationService = CommunicationServiceImplementation(
        communicationClient,
        emailTemplateProvider
    )

    @Test
    fun `given the request to send an email, when is a update email template, should be sent with the correct subject and url template`() {
        val emailRequest = EmailRequestSampler.emailUpdateTemplate()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(emailRequest.recipient, emailRequest.template.data, NotificationType.EMAIL_UPDATED_TEMPLATE )

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a password redefine email, should be sent with the correct subject and url template`() {
        val emailRequest = EmailRequestSampler.passwordRedefineEmailSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(emailRequest.recipient, emailRequest.template.data, NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a password new access email, should be sent with the correct subject and url template`() {
        val emailRequest = EmailRequestSampler.newAccessSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(emailRequest.recipient, emailRequest.template.data, NotificationType.NEW_ACCESS_TEMPLATE)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a welcome email, should must be sent with the correct subject and url template`() {
        val emailRequest = EmailRequestSampler.welcomeEmailSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(emailRequest.recipient, emailRequest.template.data, NotificationType.WELCOME_TEMPLATE)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when is a password updated email, should must be sent with the correct subject and url template`() {
        val emailRequest = EmailRequestSampler.passwordUpdatedSample()

        every { communicationClient.sendEmail(any()) } just runs

        communicationService.sendEmail(emailRequest.recipient, emailRequest.template.data, NotificationType.PASSWORD_UPDATED_TEMPLATE)

        verify { communicationClient.sendEmail(emailRequest) }
    }

    @Test
    fun `given the request to send an email, when fail to send, should throw ResourceException`() {
        val recipient = "shinji@eva01.com"
        val templateData = mapOf(
            "name" to "shinji",
            "code" to "1234"
        )

        every { communicationClient.sendEmail(any()) } throws ResourceException(
            "Fail to send email kadkiasd@gmail.com, status 500, result: null"
        )

        assertThrows<ResourceException> {
            communicationService.sendEmail(recipient, templateData, NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE)
        }
    }
}