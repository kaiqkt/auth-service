package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.SendRedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.utils.randomSixCharNumber
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class RedefinePasswordTest : ApplicationIntegrationTest() {

    @Test
    fun `given a request to redefine the password, when the code exist, should update the password and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val redefinePasswordCode = randomSixCharNumber()
        val request = RedefinePasswordV1Sampler.sample(redefinePasswordCode)
        val session = SessionSampler.sample()

        userRepository.save(user)
        sessionRepository.save(session)
        redefinePasswordRepository.save(redefinePasswordCode, user.id)

        webTestClient
            .put()
            .uri("/redefine-password")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to redefine the password, when the code not exist, should return http status 401`() {
        val user = UserSampler.sample()
        val code = randomSixCharNumber()
        val request = RedefinePasswordV1Sampler.sample(code)

        userRepository.save(user)

        webTestClient
            .put()
            .uri("/redefine-password")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `given a request to generate redefine password code, when the user exist, should create de code and send the email and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val request = SendRedefinePasswordV1Sampler.emailSample()

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/redefine-password")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_redefine_password_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to validate the redefine code, when exist and is not expired, should just return http status 204`() {
        val user = UserSampler.sample()
        val redefinePasswordCode = randomSixCharNumber()

        userRepository.save(user)
        redefinePasswordRepository.save(redefinePasswordCode, user.id)

        webTestClient
            .get()
            .uri("/redefine-password/$redefinePasswordCode")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `given a request to validate the redefine code, when is expired and not exist anymore, should delete him and return http status 401`() {
        val user = UserSampler.sample()
        val redefinePasswordCode = randomSixCharNumber()

        userRepository.save(user)

        webTestClient
            .get()
            .uri("/redefine-password/$redefinePasswordCode")
            .exchange()
            .expectStatus()
            .isUnauthorized

        Assertions.assertNull(redefinePasswordRepository.findByCode(redefinePasswordCode))
    }


}