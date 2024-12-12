package com.dicoding.heartalert2.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("articles")
    suspend fun getArticles(): Response<ArticleResponse>

    @GET("articles/{id}")
    suspend fun getArticleById(@Path("id") id: Int): Response<ArticlesItem>
}