package com.dicoding.heartalert2.api

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)

data class OtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val message: String
)

data class MessageResponse(
    @SerializedName("message")
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class UserResponse(
    val user: User
)

data class User(
    val id: Int,
    val fullname: String,
    val email: String
)