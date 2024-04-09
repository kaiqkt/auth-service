package com.kaiqkt.services.authregistryservice.resources.communication

import com.kaiqkt.services.authregistryservice.domain.gateways.CommunicationService
import com.kaiqkt.services.authregistryservice.resources.communication.entities.Email
import com.kaiqkt.services.authregistryservice.resources.communication.entities.NotificationType
import com.kaiqkt.services.authregistryservice.resources.communication.entities.Template
import com.kaiqkt.services.authregistryservice.resources.communication.provider.EmailTemplateProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@EnableConfigurationProperties(*[EmailTemplateProvider::class])
@Component
class CommunicationServiceImplementation(
    private val communicationClient: CommunicationClient,
    private val emailTemplatesProvider: EmailTemplateProvider
) : CommunicationService {

    override fun sendEmail(recipient: String, templateData: Map<String, String>, notificationType: NotificationType) {
        val email = Email(
            recipient = recipient,
            subject = notificationType.title,
            template = Template(
                url = "${emailTemplatesProvider.locationTemplate}${getTemplateUrl(notificationType)}",
                data = templateData
            )
        )

        communicationClient.sendEmail(email)
    }

    private fun getTemplateUrl(notificationType: NotificationType): String {

        return when (notificationType) {
            NotificationType.PASSWORD_REDEFINE_PASSWORD_TEMPLATE -> emailTemplatesProvider.redefinePasswordTemplate
            NotificationType.EMAIL_UPDATED_TEMPLATE -> emailTemplatesProvider.emailUpdatedTemplate
            NotificationType.WELCOME_TEMPLATE -> emailTemplatesProvider.welcomeTemplate
            NotificationType.PASSWORD_UPDATED_TEMPLATE -> emailTemplatesProvider.passwordUpdatedTemplate
            NotificationType.NEW_ACCESS_TEMPLATE -> emailTemplatesProvider.newAccessTemplate
        }
    }
}