package com.kaiqkt.services.authregistryservice.application.controllers

import com.github.kittinunf.fuel.core.Headers
import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.commons.crypto.jwt.JWTUtils
import com.kaiqkt.commons.security.auth.ROLE_USER
import com.kaiqkt.services.authregistryservice.ApplicationIntegrationTest
import com.kaiqkt.services.authregistryservice.application.dto.EmailV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.NewPasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.NewUserV1Sampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1
import com.kaiqkt.services.authregistryservice.resources.communication.helpers.CommunicationServiceMock
import io.azam.ulidj.ULID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import kotlin.jvm.optionals.getOrNull

class UserTest : ApplicationIntegrationTest() {

    @Test
    fun `giving a request to create a new user, when it is valid, should save it in mongo, send email request and return http status 201 with authentication response`() {
        val request = NewUserV1Sampler.sample()

        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .post()
            .uri("/user")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(AuthenticationV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                val token = JWTUtils.getClaims(body?.accessToken!!, customerSecret)
                Assertions.assertNotNull(body)

                val user = userRepository.findByEmail(request.email)

                val session = sessionRepository.findByIdAndUserId(token.sessionId, user!!.id)

                Assertions.assertNotNull(session)
            }

        val user = userRepository.findAll()[0]

        val passwordDecrypted = EncryptUtils.validatePassword(request.password, user.password)

        Assertions.assertNotNull(user)
        Assertions.assertEquals(request.fullName, user.fullName)
        Assertions.assertEquals(request.email, user.email)
        Assertions.assertTrue(passwordDecrypted)

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `giving a request to create a new user, when already exist a user with email, should return http status 400`() {
        val request = NewUserV1Sampler.sample()

        val user = UserSampler.sample()

        userRepository.save(user)

        webTestClient
            .post()
            .uri("/user")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ErrorV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody!!

                Assertions.assertEquals(ErrorType.EMAIL_IN_USE.name, body.type)
            }
    }

    @Test
    fun `given an access token, when exist, should return user response and status http 200`() {
        val user = UserSampler.sample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        webTestClient
            .get()
            .uri("/user")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UserV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(body?.fullName, user.fullName)
                Assertions.assertEquals(body?.email, user.email)
            }

        Assertions.assertEquals(userRepository.findAll().size, 1)
    }

    @Test
    fun `given an access token, when not exist, should return http 404`() {
        val user = UserSampler.sample()

        val token =
            JWTUtils.generateToken(
                user.id,
                customerSecret,
                listOf(ROLE_USER),
                ULID.random(),
                sessionExpiration.toLong()
            )

        webTestClient
            .get()
            .uri("/user")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `given a get user request, when the user not exist, should return http status 404`() {

        webTestClient
            .get()
            .uri("/user/${ULID.random()}")
            .header(Headers.AUTHORIZATION, serviceSecret)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `given a get user request, when is user exists in, should return http status 200 with user response`() {
        val user = UserSampler.sample()
        userRepository.save(user)
        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .get()
            .uri("/user/${user.id}")
            .header(Headers.AUTHORIZATION, serviceSecret)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UserV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                Assertions.assertEquals(body?.fullName, user.fullName)
                Assertions.assertEquals(body?.email, user.email)
            }

        Assertions.assertEquals(userRepository.findAll().size, 1)
    }

    @Test
    fun `given a request to update password, when the actual password matches, should update, delete all sessions except the current and return http status 204`() {
        CommunicationServiceMock.sendEmail.mockSendEmail()

        val user = UserSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.sample()
        val session1 = SessionSampler.sample()
        val session2 = SessionSampler.sample().copy(id = ULID.random())

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session1.id, sessionExpiration.toLong())

        sessionRepository.save(session1)
        sessionRepository.save(session2)

        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isNoContent

        Assertions.assertEquals(sessionRepository.findAllByUserId(user.id)?.size, 1)

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @Test
    fun `given a request to update password, when the actual password not matches, should throw http status 401`() {
        val user = UserSampler.sample()
        val session1 = SessionSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.invalidPasswordSample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session1.id, sessionExpiration.toLong())

        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isUnauthorized

    }

    @Test
    fun `given a request to update password, when the new password not match with the requirements, should throw http status 400 and return a message`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val newPasswordV1 = NewPasswordV1Sampler.invalidNewPasswordSample()

        userRepository.save(user)

        val token =
            JWTUtils.generateToken(user.id, customerSecret, listOf(ROLE_USER), session.id, sessionExpiration.toLong())


        webTestClient
            .put()
            .uri("/user/update-password")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .header("User-Agent", USER_AGENT)
            .header("App-Version", APP_VERSION)
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_password_v1+json"))
            .bodyValue(newPasswordV1)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a email to update, when user exist, should update successfully and return http status 204`() {
        val user = UserSampler.sample()
        val request = EmailV1Sampler.sample().copy(email = "shjinji@eva.com")

        val token = JWTUtils.generateToken(
            user.id,
            customerSecret,
            listOf(ROLE_USER),
            ULID.random(),
            sessionExpiration.toLong()
        )

        userRepository.save(user)

        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .put()
            .uri("/user/email")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_email_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isNoContent

        val userUpdated = userRepository.findById(user.id).getOrNull()

        Assertions.assertEquals(request.email, userUpdated?.email)

        CommunicationServiceMock.sendEmail.verifySendEmail(1)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `given a email to update, when email is already used, hould return http status 400`() {
        val user = UserSampler.sample()
        val request = EmailV1Sampler.sample().copy(email = user.email)

        val token = JWTUtils.generateToken(
            user.id,
            customerSecret,
            listOf(ROLE_USER),
            ULID.random(),
            sessionExpiration.toLong()
        )

        userRepository.save(user)

        CommunicationServiceMock.sendEmail.mockSendEmail()

        webTestClient
            .put()
            .uri("/user/email")
            .header(Headers.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.parseMediaType("application/vnd.kaiqkt_user_email_v1+json"))
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ErrorV1::class.java)
            .consumeWith { response ->
                val body = response.responseBody!!

                Assertions.assertEquals(ErrorType.EMAIL_IN_USE.name, body.type)
            }
    }
}