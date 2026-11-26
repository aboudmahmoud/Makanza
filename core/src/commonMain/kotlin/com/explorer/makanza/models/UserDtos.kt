package com.explorer.makanza.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.STUDENT,
    val createdAt: String
)

@Serializable
sealed class AuthRequest {
    @Serializable
    data class Register(
        val fullName: String,
        val email: String,
        val phone: String,
        val password: String,
        val confirmPassword: String,
        val role: UserRole = UserRole.STUDENT
    )

    @Serializable
    data class Login(
        val email: String,
        val password: String
    )
}

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String,
    val role: UserRole = UserRole.STUDENT
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class UpdateProfileRequest(
    val fullName: String,
    val phone: String
)
