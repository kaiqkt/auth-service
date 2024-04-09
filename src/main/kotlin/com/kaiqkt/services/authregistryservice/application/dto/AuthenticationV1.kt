package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.domain.entities.Authentication
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationV1

fun Authentication.toV1() = AuthenticationV1(
    accessToken = this.accessToken,
    refreshToken = this.refreshToken,
    userId = this.userId
)