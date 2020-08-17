package com.momentousmoss.flickr

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
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
    private var gridLayoutManager = GridLayoutManager(this, gridColumnsPortrait)
    private var searchTags : String = String()
    private var searchPage : Int = DEFAULT_SEARCH_PAGE
    private var lastScrollPosition : Int = 0
    private var applyScroll : Boolean = false
    private var wasScrolled : Boolean = false
    private var recyclerScrollY : Int = 0 //vertical scroll

    private lateinit var searchTextView : EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBtn: ImageButton
    private lateinit var photoLayoutFull: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        photoAdapter = PhotoAdapter(photosList)
        initView()
        addViewListeners()
        startNewSearch()
    }

    //save grid position and runs orientation check
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (wasScrolled) {
            wasScrolled = false
            lastScrollPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition()
            applyScroll = true
        }
        changeGridColumnsCount(newConfig.orientation)
        super.onConfigurationChanged(newConfig)
    }

    //update columns count in photoGrid
    private fun changeGridColumnsCount(orientation: Int) {
        gridLayoutManager.spanCount =
            if (orientation == ORIENTATION_PORTRAIT) {
                gridColumnsPortrait
            } else {
                gridColumnsLandscape
            }
    }

    private fun initView() {
        searchTextView = findViewById(R.id.searchTextView)
        searchBtn = findViewById(R.id.searchButton)
        photoLayoutFull = findViewById(R.id.photoFullInclude)
        recyclerView = findViewById(R.id.gridPhotos)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = photoAdapter
    }

    private fun addViewListeners() {
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(itemTouchListener())
        searchBtn.setOnClickListener { startNewSearch() }
        searchTextView.setOnEditorActionListener(editorActionListener)
        photoLayoutFull.setOnClickListener { hideFullPhoto() }
    }

    //start search for the first page
    private fun startNewSearch() {
        if (searchTextView.text?.toString() != searchTags) {
            hideKeyboard()
            photosList.clear()
            photoAdapter?.notifyDataSetChanged()
            searchTags = searchTextView.text.toString()
            loadNewPage(DEFAULT_SEARCH_PAGE)
        }
    }

    //request new search page and load photo from it
    private fun loadNewPage(page: Int) {
        thread {
            searchPage = page
            if (searchTags.isNotEmpty()) {
                val photos = SearchService().requestSearch(searchTags, page)
                if (photos?.photo != null) {
                    for (jsonPhoto in photos.photo!!) {
                        thread {
                            addPhoto(PhotosService().requestPhoto(jsonPhoto))
                        }
                    }
                }
            }
        }
    }

    //add item (photo and title) to list and update adapter
    private fun addPhoto(photo: Photo) {
        runOnUiThread {
            photosList.add(photo)
            photoAdapter?.notifyItemInserted(photoAdapter?.itemCount ?: 0)
        }
    }

    //fill full photo view
    private fun fillFullView(clickedItemView: View?) {
        if (clickedItemView != null) {
            val itemBitmap = getPhotoViewBitmap(clickedItemView)
            val itemTitle = getTitleViewText(clickedItemView)
            findViewById<ImageView>(R.id.photoViewFull).setImageBitmap(itemBitmap)
            findViewById<TextView>(R.id.photoTitleFull).text = itemTitle
            showFullPhoto()
        }
    }

    private fun getPhotoViewBitmap(clickedItemView: View): Bitmap? {
        return ((clickedItemView.findViewById<ImageView>(R.id.photoView)).drawable as BitmapDrawable).bitmap
    }

    private fun getTitleViewText(clickedItemView: View): CharSequence? {
        return (clickedItemView.findViewById<TextView>(R.id.photoTitle)).text
    }

    private fun showFullPhoto() {
        photoLayoutFull.visibility = View.VISIBLE
    }

    private fun hideFullPhoto() {
        photoLayoutFull.visibility = View.INVISIBLE
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchTextView.windowToken, 0)
    }

    //gets view from clicked position and send this data to fill
    private fun itemTouchListener() = RecyclerItemClickListener(this, object : RecyclerItemClickListener.OnItemClickListener {
        override fun onItemClick(view: View?, position: Int) {
            fillFullView(view)
        }
    })

    //to work with search button in keyboard
    private var editorActionListener = TextView.OnEditorActionListener { _, action, _ ->
        if ((action == EditorInfo.IME_ACTION_SEARCH)) {
            startNewSearch()
            true
        } else {
            false
        }
    }

    //scroll listener for recyclerView - for load new page and update position on config change
    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerViewLocal: RecyclerView, dx: Int, dy: Int) {
            val lastVisibleItemPosition = gridLayoutManager.findLastCompletelyVisibleItemPosition() + 1
            val requestedItemsSize = searchPage * pageSize
            val photosListSize = photosList.size
            //checked, that filled with current page
            if (lastVisibleItemPosition == requestedItemsSize && photosListSize == lastVisibleItemPosition) {
                searchPage++
                loadNewPage(searchPage)
            }
            //scroll to last position if needed
            if (applyScroll) {
                recyclerScrollY = dy
                applyScroll = false
                recyclerViewLocal.scrollToPosition(lastScrollPosition)
                //sometimes scroll have bug with position using Handler will fix that
                Handler().post { recyclerViewLocal.adapter?.notifyDataSetChanged() }
            } else if (recyclerScrollY != dy) {
                //used to catch real scroll (on orientation change - scroll without dY change)
                recyclerScrollY = dy
                wasScrolled = true
            }
        }
    }
}
