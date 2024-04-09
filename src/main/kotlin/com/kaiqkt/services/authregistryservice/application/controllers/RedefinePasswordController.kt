package com.kaiqkt.services.authregistryservice.application.controllers

import com.kaiqkt.services.authregistryservice.domain.services.RedefinePasswordService
import com.kaiqkt.services.authregistryservice.generated.application.controllers.RedefinePasswordApi
import com.kaiqkt.services.authregistryservice.generated.application.dto.RedefinePasswordV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.SendRedefinePasswordV1
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class RedefinePasswordController(
    private val redefinePasswordService: RedefinePasswordService
) : RedefinePasswordApi {

    override fun redefinePassword(redefinePasswordV1: RedefinePasswordV1): ResponseEntity<Unit> {
        redefinePasswordService.redefinePassword(redefinePasswordV1.code, redefinePasswordV1.newPassword)

        return ResponseEntity.noContent().build()
    }


    override fun sendCodeEmail(sendRedefinePasswordV1: SendRedefinePasswordV1): ResponseEntity<Unit> {
        redefinePasswordService.sendRedefineCodeEmail(sendRedefinePasswordV1.email)

        return ResponseEntity.noContent().build()
    }

    override fun validateCode(code: String): ResponseEntity<Unit> {
        redefinePasswordService.validateCode(code, true)

        return ResponseEntity.noContent().build()
    }
}