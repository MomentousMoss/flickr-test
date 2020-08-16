package com.momentousmoss.flickr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

open class JsonApiObject() {
    override fun toString(): String {
        return gson.toJson(this)
    }

    companion object {
        val gson: Gson
            get() = GsonBuilder().registerTypeAdapter(
                Date::class.java,
                Date()
            ).create()
    }
}
