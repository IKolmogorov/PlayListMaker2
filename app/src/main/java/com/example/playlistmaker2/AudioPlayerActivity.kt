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
import com.example.playlistmaker2.databinding.ActivityAudioPlayerBinding
import com.google.gson.Gson


class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.audioPlayer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)

        val curTrackString = intent.getStringExtra("curTrack")
        val curTrack =  Gson().fromJson(curTrackString, Track::class.java)

        val albumImage = findViewById<ImageView>(R.id.albumImage)
        val artistName = findViewById<TextView>(R.id.artistName)
        val trackName = findViewById<TextView>(R.id.trackName)
        val fullTrackTime = findViewById<TextView>(R.id.fullTrackTime)
        val album = findViewById<TextView>(R.id.album)
        val genre = findViewById<TextView>(R.id.genre)
        val year = findViewById<TextView>(R.id.year)
        val country = findViewById<TextView>(R.id.country)

        binding.apply {

            if (curTrack != null) {
                artistName.setText(curTrack?.artistName.toString())
                trackName.setText(curTrack?.trackName.toString())
                fullTrackTime.setText(curTrack.getFullTrackTime())
                album.setText(curTrack?.collectionName.toString())
                genre.setText(curTrack?.primaryGenreName.toString())
                year.setText(curTrack.getReleaseYear())
                country.setText(curTrack?.country.toString())

                Glide.with(this@AudioPlayerActivity)
                    .load(curTrack.getCoverArtwork())
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .into(albumImage)

                }
            }


        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        toolbar.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            displayIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(displayIntent)
        }


    }
}