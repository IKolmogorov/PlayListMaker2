package com.example.playlistmaker2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
    var searchedText: String = ""
    var trackList: MutableList<Track> = mutableListOf()
    var trackSearchHistoryList: MutableList<Track> = mutableListOf()
    var searchFieldHasFocus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val returnFrameLayout = findViewById<FrameLayout>(R.id.return_frame)
        val searchEditText = findViewById<EditText>(R.id.search_editText)
        val clearButton = findViewById<ImageView>(R.id.search_clear_imageView)
        val historyResetButton = findViewById<Button>(R.id.history_reset_button)
        val searchErrorRefreshButton = findViewById<Button>(R.id.search_error_refresh_button)
        val searchResultsRecyclerView = findViewById<RecyclerView>(R.id.trackList)
        val searchHistoryRecyclerView = findViewById<RecyclerView>(R.id.trackHistorySearchList)

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

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
            showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
        }

        historyResetButton.setOnClickListener {
            trackSearchHistoryList.clear()
            sharedPreferences.edit().putString(TRACKS_SEARCH_HISTORY, Gson().toJson(trackSearchHistoryList)).apply()
            showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTrackSearchResults(searchTrackService, searchEditText.text.toString(), sharedPreferences)
            }
            false
        }

        searchErrorRefreshButton.setOnClickListener {
            getTrackSearchResults(searchTrackService, searchEditText.text.toString(), sharedPreferences)
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

        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            searchFieldHasFocus = hasFocus

            if (hasFocus && searchEditText.text.isEmpty())
            {
                showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
            }
        }

        showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
}

    fun showSearchResults(trackSearchResultsType: TrackSearchResultsType, sharedPreferences: SharedPreferences){
        val searchResultFrame = findViewById<FrameLayout>(R.id.searchResultsFrame)
        val searchHistoryFrame = findViewById<LinearLayout>(R.id.searchHistoryFrame)
        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.search_error_placeholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.search_no_results_placeholder)
        val searchResultsRecyclerView = findViewById<RecyclerView>(R.id.trackList)
        val searchHistoryRecyclerView = findViewById<RecyclerView>(R.id.trackHistorySearchList)

        when (trackSearchResultsType){
            TrackSearchResultsType.SUCCESS -> {

                val trackAdapter = TrackAdapter(trackList, getTrackOnClickListener(sharedPreferences))
                searchResultsRecyclerView.adapter = trackAdapter

                searchResultFrame.visibility                = View.VISIBLE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }
            TrackSearchResultsType.EMPTY -> {
                trackList.clear()

                val trackAdapter = TrackAdapter(ArrayList<Track>(0), getTrackOnClickListener(sharedPreferences))
                searchResultsRecyclerView.adapter = trackAdapter

                showTracksSearchHistory(sharedPreferences, searchHistoryRecyclerView)

                searchResultFrame.visibility                = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }
            TrackSearchResultsType.NO_RESULTS -> {
                searchResultFrame.visibility                = View.GONE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.VISIBLE
            }
            TrackSearchResultsType.ERROR -> {
                searchResultFrame.visibility                = View.GONE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.VISIBLE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }
        }
    }

    fun showTracksSearchHistory(sharedPreferences: SharedPreferences, recyclerView: RecyclerView){
        val stringSearchHistoryList = sharedPreferences.getString(TRACKS_SEARCH_HISTORY, "")
        val searchHistoryFrame = findViewById<LinearLayout>(R.id.searchHistoryFrame)


        if (stringSearchHistoryList != null && stringSearchHistoryList != "" && searchFieldHasFocus){
            val trackSearchHistoryArray: Array<Track> = Gson().fromJson(stringSearchHistoryList, Array<Track>::class.java)
            trackSearchHistoryList = trackSearchHistoryArray.toMutableList()
            if (trackSearchHistoryList.size > 0) {

                val trackAdapter = TrackAdapter(trackSearchHistoryList, getTrackOnClickListener(sharedPreferences))
                recyclerView.adapter = trackAdapter
                searchHistoryFrame.visibility = View.VISIBLE
            }
            else {
                searchHistoryFrame.visibility = View.GONE
            }
        }
        else {
            searchHistoryFrame.visibility = View.GONE
        }
    }

    fun getTrackOnClickListener(sharedPreferences: SharedPreferences): TrackAdapter.OnTrackClickListener{

        // определяем слушателя нажатия элемента в списке
        val stateClickListener: TrackAdapter.OnTrackClickListener =
            object : TrackAdapter.OnTrackClickListener {
                override fun onTrackClick(track: Track, position: Int) {

                    updateTrackList(trackSearchHistoryList, track)
                    sharedPreferences.edit().putString(TRACKS_SEARCH_HISTORY, Gson().toJson(trackSearchHistoryList)).apply()

                    val displayIntent = Intent(this@SearchActivity, AudioPlayerActivity::class.java).apply {
                        putExtra("curTrack", Gson().toJson(track))
                    }
                    startActivity(displayIntent)

                }
            }

        return stateClickListener

    }

    fun updateTrackList(trackList: MutableList<Track>, newTrack: Track){

        // Ищем track в списке. Если уже добавлен, то удаляем из списка, затем добавляем трек в начало списка
        for (curTrack in trackList)
        {
            if (curTrack.trackId == newTrack.trackId)
            {
                trackList.remove(curTrack)
                break
            }
        }

        // Добавляем track в начало списка
        trackList.add(0, newTrack)

        // Проверяем кол-во элементов, если > 10, то все track с 11 и больше удаляем из списка
        if (trackList.size > 10)
        {
            for (i in trackList.size-1..10 )
            {
                trackList.removeAt(10)
            }
        }

    }

    fun getTrackSearchResults(searchTrackService : ItunesApi, expression: String, sharedPreferences: SharedPreferences) {
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

                                    showSearchResults(TrackSearchResultsType.SUCCESS, sharedPreferences)
                                } else {
                                    showSearchResults(TrackSearchResultsType.NO_RESULTS, sharedPreferences)
                                }
                            } else {
                                showSearchResults(TrackSearchResultsType.NO_RESULTS, sharedPreferences)
                            }
                        }
                        else -> {
                            showSearchResults(TrackSearchResultsType.ERROR, sharedPreferences)
                        }
                    }
                }

                override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                    showSearchResults(TrackSearchResultsType.ERROR, sharedPreferences)
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
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)

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
                showSearchResults(TrackSearchResultsType.SUCCESS, sharedPreferences)
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