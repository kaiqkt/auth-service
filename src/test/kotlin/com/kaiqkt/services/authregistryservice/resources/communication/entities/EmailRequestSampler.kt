package com.kaiqkt.services.authregistryservice.resources.communication.entities

object EmailRequestSampler {
    fun emailUpdateTemplate() = Email(
        subject = "Alteração de email em sua conta",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/email-updated.html",
            data = mapOf("code" to "1234")
        )
    )

    fun passwordRedefineEmailSample() = Email(
        subject = "Solicitação de redefinição de senha",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/redefine-password.html",
            data = mapOf("code" to "1234")
        )
    )

    fun welcomeEmailSample() = Email(
        subject = "Bem-vindo(a) à nossa plataforma",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/welcome.html",
            data = mapOf("name" to "shinji")
        )
    )

    fun passwordUpdatedSample() = Email(
        subject = "Sua senha foi alterada com sucesso",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/password-updated.html",
            data = mapOf("name" to "shinji")
        )
    )

    fun newAccessSample() = Email(
        subject = "Novo login em sua conta",
        recipient = "shinji@eva01.com",
        template = Template(
            url = "s3://communication-d-1/emails/new-access.html",
            data = mapOf("name" to "shinji")
        )
    )
}