package com.momentousmoss.flickr

import android.net.Uri
import android.util.Xml
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.InputStreamReader
import java.net.URL

@Suppress("DEPRECATION")
class SearchService {
    private val utf8 = Xml.Encoding.UTF_8.name

    fun requestSearch(tags: String, page: Int): JsonService.Photos? {
        val searchUrl = getSearchUrl(tags, page)
        return try {
            val searchInputStream = searchUrl.openConnection().getInputStream()
            val searchJson =
                JsonParser().parse(InputStreamReader(searchInputStream, utf8)) as JsonObject
            Gson().fromJson(searchJson, JsonService.PhotosSearchResponse::class.java).photos
        } catch (e: Exception) {
            null
        }
    }

    private fun getSearchUrl(tags: String, page: Int): URL {
        val scheme = "https"
        val authority = "www.flickr.com"
        val servicesPath = "services"
        val restPath = "rest"
        val photosSearchJson = getPhotosSearchJson(tags, page)
        val urlBuilder = Uri.Builder()
            .scheme(scheme)
            .authority(authority)
            .appendPath(servicesPath)
            .appendPath(restPath)
            .appendQueryParameter(JsonService.PhotosSearchRequest::method.name, JsonService.PhotosSearchRequest().method)
            .appendQueryParameter(JsonService.PhotosSearchRequest::api_key.name, photosSearchJson.api_key)
            .appendQueryParameter(JsonService.PhotosSearchRequest::tags.name, photosSearchJson.tags)
            .appendQueryParameter(JsonService.PhotosSearchRequest::per_page.name, photosSearchJson.per_page.toString())
            .appendQueryParameter(JsonService.PhotosSearchRequest::page.name, photosSearchJson.page.toString())
            .appendQueryParameter(JsonService.PhotosSearchRequest::format.name, photosSearchJson.format)
            .appendQueryParameter(JsonService.PhotosSearchRequest::nojsoncallback.name, (if (photosSearchJson.nojsoncallback) 1 else 0).toString())
        return URL(urlBuilder.build().toString())
    }

    private fun getPhotosSearchJson(tags: String, page: Int): JsonService.PhotosSearchRequest {
        return JsonService.PhotosSearchRequest().apply {
            api_key = MainActivity.apiKey
            this.tags = tags
            per_page = MainActivity.pageSize
            this.page = page
        }
    }
}