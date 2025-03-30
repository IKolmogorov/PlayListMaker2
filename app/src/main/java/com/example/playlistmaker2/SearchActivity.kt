package com.example.playlistmaker2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {

    private val itunesBaseUrl = "https://itunes.apple.com"
    var searchedText: String = EMPTY_TEXT
    var trackList: MutableList<Track> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val returnFrameLayout = findViewById<FrameLayout>(R.id.return_frame)
        val searchEditText = findViewById<EditText>(R.id.search_editText)
        val clearButton = findViewById<ImageView>(R.id.search_clear_imageView)
        val searchErrorRefreshButton = findViewById<Button>(R.id.search_error_refresh_button)

        val recyclerView = findViewById<RecyclerView>(R.id.trackList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(itunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val searchTrackService = retrofit.create(ItunesApi::class.java)

        clearButton.visibility = clearButtonVisibility(searchEditText.text)

        returnFrameLayout.setOnClickListener {
            val displayIntent = Intent(this, MainActivity::class.java)
            displayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(displayIntent)
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            showSearchResults(TrackSearchResultsType.EMPTY, recyclerView)
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTrackSearchResults(searchTrackService, searchEditText.text.toString(), recyclerView)
            }
            false
        }

        searchErrorRefreshButton.setOnClickListener {
            getTrackSearchResults(searchTrackService, searchEditText.text.toString(), recyclerView)
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                searchedText = s.toString()
            }
        }

        searchEditText.addTextChangedListener(searchTextWatcher)

    }

    fun showSearchResults(trackSearchResultsType: TrackSearchResultsType, recyclerView: RecyclerView){
        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.search_error_placeholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.search_no_results_placeholder)

        when (trackSearchResultsType){
            TrackSearchResultsType.SUCCESS -> {

                val trackAdapter = TrackAdapter(trackList)
                recyclerView.adapter = trackAdapter

                recyclerView.visibility = View.VISIBLE
                searchErrorPlaceholderFrame.visibility = View.GONE
                searchNoResultsPlaceholderFrame.visibility = View.GONE
            }
            TrackSearchResultsType.EMPTY -> {
                trackList.clear()
                val trackAdapter = TrackAdapter(ArrayList<Track>(0))
                recyclerView.adapter = trackAdapter

                recyclerView.visibility = View.VISIBLE
                searchErrorPlaceholderFrame.visibility = View.GONE
                searchNoResultsPlaceholderFrame.visibility = View.GONE
            }
            TrackSearchResultsType.NO_RESULTS -> {
                recyclerView.visibility = View.GONE
                searchErrorPlaceholderFrame.visibility = View.GONE
                searchNoResultsPlaceholderFrame.visibility = View.VISIBLE
            }
            TrackSearchResultsType.ERROR -> {
                recyclerView.visibility = View.GONE
                searchErrorPlaceholderFrame.visibility = View.VISIBLE
                searchNoResultsPlaceholderFrame.visibility = View.GONE
            }
        }
    }

    fun getTrackSearchResults(searchTrackService : ItunesApi, expression: String, recyclerView: RecyclerView) {
        searchTrackService
            .search(expression)
            .enqueue(object : Callback<SearchTrackResponse> {
                override fun onResponse(
                    call: Call<SearchTrackResponse>,
                    response: Response<SearchTrackResponse>
                ) {

                    when (response.code()) {
                        200 -> {
                            val searchTrackResults = response.body()?.results
                            trackList.clear()

                            if (searchTrackResults != null) {
                                if (searchTrackResults.size > 0) {
                                    trackList.addAll(searchTrackResults)

                                    showSearchResults(TrackSearchResultsType.SUCCESS, recyclerView)
                                } else {
                                    showSearchResults(TrackSearchResultsType.NO_RESULTS, recyclerView)
                                }
                            } else {
                                showSearchResults(TrackSearchResultsType.NO_RESULTS, recyclerView)
                            }
                        }
                        else -> {
                            showSearchResults(TrackSearchResultsType.ERROR, recyclerView)
                        }
                    }
                }

                override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                    showSearchResults(TrackSearchResultsType.ERROR, recyclerView)
                }
            })
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCHED_TEXT_KEY, searchedText)

        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.search_error_placeholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.search_no_results_placeholder)
        outState.putBoolean(SHOW_SEARCH_ERROR_PLACEHOLDER_KEY, searchErrorPlaceholderFrame.visibility == View.VISIBLE)
        outState.putBoolean(SHOW_NO_RESULTS_ERROR_PLACEHOLDER_KEY, searchNoResultsPlaceholderFrame.visibility == View.VISIBLE)

        outState.putString(TRACK_LIST_STRING_KEY, Gson().toJson(trackList))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchedText = savedInstanceState.getString(SEARCHED_TEXT_KEY, EMPTY_TEXT)
        val showSearchErrorPlaceHolder = savedInstanceState.getBoolean(SHOW_SEARCH_ERROR_PLACEHOLDER_KEY, false)
        val showSearchNoResultsPlaceHolder = savedInstanceState.getBoolean(SHOW_NO_RESULTS_ERROR_PLACEHOLDER_KEY, false)


        val searchEditText = findViewById<EditText>(R.id.search_editText)
        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.search_error_placeholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.search_no_results_placeholder)

        searchEditText.setText(searchedText)
        if (showSearchErrorPlaceHolder) {
            searchErrorPlaceholderFrame.visibility = View.VISIBLE
        }
        else {
            searchErrorPlaceholderFrame.visibility = View.GONE
        }
        if (showSearchNoResultsPlaceHolder) {
            searchNoResultsPlaceholderFrame.visibility = View.VISIBLE
        }
        else {
            searchNoResultsPlaceholderFrame.visibility = View.GONE
        }

        val stringTextList = savedInstanceState.getString(TRACK_LIST_STRING_KEY, EMPTY_TEXT)

        if (stringTextList != null && stringTextList != ""){
            val restoredTrackArray: Array<Track> = Gson().fromJson(stringTextList, Array<Track>::class.java)
            trackList = restoredTrackArray.toMutableList()
            if (trackList.size > 0) {
                val recyclerView = findViewById<RecyclerView>(R.id.trackList)
                showSearchResults(TrackSearchResultsType.SUCCESS, recyclerView)
            }
        }

    }

    companion object {
        const val SEARCHED_TEXT_KEY = "SEARCHED_TEXT"
        const val SHOW_SEARCH_ERROR_PLACEHOLDER_KEY = "SHOW_SEARCH_ERROR_PLACEHOLDER"
        const val SHOW_NO_RESULTS_ERROR_PLACEHOLDER_KEY = "SHOW_NO_RESULTS_ERROR_PLACEHOLDER"
        const val TRACK_LIST_STRING_KEY = "TRACK_LIST"
        const val EMPTY_TEXT = ""
    }
}