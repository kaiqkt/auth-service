package com.kaiqkt.services.authregistryservice.resources.communication.entities

enum class NotificationType(val title: String) {
    WELCOME_TEMPLATE("Bem-vindo(a) à nossa plataforma"),
    NEW_ACCESS_TEMPLATE("Novo login em sua conta"),
    EMAIL_UPDATED_TEMPLATE("Alteração de email em sua conta"),
    PASSWORD_REDEFINE_PASSWORD_TEMPLATE("Solicitação de redefinição de senha"),
    PASSWORD_UPDATED_TEMPLATE("Sua senha foi alterada com sucesso")
}