package com.example.georeality

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_cache_creation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */
class CacheCreationFragment : Fragment() {

    lateinit var recFile: File
    private var Recording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        askPerm()
        val view = inflater.inflate(R.layout.fragment_cache_creation, container, false)
        val spinner: Spinner = view.findViewById(R.id.spinner)
        val typeSwitch: SwitchCompat = view.findViewById(R.id.typeSwitch)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val recordButton: Button = view.findViewById(R.id.recordButton)
        var recording: Boolean = false
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        var location = sharedPref?.getString("locationData", "defaultLocation")

        typeSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                typeSwitch.text = getString(R.string.audio)
                typeView.text = getString(R.string.record)
                spinner.visibility = View.GONE
                recordButton.visibility = View.VISIBLE
                timerView.visibility = View.VISIBLE
            } else {
                typeSwitch.text = getString(R.string.ar)
                typeView.text = getString(R.string.type)
                spinner.visibility = View.VISIBLE
                recordButton.visibility = View.GONE
                timerView.visibility = View.GONE
            }
        }

        saveButton.setOnClickListener {

            val cacheType = typeSwitch.text.toString()
            val title = title_text_input.text
            val spinnerType = spinner.selectedItem.toString()
            Log.d("save", "Save button was clicked cache type: ${cacheType}, title: ${title}, spinnertype: ${spinnerType}, location: ${location}")
        }

        recordButton.setOnClickListener {
            if(!recording){
                audioRecorder()

                typeView.text = getString(R.string.recording)
                recordButton.text = getString(R.string.stop)
                recording = true
            } else {
                Recording = false
                typeView.text = getString(R.string.record)
                recordButton.text = getString(R.string.record)
                recording = false
            }

        }

        //Crate an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.type_array,
            R.layout.color_spinner_layout
        ).also { adapter ->
            //Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            //Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        val spinnerItems = resources.getStringArray(R.array.type_array)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos == 0){
                    Toast.makeText(requireActivity(), "Selected item" + " " + spinnerItems[pos], Toast.LENGTH_SHORT).show()
                }
                if (pos == 1){
                    Toast.makeText(requireActivity(), "Selected item" + " " + spinnerItems[pos], Toast.LENGTH_SHORT).show()
                }
                if (pos == 2){
                    Toast.makeText(requireActivity(), "Selected item" + " " + spinnerItems[pos], Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


        return view
    }

    private fun audioRecorder() {
        val file = "record.raw"
        /*val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)*/
        try {
            //recFile = File("$storageDir/$file")
        } catch (e: Exception) {
            Log.d("error", "error creating file: ${e.message}")
        }

        try {
            val outputStream = FileOutputStream(recFile)
            val bufferedOutputStream = BufferedOutputStream(outputStream)
            val dataOutputStream = DataOutputStream(bufferedOutputStream)

            val minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val aFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

            val recorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(aFormat)
                .setBufferSizeInBytes(minBufferSize)
                .build()

            val audioData = ByteArray(minBufferSize)

            GlobalScope.launch(Dispatchers.IO) {
                Recording = true
                recorder.startRecording()
                while (Recording) {
                    val numofBytes = recorder.read(audioData, 0, minBufferSize)
                    if (numofBytes > 0) {
                        dataOutputStream.write(audioData)

                    }
                }
                Log.d("audioplay", "Recording stopped")
                recorder.stop()
                dataOutputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun askPerm() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().application.applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }
}

class SpinnerActivity: Activity(), AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        parent?.getItemAtPosition(pos)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}

