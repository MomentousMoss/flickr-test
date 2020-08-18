package com.momentousmoss.flickr

import com.google.gson.annotations.SerializedName

const val METHOD_SEARCH = "flickr.photos.search"
const val DEFAULT_SEARCH_PERPAGE = 1
const val DEFAULT_SEARCH_PAGE = 1
const val DEFAULT_SEARCH_FORMAT = "json"
const val DEFAULT_SEARCH_NOJSONCALLBACK = true

open class JsonService {
    class PhotosSearchRequest : JsonApiObject() {
        val method : String = METHOD_SEARCH
        @SerializedName("api_key")
        var api_key : String? = null
        var text : String? = null
        @SerializedName("per_page")
        var per_page : Int = DEFAULT_SEARCH_PERPAGE
        var page : Int = DEFAULT_SEARCH_PAGE
        var format : String? = DEFAULT_SEARCH_FORMAT
        @SerializedName("nojsoncallback")
        var nojsoncallback : Boolean = DEFAULT_SEARCH_NOJSONCALLBACK
    }

    class PhotosSearchResponse : JsonApiObject()  {
        var photos: Photos? = null
        var stat: String? = null
    }

    class Photos : JsonApiObject() {
        var page : Int = 0
        var pages : Int = 0
        @SerializedName("per_page")
        var per_page : Int = 0
        var total : Int = 0
        var photo: List<Photo>? = null
    }

    class Photo : JsonApiObject() {
        var id : String? = null
        var owner : String? = null
        var secret : String? = null
        var server : Int = 0
        var farm : Int = 0
        var title : String? = null
        @SerializedName("ispublic")
        var ispublic : Int = 0
        @SerializedName("isfriend")
        var isfriend : Int = 0
        @SerializedName("isfamily")
        var isfamily : Int = 0
    }
}