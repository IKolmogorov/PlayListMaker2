package com.example.playlistmaker2

data class Track(
    val trackName: String, // Название композиции
    val artistName: String, // Имя исполнителя
    //val trackTime: String, // Продолжительность трека
    val trackTimeMillis: Int, //Длительность трека в миллисекундах
    val artworkUrl100: String // Ссылка на изображение обложки
)