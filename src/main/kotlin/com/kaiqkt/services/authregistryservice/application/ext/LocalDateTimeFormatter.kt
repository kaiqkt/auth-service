package com.kaiqkt.services.authregistryservice.application.ext

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.customFormatter(): String {
    val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    return pattern.format(this)
}