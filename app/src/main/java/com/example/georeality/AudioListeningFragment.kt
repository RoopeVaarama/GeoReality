package com.example.georeality

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_audio_listening.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException

//AudioListeningFragment class
class AudioListeningFragment : Fragment() {
    private val args : AudioListeningFragmentArgs by navArgs()
    private var file : File? = null
    private var playing : Boolean = false
    private val audioRecorder = AudioRecorder()
    private lateinit var timer : Chronometer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_audio_listening, container, false)

        //Get argument in JSON and convert it to a data class AudioMarker
        val audioMarkerJson = args.audioMarkerJson
        val gson = Gson()
        val audioMarkerClass = gson.fromJson(audioMarkerJson, AudioMarker::class.java)

        //Get file from AudioMarker class ID
        val audioMarkerID = audioMarkerClass.audio_id
        file = Database.dbViewModel!!.getAudioTrack(audioMarkerID!!)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    val time = async(Dispatchers.IO) { audioRecorder.playAudio(inputStream)}
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