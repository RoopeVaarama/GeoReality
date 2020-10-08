package com.example.georeality

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_audio_listening.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * @author Topias Peiponen
 * @Since 05.10.2020
 */

/**
 * Includes only one functionality, which is playing an audio file
 */
class AudioListeningFragment : Fragment() {
    private lateinit var navController : NavController
    private lateinit var audioMarkerClass: AudioMarker
    private val args : AudioListeningFragmentArgs by navArgs()
    private var file : File? = null
    private var playing : Boolean = false
    private val audioRecorder = AudioRecorder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio_listening, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        //Get argument in JSON and convert it to a data class AudioMarker
        val audioMarkerJson = args.audioMarkerJson
        val gson = Gson()
        audioMarkerClass = gson.fromJson(audioMarkerJson, AudioMarker::class.java)

        //Get file from AudioMarker class ID
        val audioMarkerID = audioMarkerClass.audio_id
        file = Database.dbViewModel!!.getAudioTrack(audioMarkerID!!)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exitButtonAudio.setOnClickListener{
            audioMarkerClass.audio_id?.let { Database.dbViewModel!!.deleteMarker(it,"audio") }
            navController.navigate(R.id.mapFragment)
        }

        actionButton.setOnClickListener {
            if (!playing) {
                playRecording(listeningTimer)
            } else {
                audioRecorder.stopRecording()
            }
        }
    }


    private fun playRecording(timer : Chronometer) {
        if (file != null) {
            timer.base = SystemClock.elapsedRealtime()
            timer.start()
            actionButton.text = getString(R.string.audio_stop)
            try {
                val inputStream = FileInputStream(file!!)
                lifecycleScope.launch(Dispatchers.Main) {
                    val time = async(Dispatchers.IO) {
                        audioRecorder.playAudio(inputStream)}
                        time.await()
                        timer.stop()
                        actionButton.text = getString(R.string.audio_play)
                        playing = false
                        timer.base = SystemClock.elapsedRealtime()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}