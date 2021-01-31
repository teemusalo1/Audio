package com.example.audiolab

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    lateinit var inputStream1: InputStream

    lateinit var recFile: File
    lateinit var dataOutputStream: DataOutputStream
    var recRunning = false
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hasPermissions()
        val recbtn = findViewById<Button>(R.id.button)
        val playBtn = findViewById<Button>(R.id.button2)
        val stopBtn = findViewById<Button>(R.id.button3)

        recbtn.setOnClickListener {
            rec()
            recRunning = true
        }
        playBtn.setOnClickListener { inputStream1 = FileInputStream("/storage/emulated/0/Android/data/com.example.audiolab/files/Music/testjv.raw")
            GlobalScope.launch { playAudio(inputStream1) } }
        stopBtn.setOnClickListener { recRunning = false }



    }
    fun showTimes(f: String, s: String) {
        findViewById<TextView>(R.id.textView).text = "first: " + f + " second: " + s
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasPermissions(): Boolean {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No audio recorder access")
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1);
            return true // assuming that the user grants permission
        }
        return true
    }
    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     fun playAudio (istream: InputStream ): String {
        val minBufferSize = AudioTrack .getMinBufferSize(
                44100, AudioFormat .CHANNEL_OUT_STEREO,
                AudioFormat .ENCODING_PCM_16BIT
        )
        val aBuilder = AudioTrack .Builder()
        val aAttr: AudioAttributes = AudioAttributes .Builder()
                .setUsage( AudioAttributes .USAGE_MEDIA)
                .setContentType( AudioAttributes .CONTENT_TYPE_MUSIC)
                .build()
        val aFormat: AudioFormat = AudioFormat .Builder()
                .setEncoding( AudioFormat .ENCODING_PCM_16BIT)
                .setSampleRate( 44100)
                .setChannelMask( AudioFormat .CHANNEL_OUT_STEREO)
                .build()
        val track = aBuilder .setAudioAttributes( aAttr)
                .setAudioFormat( aFormat)
                .setBufferSizeInBytes( minBufferSize )
                .build()
        track!!.setVolume( 0.2f)
        val startTime = LocalTime .now().toString()
        track!!.play()
        var i = 0
        val buffer = ByteArray( minBufferSize )
        try {
            i = istream.read( buffer, 0, minBufferSize )
            while (i != -1) {
                track!!.write( buffer, 0, i)
                i = istream.read( buffer, 0, minBufferSize )
            }
        } catch (e: IOException ) {
            Log.e("FYI", "Stream read error $e")
        }
        try {
            istream.close()
        } catch (e: IOException) {
            Log.e("FYI", "Close error $e")
        }
        track!!.stop()
        track!!.release()
        return startTime

    }
    @SuppressLint("NewApi")
    fun rec() {
        GlobalScope.launch {
        val recFileName = "testjv.raw"
        val storageDir = getExternalFilesDir( Environment .DIRECTORY_MUSIC)
        try {
            recFile = File(storageDir.toString() + "/"+ recFileName )
        } catch (ex: IOException ) {
            Log.e("FYI", "Can't create audio file $ex")
        }


            val outputStream = FileOutputStream( recFile)
            val bufferedOutputStream = BufferedOutputStream( outputStream )
             dataOutputStream = DataOutputStream( bufferedOutputStream )
            val minBufferSize = AudioRecord.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT)
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
            recorder.startRecording()


        while (recRunning) {
            val numofBytes = recorder.read(audioData, 0, minBufferSize)
            if(numofBytes>0) {
                dataOutputStream.write(audioData)
            }
        }
        recorder.stop()
        dataOutputStream.close()
           }}}



