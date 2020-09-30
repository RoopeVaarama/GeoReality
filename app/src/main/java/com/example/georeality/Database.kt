package com.example.georeality

import android.icu.text.Transliterator
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author Topias Peiponen
 * @since 28.09.2020
 */

/**
 * DBViewModel contains all the database write/read operations and LiveData objects
 * to which results are saved to.
 */
class DBViewModel : ViewModel() {
    private val dbAudio = Firebase.database.getReference("audio_markers")
    private val dbAR = Firebase.database.getReference("ar_markers")
    private val dbStorage = FirebaseStorage.getInstance()

    private var _audioMarkers = MutableLiveData<MutableList<AudioMarker>>()
    val audioMarkers : LiveData<MutableList<AudioMarker>> = _audioMarkers

    private var _arMarkers = MutableLiveData<MutableList<ARMarker>>()
    val arMarkers : LiveData<MutableList<ARMarker>> = _arMarkers

    init {
        _audioMarkers.value = ArrayList()
        _arMarkers.value = ArrayList()

        // Implement logic when a new audio marker is added
        val audioMarkerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("OnChildAddedAudio", snapshot.value.toString())
                for (marker in snapshot.children) {
                    val markerClass = marker.getValue<AudioMarker>()
                    if (markerClass != null) {
                        _audioMarkers.value?.add(markerClass)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }

        }
        dbAudio.addValueEventListener(audioMarkerListener)

        // Implement logic when new AR marker is added
        val arMarkerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("OnChildAddedAR", snapshot.value.toString())
                for (marker in snapshot.children) {
                    val markerClass = marker.getValue<ARMarker>()
                    if (markerClass != null) {
                        _arMarkers.value?.add(markerClass)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }
        dbAR.addValueEventListener(arMarkerListener)

    }

    fun addNewAudioMarker(user : String?,
                          latitude : Double?,
                          longitude : Double?,
                          title : String?,
                          file : File) {
        val tempDbAudio = dbAudio.child("audio").push()
        val markerID = tempDbAudio.key
        if (markerID != null) {
            addNewAudioTrack(file, markerID)
        }
        val newAudioMarker = AudioMarker(user, latitude, longitude, title, markerID)
        tempDbAudio.setValue(newAudioMarker)
    }

    fun addNewARMarker(arMarker : ARMarker) {
        val tempDbAR = dbAR.child("ar").push()
        tempDbAR.setValue(arMarker)

    }

    private fun addNewAudioTrack(file : File, id : String) {
        val inputStream : InputStream = FileInputStream(file)

        dbStorage.reference.child(id).putStream(inputStream)
    }
}

object Database {
    var dbViewModel : DBViewModel? = null
    init {
        dbViewModel = DBViewModel()
    }
}

// AudioMarker contains data to create a marker with audio features
data class AudioMarker(
    var creator : String? = "",
    var latitude : Double? = 0.0,
    var longitude : Double? = 0.0,
    var title : String? = "",
    var audio_id : String? = ""
)

// ARMarker contains data to create a marker with AR features and capabilities
data class ARMarker(
    var creator : String? = "",
    var latitude : Double? = 0.0,
    var longitude : Double? = 0.0,
    var title : String? = "",
    var type : String? = "",
    var text : String? = "",
    var model_type : String? = ""

)