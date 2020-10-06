package com.example.georeality

import android.app.Activity
import android.media.*
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception
import java.time.LocalTime

/**
 * AudioRecorder class contains control functions for recording and playing audio
 */
class AudioRecorder {
    private lateinit var recFile : File
    private var recording : Boolean = false

    /** Function to record the audio
     */
    fun recordAudio(activity : Activity): File? {
        val file = "record.raw"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        Log.d("StorageDir", storageDir.toString())
        try {
            recFile = File("$storageDir/$file")
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
                recording = true
                recorder.startRecording()
                while (recording) {
                    val numOfBytes = recorder.read(audioData, 0, minBufferSize)
                    if (numOfBytes > 0) {
                        dataOutputStream.write(audioData)

                    }
                }
                Log.d("audioplay", "Recording stopped")
                recorder.stop()
                dataOutputStream.close()
            }
            return recFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /** Function to stop recording
     */
    fun stopRecording() {
        recording = false
    }
    /** Function to play the recording
     */
    fun playAudio(istream : InputStream) : String {
        Log.d("PlayAudio", "Playing audio...")
        val minBufferSize= AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT)
        val aBuilder= AudioTrack.Builder()
        val aAttr: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val aFormat: AudioFormat= AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(44100)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        val track= aBuilder.setAudioAttributes(aAttr)
            .setAudioFormat(aFormat)
            .setBufferSizeInBytes(minBufferSize)
            .build()
        track.setVolume(0.2f)
        val startTime= LocalTime.now().toString()
        track.play()
        var i = 0
        val buffer= ByteArray(minBufferSize)
        try{
            i = istream.read(buffer, 0, minBufferSize)
            while(i != -1) {
                track.write(buffer, 0, i)
                i = istream.read(buffer, 0, minBufferSize)
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }
        try{
            istream.close()
        } catch(e: IOException) {
            e.printStackTrace()
        }
        track.stop()
        track.release()
        return startTime
    }
}