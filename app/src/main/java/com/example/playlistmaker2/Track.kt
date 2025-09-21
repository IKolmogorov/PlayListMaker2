package com.example.playlistmaker2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Track(
    val trackId: String, // Уникальный идентификатор композиции
    val trackName: String, // Название композиции
    val collectionName: String, //Название альбома
    val releaseDate: Date, //Год релиза трека
    val primaryGenreName: String, //Жанр трека
    val artistName: String, // Имя исполнителя
    val country: String, // Страна исполнителя
    val trackTimeMillis: Int, //Длительность трека в миллисекундах
    val artworkUrl100: String // Ссылка на изображение обложки

) {
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/',"512x512bb.jpg")

    fun getReleaseYear() = SimpleDateFormat("yyyy", Locale.getDefault()).format(releaseDate)

    fun getFullTrackTime() = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
}

