package com.a13897.mip2.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageItem(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val download_url: String
)
