package com.kaiqkt.services.authregistryservice.domain.gateways

import com.kaiqkt.services.authregistryservice.resources.communication.entities.NotificationType

interface CommunicationService {
    fun sendEmail(recipient: String, templateData: Map<String, String>, notificationType: NotificationType)
}