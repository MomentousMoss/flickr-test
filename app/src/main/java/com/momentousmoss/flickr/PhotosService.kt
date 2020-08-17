package com.momentousmoss.flickr

import android.graphics.BitmapFactory
import android.net.Uri
import java.net.URL

class PhotosService {
    //requests image and return Photo with title and bitmap
    fun requestPhoto(jsonPhoto: JsonService.Photo): Photo {
        val photoUrl = getPhotoUrl(jsonPhoto)
        val photoBitmap =
            try {
                BitmapFactory.decodeStream(photoUrl.openConnection().getInputStream())
            } catch (e: Exception) {
                null
            }
        return Photo(
            jsonPhoto.title,
            photoBitmap
        )
    }

    //generate url base on photo json
    private fun getPhotoUrl(photo: JsonService.Photo): URL {
        val scheme = "https"
        val authority = "farm" + photo.farm + ".staticflickr.com"
        val serverPath = photo.server.toString()
        val photoPath = photo.id + "_" + photo.secret + "_" + MainActivity.photoSize + MainActivity.photoFormat
        val urlBuilder = Uri.Builder()
            .scheme(scheme)
            .authority(authority)
            .appendPath(serverPath)
            .appendPath(photoPath)
        return URL(urlBuilder.build().toString())
    }
}