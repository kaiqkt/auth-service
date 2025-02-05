package com.kaiqkt.services.authregistryservice.resources.communication

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.kaiqkt.services.authregistryservice.resources.communication.entities.Email
import com.kaiqkt.services.authregistryservice.resources.exceptions.ResourceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CommunicationClient(
    @Value("\${communication.url}")
    private val serviceUrl: String,
    @Value("\${communication.service-shared}")
    private val serviceShared: String
) {

    fun sendEmail(email: Email) {
        logger.info("Sending email request to communication service to ${email.recipient}")

        Fuel.post("$serviceUrl/email")
            .jsonBody(jacksonObjectMapper().writeValueAsString(email))
            .header(
                mapOf(
                    Headers.AUTHORIZATION to serviceShared,
                    Headers.CONTENT_TYPE to "application/vnd.kaiqkt_email_v1+json"
                )
            ).response().let {(_, response, result) ->
                when{
                    response.isSuccessful -> {
                        logger.info("Email sent to: ${email.recipient} with subject ${email.subject} successfully")
                    }
                    else -> {
                        throw ResourceException(
                            "Failed to send email for ${email.recipient}, status ${response.statusCode}, result: $result"
                        )
                    }
                }
            }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}