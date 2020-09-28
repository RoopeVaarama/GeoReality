package com.example.georeality

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

/**
 * @author Topias Peiponen
 * @since 28.09.2020
 */

/**
 * Database object contains all the database write/read operations
 */
class DBViewModel : ViewModel() {
    private val db = Firebase.database.reference

    private var _audioMarkers = MutableLiveData<MutableList<AudioMarker>>()
    val audioMarkers : LiveData<MutableList<AudioMarker>> = _audioMarkers

    private var _arMarkers = MutableLiveData<List<AudioMarker>>()
    val arMarkers : LiveData<List<AudioMarker>> = _arMarkers

    init {
        _audioMarkers.value = ArrayList()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    private val audioMarkerListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("OnChildAdded", snapshot.value.toString())
            val asd = snapshot.getValue<AudioMarker>()
            audioMarkers.value?.add()
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }
    val lis = db.addChildEventListener(audioMarkerListener)

    fun fetchAudioMarkers() {
    }

    fun addNewAudioMarker(creator: String?, latitude: Double?, longitude: Double?, title: String?) {

            val newAudioMarker = AudioMarker(creator, latitude, longitude, title)

            db.child("audio_markers").setValue(newAudioMarker)

    }
    fun addNewARMarker(creator: String?, latitude: Double?, longitude: Double?, title: String?) {
        val newARMarker = ARMarker(creator, latitude, longitude, title)

        db.child("ar_markers").setValue(newARMarker)
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