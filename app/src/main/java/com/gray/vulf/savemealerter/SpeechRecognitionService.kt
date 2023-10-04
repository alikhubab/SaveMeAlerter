package com.gray.vulf.savemealerter

//import android.speech.RecognitionResult

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Properties
import javax.mail.Address
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


const val TAG = "SpeechRecognitionService";

class SpeechRecognitionService : Service() {
    private lateinit var speechRecognizer: SpeechRecognizer

    private val CHANNEL_ID = "SaveMeAlerterChannel"
    private val NOTIFICATION_ID = 1

    private fun sendMail(targetEmail: String, message: String) {
        if (targetEmail.isBlank()) return

        val username = "alikhubab6@gmail.com" // Your email address
        val password = "okhwkeckgfdpuunk" // Your email password


        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com" // Replace with your SMTP server
        props["mail.smtp.port"] = "587" // Replace with the appropriate port

        try {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(username))
            mimeMessage.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(targetEmail)
            )
            mimeMessage.subject = "Emergency Save"
            mimeMessage.setText(message)

            GlobalScope.launch(Dispatchers.IO) {
                Transport.send(mimeMessage)
//                showNotification("Save Me Email sent", "Save Me Email sent to $targetEmail")
            }
            Log.i(TAG, "Email Send Successfully")
        } catch (e: MessagingException) {
            Log.e(TAG, "Error Sending Email ${e.message}")
            e.printStackTrace()
        }
    }


    private fun sendMessage(targetPhone: String, message: String) {
        if (targetPhone.isBlank()) return
        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
        Log.i(TAG, "sendMessage>>subscriptions $subscriptionId")
        val smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        try {
            smsManager.sendTextMessage(targetPhone, null, message, null, null)
//            showNotification("Save Me SMS sent", "Save Me SMS sent to $targetPhone")

        } catch (e: Exception) {
            Log.e(TAG, "Error sending message>> $e")
        }
        Log.i(TAG, "sendMessage>>subscriptions $smsManager")
    }


    private fun sendWhatsappMessage(message: String, mobileNumbers: Array<String>) {
        try {
            val packageManager = applicationContext.packageManager
            if (mobileNumbers.isNotEmpty()) {
                mobileNumbers.forEach {
                    val uri = "https://api.whatsapp.com?phone=$it&text=${
                        URLEncoder.encode(
                            message,
                            "utf-8"
                        )
                    }"
                    val whatsappIntent = Intent(Intent.ACTION_VIEW)

                    with(whatsappIntent) {
                        setPackage("com.whatsapp")
                        data = Uri.parse(uri)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (whatsappIntent.resolveActivity(packageManager) != null) {
                        applicationContext.startActivity(whatsappIntent)
                        Thread.sleep(10000)

                    } else {
                        Log.e(TAG, "Whatsapp not installed")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }

    }


    private fun getSaveMeData(): SaveMeData {
        val sharedPreferences = getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        return object : SaveMeData {
            override val message = sharedPreferences.getString("message", "") ?: "Save Me"
            override val targetPhone = sharedPreferences.getString("phone", "") ?: ""
            override val targetEmail = sharedPreferences.getString("email", "") ?: ""
        }
    }

    interface SaveMeData {
        val message: String
        val targetPhone: String
        val targetEmail: String
    }

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
                val recognizedWords =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!recognizedWords.isNullOrEmpty()) {
                    val recognizedText = recognizedWords[0]
                    Log.i(TAG, "onPartialResults: $recognizedText")
                    if (recognizedText.lowercase().contains("save me")) {
                        val saveMeData = getSaveMeData();
                        sendMessage(saveMeData.targetPhone, saveMeData.message)
                        sendMail(saveMeData.targetEmail, saveMeData.message)
                    }
//                     Process or use the recognized text as needed.
                }

            }

            override fun onResults(results: Bundle?) {
                Log.e(TAG, "onResults")

                val recognizedWords =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
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
                if (error == 9) {
                    Thread.sleep(2000)
                }
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
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
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

    private fun showNotification(title: String, content: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                CHANNEL_ID
            }


        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(12, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
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