package com.a13897.mip2.repository

import com.a13897.mip2.api.ApiClient
import com.a13897.mip2.model.ImageItem

class ImageRepository {
    suspend fun getImages(page: Int = 1, limit: Int = 20): List<ImageItem> {
        return ApiClient.retrofit.getImages(page, limit)
    }
}
