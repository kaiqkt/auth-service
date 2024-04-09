package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.application.dto.RedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.application.dto.SendRedefinePasswordV1Sampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.services.RedefinePasswordService
import com.kaiqkt.services.authregistryservice.domain.utils.randomSixCharNumber
import io.azam.ulidj.ULID
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

internal class RedefinePasswordControllerTest {

    private val redefinePasswordService: RedefinePasswordService = mockk(relaxed = true)
    private val redefinePasswordController: RedefinePasswordController = RedefinePasswordController(redefinePasswordService)

    @Test
    fun `given a redefine code and the new password, when the user exist and the code matches, should persist the new password and return http status 204`() {
        val code = randomSixCharNumber()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.redefinePassword(any(), any()) } just runs

        val response = redefinePasswordController.redefinePassword(request)

        verify { redefinePasswordService.redefinePassword(request.code, request.newPassword) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a request to redefine password, when the user exist, should send the email with the code to redefine the password and return http status 204`() {
        val request = SendRedefinePasswordV1Sampler.emailSample()

        every { redefinePasswordService.sendRedefineCodeEmail(any()) } just runs

        val response = redefinePasswordController.sendCodeEmail(request)

        verify { redefinePasswordService.sendRedefineCodeEmail(request.email) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a redefine code to validate, when exists and is not expired, should return http status 204`() {
        val code = randomSixCharNumber()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.validateCode(any(), any()) } returns ULID.random()

        val response = redefinePasswordController.validateCode(request.code)

        verify { redefinePasswordService.validateCode(request.code, true) }

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `given a redefine code to validate, when not exists, should throw InvalidRedefinePasswordException`() {
        val code = randomSixCharNumber()
        val request = RedefinePasswordV1Sampler.sample(code)

        every { redefinePasswordService.validateCode(any(), any()) } throws InvalidRedefinePasswordException()

        assertThrows<InvalidRedefinePasswordException> {
            redefinePasswordController.validateCode(request.code)
        }

        verify { redefinePasswordService.validateCode(request.code, true) }
    }
}