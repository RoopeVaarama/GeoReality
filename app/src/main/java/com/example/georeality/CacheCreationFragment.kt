package com.example.georeality

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_cache_creation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.*

/**
 * @author Topias Peiponen, Roope Vaarama
 * @since 24.09.2020
 */

/**
 * CacheCreationFragment contains all functionality for modifying a marker and
 * submitting it to the database.
 */
class CacheCreationFragment : Fragment() {
    private var file : File? = null
    private var audioRecorder : AudioRecorder? = null
    private var switchIsOn = false
    private var location : String? = null
    private lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        askPerm()
        val view = inflater.inflate(R.layout.fragment_cache_creation, container, false)
        setupLayout(view)
        audioRecorder = AudioRecorder()
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)


        return view
    }

    private fun setupLayout(view : View) {
        val spinner: Spinner = view.findViewById(R.id.spinner)
        val spinnerModels : Spinner = view.findViewById(R.id.spinnerModels)
        val typeSwitch: SwitchCompat = view.findViewById(R.id.typeSwitch)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val recordButton: Button = view.findViewById(R.id.recordButton)
        val playButton : Button = view.findViewById(R.id.playButton)
        val timer : Chronometer = view.findViewById(R.id.timerView)
        var recording = false
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        location = sharedPref?.getString("locationData", "defaultLocation")

        typeSwitch.setOnCheckedChangeListener { _, b ->
            //AUDIO IS CHECKED
            if (b) {
                typeSwitch.text = getString(R.string.audio)
                typeTitle.text = getString(R.string.audio_record)
                spinner.visibility = View.GONE
                spinnerModels.visibility = View.GONE
                arTextInput.visibility = View.GONE
                audioPanel.visibility = View.VISIBLE
                recordButton.visibility = View.VISIBLE
                timerView.visibility = View.VISIBLE
                switchIsOn = true
            }
            //AR IS CHECKED
            else {
                typeSwitch.text = getString(R.string.ar)
                typeTitle.text = getString(R.string.type)
                spinner.visibility = View.VISIBLE
                audioPanel.visibility = View.GONE
                switchIsOn = false
                if (spinner.selectedItem.toString() == getString(R.string.ar_type_2d)) {
                    arTextInput.visibility = View.VISIBLE
                } else if (spinner.selectedItem.toString() == getString(R.string.ar_type_3d)) {
                    spinnerModels.visibility = View.VISIBLE
                }
            }
        }

        saveButton.setOnClickListener {
            val cacheType = typeSwitch.text.toString()
            val title = titleTextInput.text
            val spinnerType = spinner.selectedItem.toString()
            Log.d("save", "Save button was clicked cache type: ${cacheType}, title: ${title}, spinnertype: ${spinnerType}, location: $location")
            submitCache()
        }

        recordButton.setOnClickListener {
            if(!recording){
                file = audioRecorder?.recordAudio(requireActivity())
                typeTitle.text = getString(R.string.recording)
                recordButton.text = getString(R.string.stop)
                playButton.visibility = View.GONE
                timer.base = SystemClock.elapsedRealtime()
                timer.start()
                recording = true

            } else {
                audioRecorder?.stopRecording()
                typeTitle.text = getString(R.string.audio_record)
                recordButton.text = getString(R.string.audio_record)
                timer.stop()
                timer.base = SystemClock.elapsedRealtime()
                recording = false

                if (file != null) {
                    playButton.visibility = View.VISIBLE
                }
            }
        }

        playButton.setOnClickListener {
            playRecording(timer)
        }

        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            spinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.model_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            spinnerModels.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                //2D text is selected
                if (pos == 0){
                    arTextInput.visibility = View.VISIBLE
                    spinnerModels.visibility = View.GONE
                }
                //3D models is selected
                if (pos == 1){
                    arTextInput.visibility = View.GONE
                    spinnerModels.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    /**
     * Checks if fields in CacheCreationFragment layout are filled correctly
     * @return Boolean, true if filled correctly, false if not
     */
    private fun formIsValid() : Boolean {
        //Check if title is empty
        Log.d("Validation", titleTextInput.text.toString())
        if (titleTextInput.text.toString() == "") {
            return false
        }
        //Check if location is available
        if (location == null) {
            return false
        }

        //Check if type specific fields are empty
        if (switchIsOn) {
            return if (file == null) {
                Log.d("audio", "Audio must be recorded!")
                false
            } else {
                true
            }
        } else if (!switchIsOn) {
            return if (arTextInput.text.toString() == "" && spinner.selectedItem.toString() == getString(R.string.ar_type_2d)) {
                Log.d("audio", "AR text cannot be empty!")
                false
            } else {
                true
            }
        }
        return true
    }

    /**
     * Utilizes the AudioRecorder class to play audio and sync it with the UI chronometer
     */
    private fun playRecording(timer : Chronometer) {
        if (file != null) {
            timer.base = SystemClock.elapsedRealtime()
            timer.start()
            try {
                val inputStream = FileInputStream(file!!)
                lifecycleScope.launch(Dispatchers.Main) {
                    val time = async(Dispatchers.IO) {audioRecorder!!.playAudio(inputStream)}
                    time.await()
                    timer.stop()
                    timer.base = SystemClock.elapsedRealtime()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Submits all selected fields and choices
     */
    private fun submitCache() {
        Log.d("SubmitCache", "Clicked! $switchIsOn")
        if (formIsValid()) {
            val title = titleTextInput.text.toString()
            val user = FirebaseAuth.getInstance().currentUser!!.email
            val locationArray = location!!.split(",")
            val latitude = locationArray[0]
            val longitude = locationArray[1]
            val displayText = titleTextInput.text.toString()

            //AR is selected
            if (!switchIsOn) {
                val type = spinner.selectedItem.toString()

                //2D text is selected
                if (type == getString(R.string.ar_type_2d)) {
                    Database.dbViewModel!!.addNewARMarker(
                        user,
                        latitude.toDouble(),
                        longitude.toDouble(),
                        title,
                        type,
                        displayText,
                        null)
                }

                //3D model is selected
                if (type == getString(R.string.ar_type_3d)) {
                    val model = spinnerModels.selectedItem.toString()
                    Database.dbViewModel!!.addNewARMarker(user,
                        latitude.toDouble(),
                        longitude.toDouble(),
                        title,
                        type,
                        null,
                        model)
                }
                navController.navigate(R.id.mapFragment)

            }
            //Audio is selected
            else if (switchIsOn) {
                Log.d("AudioFile", file.toString())
                Database.dbViewModel!!.addNewAudioMarker(user, latitude.toDouble(), longitude.toDouble(), title, file!!)
                navController.navigate(R.id.mapFragment)
            }
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

