package com.dicoding.heartalert2.api

import com.dicoding.heartalert2.HospitalResponse
import com.dicoding.heartalert2.LocationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("articles")
    suspend fun getArticles(): Response<ArticleResponse>

    @GET("articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Response<ArticlesItem>

    @POST("hospital")
    suspend fun getHospitals( @Body locationRequest: LocationRequest): HospitalResponse

    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body otpRequest: OtpRequest): Response<VerifyOtpResponse>

    @POST("resend-otp")
    suspend fun resendOtp(@Body data: Map<String, String>): Response<VerifyOtpResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body email: Map<String, String>): Response<ForgotPasswordResponse>

    @GET("profile")
    suspend fun getUserProfile(): Response<UserResponse>
}