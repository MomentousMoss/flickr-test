package com.momentousmoss.flickr

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
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
    private var rv: RecyclerView? = null
    private var searchTags : String = String()
    private var lastScrollPosition : Int? = null
    private var applyScroll : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        loadNewPage(DEFAULT_SEARCH_PAGE)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        lastScrollPosition = glm.findFirstCompletelyVisibleItemPosition();
        applyScroll = true
        changeGridColumnsCount(newConfig.orientation)
        super.onConfigurationChanged(newConfig)
        Log.i("My", "scroll to " + lastScrollPosition)
    }

    private fun changeGridColumnsCount(orientation: Int) {
        glm.spanCount =
            if (orientation == ORIENTATION_PORTRAIT) {
                gridColumnsPortrait
            } else {
                gridColumnsLandscape
            }
    }

    private lateinit var scrollListener: RecyclerView.OnScrollListener

    private fun initView() {
        photoAdapter = PhotoAdapter(photosList)
        rv = findViewById<View>(R.id.gridPhotos) as RecyclerView
        rv?.layoutManager = glm
        rv?.adapter = photoAdapter

        val searchBtn = findViewById<ImageButton>(R.id.buttonSearch)
        searchBtn.setOnClickListener {
            startNewSearch()
        }
        val searchTextView = findViewById<EditText>(R.id.searchTextView)
        searchTextView.setOnEditorActionListener { textView, action, keyEvent ->
            if ((action == EditorInfo.IME_ACTION_SEARCH)) {
                startNewSearch()
                true
            } else {
                false
            }
        }

        //initializing new downloads by scrolling
        scrollListener = object : RecyclerView.OnScrollListener() {
            var element_old_number = 0
            override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                if (applyScroll) {
                    rv?.scrollToPosition(lastScrollPosition ?: 0)
                    Log.i("My", "scrolled to " + lastScrollPosition)
                    applyScroll = false
                }
                val element_number = glm.findLastCompletelyVisibleItemPosition()
                if (element_number != element_old_number) element_old_number = element_number
                }
            }
        rv?.addOnScrollListener(scrollListener)
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

    private fun hideKeyboard(view: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadNewPage(page: Int) {
        thread {
            searchTags = findViewById<EditText>(R.id.searchTextView).text.toString()
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
            Log.i("My", "loaded added " + photoAdapter?.itemCount)
        }
    }
}
