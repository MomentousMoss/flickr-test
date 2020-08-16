package com.momentousmoss.flickr

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class Adapter(photos: MutableList<Photo?>) : RecyclerView.Adapter<Adapter.PhotoViewHolder>() {
    private var photosCount = 0
    private var photos: List<Photo?>? = null

    init {
        this.photos = photos
    }

    class PhotoViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var description: TextView = itemView.findViewById<View>(R.id.photoDescription) as TextView
        var image: ImageView = itemView.findViewById<View>(R.id.photoView) as ImageView
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PhotoViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.photo_layout, viewGroup, false)
        return PhotoViewHolder(v)
    }

    override fun onBindViewHolder(pokemonViewHolder: PhotoViewHolder, i: Int) {
        pokemonViewHolder.description.text = photos?.get(i)?.description
        pokemonViewHolder.image.setImageBitmap(photos?.get(i)?.bmp)
    }

    //get number of photos
    override fun getItemCount(): Int {
        photosCount = photos?.size ?: photosCount
        return photosCount
    }
}