package com.kaiqkt.services.authregistryservice.domain.entities

data class Authentication(
    val userId: String,
    val accessToken: String,
    val refreshToken: String
)
