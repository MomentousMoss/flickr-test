package com.momentousmoss.flickr

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : Activity() {

    companion object {
        const val gridColumnsPortrait = 3
        const val gridColumnsLandscape = 4
        const val photoSize = "m"
        const val photoFormat = ".jpg"
        const val apiKey = "a155ac73221cb61239deb9db88614f22"
        const val pageSize = 20
    }

    private var photosList: MutableList<Photo?> = mutableListOf()
    private var photoAdapter : PhotoAdapter? = null
    private var glm = GridLayoutManager(this, gridColumnsPortrait)
    private var gridView: RecyclerView? = null
    private var searchTags : String = String()
    private var searchPage : Int = DEFAULT_SEARCH_PAGE
    private var lastScrollPosition : Int = 0
    private var applyScroll : Boolean = false
    private var wasScrolled : Boolean = false
    private var gridScrollY : Int = 0 //vertical scroll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        loadNewPage(DEFAULT_SEARCH_PAGE)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (wasScrolled) {
            wasScrolled = false
            lastScrollPosition = glm.findFirstCompletelyVisibleItemPosition()
            applyScroll = true
        }
        changeGridColumnsCount(newConfig.orientation)
        super.onConfigurationChanged(newConfig)
    }

    private fun changeGridColumnsCount(orientation: Int) {
        glm.spanCount =
            if (orientation == ORIENTATION_PORTRAIT) {
                gridColumnsPortrait
            } else {
                gridColumnsLandscape
            }
    }

    private fun initView() {
        photoAdapter = PhotoAdapter(photosList)
        gridView = findViewById<View>(R.id.gridPhotos) as RecyclerView
        gridView?.layoutManager = glm
        gridView?.adapter = photoAdapter
        gridView?.addOnScrollListener(scrollListener)

        gridView?.addOnItemTouchListener(
            RecyclerItemClickListener(this, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    fillFullPhoto(view)
                }
            })
        )

        val searchBtn = findViewById<ImageButton>(R.id.buttonSearch)
        searchBtn.setOnClickListener {
            startNewSearch()
        }

        //to work with search button in keyboard
        val searchTextView = findViewById<EditText>(R.id.searchTextView)
        searchTextView.setOnEditorActionListener { _, action, _ ->
            if ((action == EditorInfo.IME_ACTION_SEARCH)) {
                startNewSearch()
                true
            } else {
                false
            }
        }

        val photoFullView = findViewById<View>(R.id.photoLayoutFull)
        photoFullView.setOnClickListener {
            hideFullPhoto()
        }
    }

    private fun fillFullPhoto(clickedItemView: View?) {
        if (gridView != null && clickedItemView != null) {
            val itemBitmap = ((clickedItemView.findViewById<ImageView>(R.id.photoView)).drawable as BitmapDrawable).bitmap
            val itemTitle = (clickedItemView.findViewById<TextView>(R.id.photoTitle)).text
            findViewById<ImageView>(R.id.photoViewFull).setImageBitmap(itemBitmap)
            findViewById<TextView>(R.id.photoTitleFull).text = itemTitle

            showFullPhoto()
        }
    }

    private fun showFullPhoto() {
        findViewById<View>(R.id.photoLayoutFull).visibility = View.VISIBLE
    }

    private fun hideFullPhoto() {
        findViewById<View>(R.id.photoLayoutFull).visibility = View.INVISIBLE
    }

    private fun startNewSearch() {
        val searchTextView = findViewById<EditText>(R.id.searchTextView)
        if (searchTextView.text?.toString() != searchTags) {
            hideKeyboard(searchTextView)
            photosList.clear()
            photoAdapter?.notifyDataSetChanged()
            loadNewPage(DEFAULT_SEARCH_PAGE)
        }
    }

    private fun loadNewPage(page: Int) {
        thread {
            searchTags = findViewById<EditText>(R.id.searchTextView).text.toString()
            searchPage = page
            if (searchTags.isNotEmpty()) {
                val photos = SearchService().requestSearch(searchTags, page)
                if (photos?.photo != null) {
                    for (jsonPhoto in photos.photo!!) {
                        addPhoto(PhotosService().requestPhoto(jsonPhoto))
                    }
                }
            }
        }
    }

    private fun addPhoto(photo: Photo) {
        runOnUiThread {
            photosList.add(photo)
            photoAdapter?.notifyItemInserted(photoAdapter?.itemCount ?: 0)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(gridView: RecyclerView, dx: Int, dy: Int) {
            val lastVisibleItemPosition = glm.findLastCompletelyVisibleItemPosition() + 1
            val requestedItemsSize = searchPage * pageSize
            val photosListSize = photosList.size
            if (lastVisibleItemPosition == requestedItemsSize && photosListSize == lastVisibleItemPosition) {
                searchPage++
                loadNewPage(searchPage)
            }

            if (applyScroll) {
                gridScrollY = dy
                applyScroll = false
                gridView.scrollToPosition(lastScrollPosition)

                //sometimes scroll have bug with position using Handler will fix that
                Handler().post {
                    gridView.adapter?.notifyDataSetChanged()
                }
            } else if (gridScrollY != dy) {
                //used to catch real scroll (on orientation change - scroll without dy change)
                gridScrollY = dy
                wasScrolled = true
            }
        }
    }
}
