package com.kaiqkt.services.authregistryservice.application.dto

import com.kaiqkt.commons.crypto.encrypt.EncryptUtils
import com.kaiqkt.services.authregistryservice.domain.entities.User
import com.kaiqkt.services.authregistryservice.generated.application.dto.NewUserV1
import com.kaiqkt.services.authregistryservice.generated.application.dto.UserV1

fun NewUserV1.toDomain() = User(
    fullName = this.fullName,
    email = this.email,
    password = EncryptUtils.encryptPassword(this.password),
)

fun User.toV1() = UserV1(
    fullName = this.fullName,
    email = this.email,
)