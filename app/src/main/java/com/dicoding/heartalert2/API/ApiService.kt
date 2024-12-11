package com.dicoding.heartalert2.API

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("articles")
    suspend fun getArticles(): Response<ArticleResponse>
}

data class ArticleResponse(
    val articles: List<Article>
)