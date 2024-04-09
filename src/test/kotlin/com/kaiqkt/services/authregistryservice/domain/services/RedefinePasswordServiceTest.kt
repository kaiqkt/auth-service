package com.kaiqkt.services.authregistryservice.domain.services

import com.kaiqkt.services.authregistryservice.domain.entities.UserSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import com.kaiqkt.services.authregistryservice.domain.repositories.RedefinePasswordRepository
import com.kaiqkt.services.authregistryservice.domain.utils.randomSixCharNumber
import com.kaiqkt.services.authregistryservice.resources.exceptions.PersistenceException
import io.azam.ulidj.ULID
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RedefinePasswordServiceTest {
    private val redefinePasswordRepository: RedefinePasswordRepository = mockk(relaxed = true)
    private val sessionService: SessionService = mockk(relaxed = true)
    private val userService: UserService = mockk(relaxed = true)
    private val emailService: EmailService = mockk(relaxed = true)
    private val redefinePasswordService: RedefinePasswordService = RedefinePasswordService(
        redefinePasswordRepository,
        userService,
        sessionService,
        emailService
    )

    @Test
    fun `given a request o generate password redefine code, should send a email with him`() {
        val user = UserSampler.sample()

        every { userService.findByEmail(any()) } returns user
        every { redefinePasswordRepository.save(any(), any()) } just runs
        every { emailService.sendRedefinePasswordEmail(any(), any()) } just runs

        redefinePasswordService.sendRedefineCodeEmail(user.email)

        verify { userService.findByEmail(user.email) }
        verify { redefinePasswordRepository.save(any(), user.id) }
        verify { emailService.sendRedefinePasswordEmail(any(), any()) }
    }

    @Test
    fun `given a request o generate password redefine code, when the user not exist, should throw UserNotFoundException`() {
        val user = UserSampler.sample()

        every { userService.findByEmail(any()) } throws UserNotFoundException()
        every { redefinePasswordRepository.save(any(), any()) } just runs
        every { emailService.sendRedefinePasswordEmail(any(), any()) } just runs

        assertThrows<UserNotFoundException> {
            redefinePasswordService.sendRedefineCodeEmail(user.email)
        }

        verify { userService.findByEmail(user.email) }
        verify(exactly = 0) { redefinePasswordRepository.save(any(), user.id) }
        verify(exactly = 0) { emailService.sendRedefinePasswordEmail(any(), any()) }
    }

    @Test
    fun `given password redefine code and new password, should update the password and send password updated email`() {
        val code = randomSixCharNumber()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordRepository.findByCode(any()) } returns user.id
        every { redefinePasswordRepository.delete(any()) } just runs
        every { userService.findById(any()) } returns user
        every { userService.updatePassword(any(), any()) } just runs
        every { sessionService.revokeAll(any()) } just runs
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        redefinePasswordService.redefinePassword(code, password)

        verify { redefinePasswordRepository.findByCode(code) }
        verify { userService.findById(user.id) }
        verify { userService.updatePassword(user.id, password) }
        verify { redefinePasswordRepository.delete(code) }
        verify { sessionService.revokeAll(user.id) }
        verify { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password redefine code and new password, when not find the code, should throw InvalidRedefinePasswordException`() {
        val code = randomSixCharNumber()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordRepository.findByCode(any()) } returns null
        every { redefinePasswordRepository.delete(any()) } just runs
        every { userService.findById(any()) } returns user
        every { userService.updatePassword(any(), any()) } just runs
        every { sessionService.revokeAll(any()) } just runs
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordService.redefinePassword(code, password)
        }

        verify { redefinePasswordRepository.findByCode(code) }
        verify(exactly = 0) { userService.findById(user.id) }
        verify(exactly = 0) { userService.updatePassword(user.id, password) }
        verify(exactly = 0) { redefinePasswordRepository.delete(code) }
        verify(exactly = 0) { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password redefine code and new password, when not find the user, should throw UserNotFoundException`() {
        val code = randomSixCharNumber()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordRepository.findByCode(any()) } returns user.id
        every { redefinePasswordRepository.delete(any()) } just runs
        every { userService.findById(any()) } throws UserNotFoundException()
        every { userService.updatePassword(any(), any()) } just runs
        every { sessionService.revokeAll(any()) } just runs
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs


        assertThrows<UserNotFoundException> {
            redefinePasswordService.redefinePassword(code, password)
        }

        verify { redefinePasswordRepository.findByCode(code) }
        verify { userService.findById(user.id) }
        verify { redefinePasswordRepository.delete(code) }
        verify(exactly = 0) { userService.updatePassword(user.id, password) }
        verify(exactly = 0) { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given password redefine code and new password, when fail to revoke the sessions, should throw PersistenceException`() {
        val code = randomSixCharNumber()
        val password = "123456"
        val user = UserSampler.sample()

        every { redefinePasswordRepository.findByCode(any()) } returns user.id
        every { redefinePasswordRepository.delete(any()) } just runs
        every { userService.findById(any()) } returns user
        every { userService.updatePassword(any(), any()) } just runs
        every { sessionService.revokeAll(any()) } throws PersistenceException("Unable to delete all sessions")
        every { emailService.sendPasswordUpdatedEmail(any()) } just runs

        assertThrows<PersistenceException> {
            redefinePasswordService.redefinePassword(code, password)
        }

        verify { redefinePasswordRepository.findByCode(code) }
        verify { userService.findById(user.id) }
        verify { redefinePasswordRepository.delete(code) }
        verify { sessionService.revokeAll(user.id) }
        verify(exactly = 0) { userService.updatePassword(user.id, password) }
        verify(exactly = 0) { emailService.sendPasswordUpdatedEmail(user) }
    }

    @Test
    fun `given a code, when is not exist, should throw InvalidRedefinePasswordException`() {
        val redefinePasswordCode = randomSixCharNumber()

        every { redefinePasswordRepository.findByCode(any()) } returns null

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordService.validateCode(redefinePasswordCode)
        }

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist and is expired, should throw InvalidRedefinePasswordException and delete him`() {
        val redefinePasswordCode = randomSixCharNumber()

        every { redefinePasswordRepository.findByCode(any()) } returns null
        every { redefinePasswordRepository.delete(any()) } just runs

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordService.validateCode(redefinePasswordCode)
        }

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist, is not expired and is not validation request, should delete and return the user who use this code`() {
        val redefinePasswordCode = randomSixCharNumber()
        val userId = ULID.random()

        every { redefinePasswordRepository.findByCode(any()) } returns userId
        every { redefinePasswordRepository.delete(any()) } just runs

        redefinePasswordService.validateCode(redefinePasswordCode)

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
        verify { redefinePasswordRepository.delete(redefinePasswordCode) }
    }

    @Test
    fun `given a code, when exist, is not expired and is validation request, should return the user who use this code`() {
        val redefinePasswordCode = randomSixCharNumber()
        val userId = ULID.random()

        every { redefinePasswordRepository.findByCode(any()) } returns userId

        redefinePasswordService.validateCode(redefinePasswordCode, true)

        verify { redefinePasswordRepository.findByCode(redefinePasswordCode) }
    }
}