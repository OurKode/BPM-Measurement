package com.dicoding.heartalert2.API

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
	val id: Int,
	val title: String,
	val image_url: String,
	val content: String,
	val reference_link: String,
	val created_at: String,
	val updated_at: String
) : Parcelable