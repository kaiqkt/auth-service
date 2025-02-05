package com.kaiqkt.services.authregistryservice.application.handler

import com.kaiqkt.services.authregistryservice.application.dto.ErrorSampler
import com.kaiqkt.services.authregistryservice.domain.exceptions.AlreadyInUseException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadCredentialsException
import com.kaiqkt.services.authregistryservice.domain.exceptions.BadRefreshTokenException
import com.kaiqkt.services.authregistryservice.domain.exceptions.DomainException
import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.domain.exceptions.InvalidRedefinePasswordException
import com.kaiqkt.services.authregistryservice.domain.exceptions.SessionNotFoundException
import com.kaiqkt.services.authregistryservice.domain.exceptions.UserNotFoundException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.MapBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest


class ErrorHandlerTest {

    private val webRequest: WebRequest = mockk(relaxed = true)

    @Test
    fun `given an DomainException, when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sample()
        val exception = DomainException(ErrorType.valueOf(error.type), error.message)

        val response = errorHandler.handleDomainException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an BadCredentialsException when handling, should return HTTP status 403`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleBadCredentialsError()
        val exception = BadCredentialsException()

        val response = errorHandler.handleBadCredentialsException(exception)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an UserNotFoundException when handling, should return HTTP status 404`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleUserNotFoundError()
        val exception = UserNotFoundException()

        val response = errorHandler.handleUserNotFoundException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an AlreadyInUseException when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleAlreadyInUseError()
        val exception = AlreadyInUseException(ErrorType.EMAIL_IN_USE)

        val response = errorHandler.handleAlreadyInUseException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an BadRefreshTokenException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleRefreshTokenError()
        val exception = BadRefreshTokenException()

        val response = errorHandler.handleBadRefreshTokenException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an SessionNotFoundException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleSessionNotFoundError()
        val exception = SessionNotFoundException()

        val response = errorHandler.handleSessionNotFoundException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an InvalidRedefinePasswordException when handling, should return HTTP status 401`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleRedefinePasswordCodeNotFoundException()
        val exception = InvalidRedefinePasswordException()

        val response = errorHandler.handleInvalidRedefinePasswordException(exception, webRequest)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals(error.type, response.body?.type)
        Assertions.assertEquals(error.message, response.body?.message)
    }

    @Test
    fun `given an MethodArgumentNotValidException when handling, should return HTTP status 400`() {
        val errorHandler = ErrorHandler()
        val error = ErrorSampler.sampleMethodArgumentNotValidError()

        val parameter: MethodParameter = mockk(relaxed = true)

        val headers = HttpHeaders()
        headers.add("test", "test")

        val bindingResult: BindingResult = MapBindingResult(mapOf<String, String>(), "objectName")
        bindingResult.addError(FieldError("objectName", "email",  "must match \"\\S+@\\S+\\.\\S+\""))

        val exception = MethodArgumentNotValidException(parameter, bindingResult)

        val response = errorHandler.handleMethodArgumentNotValid(
            exception,
            headers,
            HttpStatus.OK,
            webRequest
        )

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals(error, response.body)
    }


}