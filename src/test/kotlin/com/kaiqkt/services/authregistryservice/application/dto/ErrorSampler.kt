package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.exceptions.ErrorType
import com.kaiqkt.services.authregistryservice.generated.application.dto.ErrorV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.GenericErrorV1

object ErrorSampler {
    fun sample() = ErrorV1(
        type = ErrorType.USER_NOT_FOUND.name,
        message = "test"
    )

    fun sampleBadCredentialsError() = ErrorV1(
        type = ErrorType.INCORRECT_PASSWORD.name,
        message = "Incorrect password"
    )

    fun sampleUserNotFoundError() = ErrorV1(
        type = ErrorType.USER_NOT_FOUND.name,
        message = "User not found"
    )
    
    fun sampleAlreadyInUseError() = ErrorV1(
        type = ErrorType.EMAIL_IN_USE.name,
        message = "Already in use"
    )

    fun sampleRefreshTokenError() = ErrorV1(
        type = ErrorType.REFRESH_TOKEN_INCORRECT.name,
        message = "Incorrect refresh token"
    )

    fun sampleSessionNotFoundError() = ErrorV1(
        type = ErrorType.SESSION_NOT_FOUND.name,
        message = "Session not found"
    )

    fun sampleRedefinePasswordCodeNotFoundException() = ErrorV1(
        type = ErrorType.INVALID_REDEFINE_PASSWORD_CODE.name,
        message = "Redefine password code not exist or is expired"
    )

    fun sampleMethodArgumentNotValidError() = GenericErrorV1(
        details = mapOf("email" to "must match \"\\S+@\\S+\\.\\S+\"")
    )
}