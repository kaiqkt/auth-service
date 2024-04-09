package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.services.authregistryservice.generated.application.dto.SendRedefinePasswordV1

object SendRedefinePasswordV1Sampler {
    fun emailSample() = SendRedefinePasswordV1(
        email = "shinji@eva01.com"
    )
}