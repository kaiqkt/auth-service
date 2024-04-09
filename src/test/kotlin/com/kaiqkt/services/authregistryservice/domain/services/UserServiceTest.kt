package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.AuthenticationSampler
import com.kaiqkt.services.authregistryservice.domain.entities.DeviceSampler
import com.kaiqkt.services.authregistryservice.domain.entities.SessionSampler
import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AlreadyInUseException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.UserRepository
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserServiceTest {
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val authenticationService: AuthenticationService = mockk(relaxed = true)
    private val sessionService: SessionService = mockk(relaxed = true)
    private val userService: UserService =
        UserService(
            userRepository,
            emailService,
            authenticationService,
            sessionService
        )

    @Test
    fun `given a new user, when validated with successfully, should persist in database, send welcome email and return authentication`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.save(any()) } returns user
        every { emailService.sendWelcomeEmail(any()) } just runs
        every {
            authenticationService.authenticate(
                any(),
                any()
            )
        } returns AuthenticationSampler.sample()

        userService.create(device, user)

        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.save(user) }
        verify { emailService.sendWelcomeEmail(user) }
        verify { authenticationService.authenticate(user, device) }
    }

    @Test
    fun `given a new user, when email is already in use, should throw AlreadyInUseException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.existsByEmail(any()) } returns true

        assertThrows<AlreadyInUseException> {
            userService.create(device, user)
        }

        verify { userRepository.existsByEmail(user.email) }
    }

    @Test
    fun `given a new user, when fail to create authentication session, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.save(any()) } returns user
        every { emailService.sendWelcomeEmail(any()) } just runs
        every {
            authenticationService.authenticate(any(), any())
        } throws PersistenceException("Unable to persist session 209310LDFL")

        assertThrows<PersistenceException> {
            userService.create(device, user)
        }

        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.save(user) }
        verify { emailService.sendWelcomeEmail(user) }
        verify { authenticationService.authenticate(user, device) }
    }


    @Test
    fun `given find user, when user is exist in database, should return his`() {
        val user = UserSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)

        userService.findById(user.id)

        verify { userRepository.findById(user.id) }
    }

    @Test
    fun `given an email to update,when is not in use, should update with successfully`() {
        val user = UserSampler.sample()
        val sessionId = ULID.random()

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { userRepository.updateEmail(any(), any()) } just runs

        userService.updateEmail(user.id, user.email, sessionId)

        verify { userRepository.findById(user.id) }
        verify { userRepository.existsByEmail(user.email) }
        verify { userRepository.updateEmail(user.id, user.email) }
    }

    @Test
    fun `given an email to update,when not find the user, should update with successfully`() {
        val user = UserSampler.sample()
        val sessionId = ULID.random()

        every { userRepository.existsByEmail(any()) } returns false
        every { userRepository.findById(any()) } returns Optional.empty()
        every { userRepository.updateEmail(any(), any()) } just runs

        assertThrows<UserNotFoundException> {
            userService.updateEmail(user.id, user.email, sessionId)
        }

        verify { userRepository.findById(user.id) }
        verify { userRepository.existsByEmail(user.email) }
        verify(exactly = 0) { userRepository.updateEmail(user.id, user.email) }
    }

    @Test
    fun `given an email to update, when email is in use, should throw AlreadyInUseException`() {
        val user = UserSampler.sample()
        val sessionId = ULID.random()

        every { userRepository.existsByEmail(any()) } returns true
        every { userRepository.updateEmail(any(), any()) } just runs

        assertThrows<AlreadyInUseException> {
            userService.updateEmail(user.id, user.email, sessionId)
        }

        verify { userRepository.existsByEmail(user.email) }
        verify(exactly = 0) { userRepository.updateEmail(user.id, user.email) }
    }

    @Test
    fun `given find user, when user not exists in database, should throws UserNotFoundException`() {
        val userId = ULID.random()

        every { userRepository.findById(any()) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            userService.findById(userId)
        }

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `given an user email, when find user successfully, should return him`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns user

        userService.findByEmail(user.email)

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given an user email, when user not exists, should throw UserNotFoundException`() {
        val user = UserSampler.sample()

        every { userRepository.findByEmail(any()) } returns null

        assertThrows<UserNotFoundException> {
            userService.findByEmail(user.email)
        }

        verify { userRepository.findByEmail(user.email) }
    }

    @Test
    fun `given a password to redefine, when the actual password matches, should update successfully`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()
        val newPassword = "12345678"

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.revokeAllExceptCurrent(any(), any()) } just runs
        every { userRepository.updatePassword(any(), any()) } just runs

        userService.updatePasswordWithActual("1234657", newPassword, user.id, session.id, device)

        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify { userRepository.updatePassword(user.id, any()) }
    }

    @Test
    fun `given a password to redefine, when the actual password not matches, should throw BadCredentialsException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every { sessionService.revokeAllExceptCurrent(any(), any()) } just runs
        every { userRepository.save(any()) } returns user

        assertThrows<BadCredentialsException> {
            userService.updatePasswordWithActual("1234658", "12345678", user.id, session.id, device)
        }

        verify { userRepository.findById(user.id) }
        verify(exactly = 0) { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `given a password to redefine, when fail to revoke the sessions, should throw PersistenceException`() {
        val user = UserSampler.sample()
        val session = SessionSampler.sample()
        val device = DeviceSampler.sample()

        every { userRepository.findById(any()) } returns Optional.of(user)
        every {
            sessionService.revokeAllExceptCurrent(
                any(),
                any()
            )
        } throws PersistenceException("Unable to delete sessions")
        every { userRepository.save(any()) } returns user

        assertThrows<PersistenceException> {
            userService.updatePasswordWithActual("1234657", "12345678", user.id, session.id, device)
        }

        verify { userRepository.findById(user.id) }
        verify { sessionService.revokeAllExceptCurrent(session.id, user.id) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

}