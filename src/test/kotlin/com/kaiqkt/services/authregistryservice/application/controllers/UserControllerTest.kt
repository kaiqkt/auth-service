package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.application.dto.EmailV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.NewPasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.NewUserV1Sampler
import com.kaiqkt.services.authregistryservice.application.security.CustomAuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.AuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AlreadyInUseException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.services.UserService
import io.azam.ulidj.ULID
import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder

class UserControllerTest {

    private val userService: UserService = mockk(relaxed = true)
    private val controller: UserController = UserController(userService)

    @Test
    fun `given a request to create a user, when successfully created, should return http status 200`() {
        val request = NewUserV1Sampler.sample()
        val authentication = AuthenticationSampler.sample()
        val device = DeviceSampler.sample()

        every { userService.create(any(), any()) } returns authentication

        val response = controller.create(USER_AGENT, APP_VERSION, request)

        verify { userService.create(device, any()) }

        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertNotNull(response)
    }

    @Test
    fun `given a request to create a user, when email is already in use, should throw ValidationException`() {
        val request = NewUserV1Sampler.sample()

        every {
            userService.create(
                any(),
                any()
            )
        } throws AlreadyInUseException(ErrorType.EMAIL_IN_USE)

        assertThrows<AlreadyInUseException> {
            controller.create(USER_AGENT, APP_VERSION, request)
        }
    }

    @Test
    fun `given a request to find a user, when the user exists, should return his information and http status 200`() {
        val user = UserSampler.sample()

        every { userService.findById(any()) } returns user

        val response = controller.findById(user.id)

        verify { userService.findById(user.id) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
    }

    @Test
    fun `given a request to get and user, when given a user id not exist in database, should throw UserNotFoundException`() {
        val userId = ULID.random()

        every { userService.findById(any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.findById(userId)
        }

        verify { userService.findById(userId) }
    }

    @Test
    fun `given a request to find a user based on access token, when the user exists, should return his information and http status 200`() {
        val user = UserSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.findById(any()) } returns user

        val response = controller.findByAccessToken()

        verify { userService.findById(user.id) }

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertNotNull(response.body)
    }

    @Test
    fun `given a request to get and user based on access token, when given a user id not exist in database, should throw UserNotFoundException`() {
        val userId = "01GFPPTXKZ8ZJRG8MF701M0W99"

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.findById(any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.findByAccessToken()
        }

        verify { userService.findById(userId) }
    }

    @Test
    fun `given a request to update password based an actual password, when the user exists and the actual password matches, should revoke all sessions except the actual and return http status 204`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePasswordWithActual(any(), any(), any(), any(), any()) } just runs

        val response = controller.updatePassword(USER_AGENT, APP_VERSION, password)

        verify {
            userService.updatePasswordWithActual(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a request to update password based an actual password, when the user not exists, should throw UserNotFoundException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePasswordWithActual(any(), any(), any(), any(), any()) } throws UserNotFoundException()

        assertThrows<UserNotFoundException> {
            controller.updatePassword(USER_AGENT, APP_VERSION, password)
        }

        verify {
            userService.updatePasswordWithActual(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }
    }

    @Test
    fun `given a request to update password based an actual password, when the actual password not matches, should throw BadCredentialsException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val password = NewPasswordV1Sampler.sample()
        val device = DeviceSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updatePasswordWithActual(any(), any(), any(), any(), any()) } throws BadCredentialsException()

        assertThrows<BadCredentialsException> {
            controller.updatePassword(USER_AGENT, APP_VERSION, password)
        }

        verify {
            userService.updatePasswordWithActual(
                password.actualPassword,
                password.newPassword,
                user.id,
                session.id,
                device
            )
        }
    }

    @Test
    fun `given a email to update, should update and return http 204`() {
        val emailV1 = EmailV1Sampler.sample()
        val user = UserSampler.sample()
        val authentication = CustomAuthenticationSampler.sample()

        SecurityContextHolder.getContext().authentication = authentication

        every { userService.updateEmail(any(), any(), any()) } just runs

        val response = controller.updateEmail(emailV1)

        verify { userService.updateEmail(user.id, emailV1.email, authentication.sessionId!!) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a email to update, when email already exist, should throw a exception`() {
        val emailV1 = EmailV1Sampler.sample()
        val user = UserSampler.sample()
        val authentication = CustomAuthenticationSampler.sample()

        SecurityContextHolder.getContext().authentication = CustomAuthenticationSampler.sample()

        every { userService.updateEmail(any(), any(), any()) } throws AlreadyInUseException(ErrorType.EMAIL_IN_USE     )

        assertThrows<AlreadyInUseException> {
            controller.updateEmail(emailV1)
        }

        verify { userService.updateEmail(user.id, emailV1.email, authentication.sessionId!!) }
    }
}