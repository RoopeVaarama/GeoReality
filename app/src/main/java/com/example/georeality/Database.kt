package com.example.georeality

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
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

        // Listen to database changes for audio markers
        val audioMarkerListener = object : ChildEventListener {
            // Database node added to database
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val markerClass = snapshot.getValue<AudioMarker>()
                if (markerClass != null) {
                    _audioMarkers.value?.add(markerClass)
                    _audioMarkers.value = _audioMarkers.value

                }
            }
            // Database node removed from database
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val markerClass = snapshot.getValue<AudioMarker>()
                if (markerClass != null) {
                    _audioMarkers.value?.remove(markerClass)
                    _audioMarkers.value = _audioMarkers.value

                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        dbAudio.child("audio").addChildEventListener(audioMarkerListener)

        // Listen to database changes for AR markers
        val arMarkerListener = object : ChildEventListener {
            // Database node added to database
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val markerClass = snapshot.getValue<ARMarker>()
                if (markerClass != null) {
                    _arMarkers.value?.add(markerClass)
                    _arMarkers.value = _arMarkers.value

                }
            }
            // Database node removed from database
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val markerClass = snapshot.getValue<ARMarker>()
                if (markerClass != null) {
                    _arMarkers.value?.remove(markerClass)
                    _arMarkers.value = _arMarkers.value
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        dbAR.child("ar").addChildEventListener(arMarkerListener)
    }

    /**
     * Saves AudioMarker to database
     */
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

    /**
     * Saves ARMarker to database
     */
    fun addNewARMarker(user : String?,
                       latitude: Double?,
                       longitude : Double?,
                       title : String?,
                       type : String?,
                       displayText : String?,
                       model_type : String?
                       ) {
        val tempDbAR = dbAR.child("ar").push()
        val markerID = tempDbAR.key
        val newArMarker = ARMarker(user, latitude, longitude, title, type, displayText, model_type, markerID)
        tempDbAR.setValue(newArMarker)

    }

    /**
     * Fetches audio track from database based on (id) parameter and returns it
     */
    fun getAudioTrack(id : String) : File? {
        val storageReference = dbStorage.reference
        val pathReference = storageReference.child(id)

        val track = File.createTempFile("tempTrack", "raw")
        pathReference.getFile(track).addOnSuccessListener {
            Log.d("Track", "Succesfully created")
        }.addOnFailureListener {
            Log.d("Error", it.toString())
        }
        return track
    }

    /**
     * Deletes a marker based on the (type) parameter
     */
    fun deleteMarker(id : String, type : String) {
        if (type == "audio") {
            dbAudio.child("audio").child(id).setValue(null)
            val trackToDeleteReference = dbStorage.reference.child(id)
            Log.d("FilePath", id)
            trackToDeleteReference.delete().addOnSuccessListener {
                Log.d("File", "File deleted succesfully!")
            }.addOnFailureListener {
                Log.d("File", "File could not be deleted!")
            }
        } else if (type == "ar") {
            dbAR.child("ar").child(id).setValue(null)
        }
    }

    private fun addNewAudioTrack(file : File, id : String) {
        val inputStream : InputStream = FileInputStream(file)

        dbStorage.reference.child(id).putStream(inputStream)
    }
}

/**
 * Include dbViewModel inside an object for global level access
 */
object Database {
    var dbViewModel : DBViewModel? = null
    init {
        dbViewModel = DBViewModel()
    }
}

data class AudioMarker(
    var creator : String? = "",
    var latitude : Double? = 0.0,
    var longitude : Double? = 0.0,
    var title : String? = "",
    var audio_id : String? = ""
)

data class ARMarker(
    var creator : String? = "",
    var latitude : Double? = 0.0,
    var longitude : Double? = 0.0,
    var title : String? = "",
    var type : String? = "",
    var text : String? = "",
    var model_type : String? = "",
    var ar_id : String? = ""
)