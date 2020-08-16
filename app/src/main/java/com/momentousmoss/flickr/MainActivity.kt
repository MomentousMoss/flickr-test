package com.momentousmoss.flickr

import android.app.Activity
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

class MainActivity : Activity() {

    companion object {
        const val gridColumnsPortrait = 3
        const val gridColumnsLandscape = 4
    }

    private var photos: MutableList<Photo?> = mutableListOf()
    private var photoAdapter : Adapter? = null
    private var glm = GridLayoutManager(this, gridColumnsPortrait)
    private var rv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        changeGridColumnsCount()
        addNullItems()
    }

    private fun changeGridColumnsCount() {
        val displayMode = resources.configuration.orientation
        glm.spanCount =
            if (displayMode == ORIENTATION_PORTRAIT) {
                gridColumnsPortrait
            } else {
                gridColumnsLandscape
            }
    }

    private fun initView() {
        photoAdapter = Adapter(photos)
        rv = findViewById<View>(R.id.gridPhotos) as RecyclerView
        rv!!.layoutManager = glm
        rv?.adapter = photoAdapter
    }

    //TODO remove
    private fun addNullItems() {
        val nullPhoto = Photo(
            "AAA",
            BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher)
        )
        photos.add(nullPhoto)
        photos.add(nullPhoto)
        photos.add(nullPhoto)
        photos.add(nullPhoto)
        photos.add(nullPhoto)
    }
}
