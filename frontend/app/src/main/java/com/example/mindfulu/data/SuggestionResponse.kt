package com.example.mindfulu.data

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SuggestionResponse(
    val message: String,
    val suggestions: Suggestions
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Suggestions(
    val saranMusik: MusicSuggestion,
    val saranKegiatan: List<ActivitySuggestion>
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class MusicSuggestion(
    val judul: String,
    val artis: String,
    val alasan: String,
    val linkVideo: String?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class ActivitySuggestion(
    val kegiatan: String,
    val deskripsi: String
) : Parcelable