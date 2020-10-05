package com.example.georeality

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson

class CacheEntityFragment : Fragment() {
    private lateinit var navController : NavController
    private var cacheType : String = ""
    private val args : CacheEntityFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cache_entity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val title = view.findViewById<TextView>(R.id.cacheTitle)
        val creator = view.findViewById<TextView>(R.id.cacheCreator)
        val type = view.findViewById<TextView>(R.id.cacheType)
        val button = view.findViewById<Button>(R.id.cacheOpen)

        //Get JSON data class and convert to proper data class
        val gson = Gson()
        val dataClassJson = args.dataClassJson

        //Check if argument second argument is class AudioMarker or ARMarker
        cacheType = args.dataClasstype
        Log.d("Tags", dataClassJson)

        if (cacheType == "ar") {
            val arMarker = gson.fromJson(dataClassJson, ARMarker::class.java)
            title.text = arMarker.title
            creator.text = arMarker.creator
            type.text = getString(R.string.cache_entity_AR_type)
        } else if (cacheType == "audio") {
            val audioMarker = gson.fromJson(dataClassJson, AudioMarker::class.java)
            title.text = audioMarker.title
            creator.text = audioMarker.creator
            type.text = getString(R.string.cache_entity_audio_type)
        }

        button.setOnClickListener { openCache() }
    }

    private fun openCache() {
        if (cacheType == "ar") {
            navController.navigate(R.id.arFragment)
        } else if (cacheType == "audio") {
        }
    }
}