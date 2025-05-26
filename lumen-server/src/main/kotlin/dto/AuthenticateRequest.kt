package me.bottdev.lumenserver.dto

data class AuthenticateRequest(
    val username: String = "",
    val password: String = ""
) {
    fun isBlank(): Boolean = username == "" && password == ""
}
