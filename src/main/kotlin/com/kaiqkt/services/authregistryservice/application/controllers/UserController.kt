package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.commons.security.auth.AUTHORIZE_SERVICE
import com.kaiqkt.commons.security.auth.AUTHORIZE_USER
import com.kaiqkt.commons.security.auth.getSessionId
import com.kaiqkt.commons.security.auth.getUserId
import com.kaiqkt.services.authregistryservice.application.dto.toDomain
import com.kaiqkt.services.authregistryservice.application.dto.toV1
import com.kaiqkt.services.authregistryservice.domain.entities.Device
import com.kaiqkt.services.authregistryservice.domain.services.UserService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.UserApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.AuthenticationV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.EmailV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.NewPasswordV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.NewUserV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) : UserApi {

    @PreAuthorize(AUTHORIZE_USER)
    override fun updateEmail(emailV1: EmailV1): ResponseEntity<Unit> {
        userService.updateEmail(getUserId(), emailV1.email, getSessionId())
            .also { return ResponseEntity.noContent().build() }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun updatePassword(
        userAgent: String,
        appVersion: String,
        newPasswordV1: NewPasswordV1
    ): ResponseEntity<Unit> {
        val device = Device(userAgent, appVersion)

        userService.updatePasswordWithActual(
            newPasswordV1.actualPassword,
            newPasswordV1.newPassword,
            getUserId(),
            getSessionId(),
            device
        )

        return ResponseEntity.noContent().build()
    }

    @PreAuthorize(AUTHORIZE_SERVICE)
    override fun findById(userId: String): ResponseEntity<UserV1> {
        userService.findById(userId).also { return ResponseEntity.ok(it.toV1()) }
    }

    @PreAuthorize(AUTHORIZE_USER)
    override fun findByAccessToken(): ResponseEntity<UserV1> {
        userService.findById(getUserId()).also { return ResponseEntity.ok(it.toV1()) }
    }

    override fun create(
        userAgent: String,
        appVersion: String,
        newUserV1: NewUserV1
    ): ResponseEntity<AuthenticationV1> {
        val device = Device(userAgent, appVersion)
        userService.create(device, newUserV1.toDomain())
            .toV1()
            .also { return ResponseEntity(it, HttpStatus.CREATED) }
    }
}