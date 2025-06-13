package com.example.playlistmaker2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson


class AudioPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.audioPlayer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val curTrackString = intent.getStringExtra("curTrack")
        val curTrack = Gson().fromJson(curTrackString, Track::class.java)

        val albumImage = findViewById<ImageView>(R.id.albumImage)
        val artistName = findViewById<TextView>(R.id.artistName)
        val trackName = findViewById<TextView>(R.id.trackName)
        val fullTrackTime = findViewById<TextView>(R.id.fullTrackTime)
        val album = findViewById<TextView>(R.id.album)
        val genre = findViewById<TextView>(R.id.genre)
        val year = findViewById<TextView>(R.id.year)
        val country = findViewById<TextView>(R.id.country)


        if (curTrack != null) {

            artistName.text = curTrack.artistName
            trackName.text = curTrack.trackName
            fullTrackTime.text = curTrack.getFullTrackTime()
            album.text = curTrack.collectionName
            genre.text = curTrack.primaryGenreName
            year.text = curTrack.getReleaseYear()
            country.text = curTrack.country

            Glide.with(this@AudioPlayerActivity)
                .load(curTrack.getCoverArtwork())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(albumImage)

        }


        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            displayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(displayIntent)
        }


    }
}