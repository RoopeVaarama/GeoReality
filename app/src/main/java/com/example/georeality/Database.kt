package com.example.georeality

import com.google.firebase.database.DatabaseReference

/**
 * @author Topias Peiponen
 * @since 28.09.2020
 */

/**
 * Database object contains all the database write/read operations
 */
object Database {
    var databaseRef : DatabaseReference? = null

    fun addNewAudioMarker(creator: String?, latitude: Double?, longitude: Double?, title: String?) {
        val newAudioMarker = AudioMarker(creator, latitude, longitude, title)

        if (databaseRef != null) {
            databaseRef!!.child("audio_markers").setValue(newAudioMarker)
        }
    }
    fun addNewARMarker(creator: String?, latitude: Double?, longitude: Double?, title: String?) {
        val newARMarker = ARMarker(creator, latitude, longitude, title)

        if (databaseRef != null) {
            databaseRef!!.child("ar_markers").setValue(newARMarker)
        }
    }
}

data class AudioMarker(
    var creator : String?,
    var latitude : Double?,
    var longitude : Double?,
    var title : String?
)

data class ARMarker(
    var creator : String?,
    var latitude : Double?,
    var longitude : Double?,
    var title : String?
)