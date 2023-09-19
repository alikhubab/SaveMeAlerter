package com.gray.vulf.savemealerter

//import android.speech.RecognitionResult
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi


const val TAG = "SpeechRecognitionService";
class SpeechRecognitionService: Service() {
    private lateinit var speechRecognizer: SpeechRecognizer

    private val CHANNEL_ID = "SaveMeAlerterChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000);


        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.e(TAG, "onReadyForSpeech")
            }
            override fun onBeginningOfSpeech() {
                Log.e(TAG, "onBeginningOfSpeech")

            }
            override fun onRmsChanged(rmsdB: Float) {
//                Log.e(TAG, "onRmsChanged")

            }
            override fun onBufferReceived(p0: ByteArray?) {
                Log.e(TAG, "onBufferReceived")

            }

            override fun onPartialResults(partialResults: Bundle?) {
                val recognizedWords = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!recognizedWords.isNullOrEmpty()) {
                    val recognizedText = recognizedWords[0]
                    Log.i(TAG, "onPartialResults: $recognizedText")
                    // Process or use the recognized text as needed.
                }

            }

            override fun onResults(results: Bundle?) {
                Log.e(TAG, "onResults")

                val recognizedWords = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!recognizedWords.isNullOrEmpty()) {
                    val recognizedText = recognizedWords[0]
                    Log.i(TAG, "onResults: $recognizedText")
                    // Process or use the recognized text as needed.
                }
                speechRecognizer.startListening(recognizerIntent)
                // Handle speech recognition results here.
            }
            override fun onEndOfSpeech() {
                Log.e(TAG, "onEndOfSpeech")

            }
            override fun onError(error: Int) {
                speechRecognizer.startListening(recognizerIntent)
                Log.e(TAG, "onError $error")

            }
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.e(TAG, "onEvent")

            }
        })

        speechRecognizer.startListening(recognizerIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }


        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                CHANNEL_ID
            }


        val notification: Notification = Notification.Builder(
            this,
            channelId
        ).setContentTitle("Save Me Alerter")
            .setOngoing(true)
            .setContentText("Listening for save me")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("new ticker")
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()


        startForeground(NOTIFICATION_ID, notification)

        // Start speech recognition here.

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = 0x452312
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}