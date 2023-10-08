package com.gray.vulf.savemealerter

//import android.speech.RecognitionResult

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.gray.vulf.savemealerter.data.models.EmailContact
import com.gray.vulf.savemealerter.data.models.EmailPassword
import com.gray.vulf.savemealerter.data.models.PhoneContact
import com.gray.vulf.savemealerter.data.models.SaveMeMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.util.Locale
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


const val TAG = "SpeechRecognitionService";

class SpeechRecognitionService : Service() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val CHANNEL_ID = "SaveMeAlerterChannel"
    private val NOTIFICATION_ID = 1

    private fun sendMail(targetEmails: List<EmailContact>, sender: EmailPassword, message: String) {

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com" // Replace with your SMTP server
        props["mail.smtp.port"] = "587" // Replace with the appropriate port

        try {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(sender.email, sender.password)
                }
            })

            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(sender.email))

            val recipientAddresses = targetEmails.map { InternetAddress(it.email) }.toTypedArray()
            mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddresses)

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


    private fun sendMessage(targetPhones: List<PhoneContact>, message: String) {
        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
        Log.i(TAG, "sendMessage>>subscriptions $subscriptionId")
        val smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        try {
            targetPhones.map {
                if (it.phone.isBlank()) return
                smsManager.sendTextMessage(it.phone, null, message, null, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message>> $e")
        }
        Log.i(TAG, "sendMessage>>subscriptions $smsManager")
    }

    private suspend fun getAddress(latitude: Double, longitude: Double): String =
        withContext(Dispatchers.IO) {


            return@withContext suspendCoroutine<String> { continuation ->
                val geocoder = Geocoder(this@SpeechRecognitionService, Locale.getDefault())
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(latitude, longitude, 1) {
                            if (it.isEmpty())
                                continuation.resume("")
                            else
                                continuation.resume(it[0].getAddressLine(0))
                        }
                    } else {
                        geocoder.getFromLocation(latitude, longitude, 1).let {
                            if (it.isNullOrEmpty())
                                continuation.resume("")
                            else
                                continuation.resume(it[0].getAddressLine(0))
                        }
                    }
                } catch (e: Exception) {
                    continuation.resume("")
                }
            }
        }

    private fun askForHelp() {
        val message: SaveMeMessage = getSaveMeMessage()
        Log.e("askForHelp>>message", message.message)
        Log.e("askForHelp>>senderEmail", message.sender.email)
        Log.e("askForHelp>>firstEmailTarget", message.mailTargets.toString())
        Log.e("askForHelp>>firstPhoneTarget", message.phoneTargets.toString())

        scope.launch {
            val location = getLocation()
            Log.i("askForHelp>>Location>>", location?.latitude.toString())
            var address = ""
            if (location != null)
                address = getAddress(location.latitude, location.longitude)
            Log.i("askForHelp>>address>>", address)
            val msg =
                "${message.message} \n Address: ${address} \n Coordinates: ${location?.latitude} , ${location?.longitude}"
            sendMessage(message.phoneTargets, msg)
            Log.e("Mesage is>>>>>>>>>>>>>>>>>", msg)
            sendMail(message.mailTargets, message.sender, msg)
        }
    }


    private fun hasPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): Location? = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            if (hasPermission()) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this@SpeechRecognitionService)

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    }
                ).addOnSuccessListener {
                    continuation.resume(it)
                }
            } else {
                continuation.resume(null)
            }
        }
    }


    private inline fun <reified K> getContactsListFromStorage(contactType: String): List<K> {
        val sharedPreferences = getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val contacts: List<K>
        val jsonContacts = sharedPreferences.getString(contactType, "");

        contacts = if (jsonContacts.isNullOrBlank()) {
            listOf()
        } else {
            Json.decodeFromString<List<K>>(jsonContacts)
        }
        return contacts
    }


    private inline fun <reified K> getContactFromStorage(contactType: String): K? {
        val sharedPreferences = getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val contact: K?
        val jsonContact = sharedPreferences.getString(contactType, "")
        contact = if (jsonContact.isNullOrBlank()) {
            null
        } else {
            Json.decodeFromString<K>(jsonContact)
        }
        return contact
    }

    private fun getMessageFromStorage(): String {
        val sharedPreferences = getSharedPreferences("message", Context.MODE_PRIVATE)
        return sharedPreferences.getString("message", "").let {
            if (it.isNullOrBlank())
                return@let "Save Me"
            it
        }
    }

    private fun getSaveMeMessage(): SaveMeMessage {
        val phoneContacts = getContactsListFromStorage<PhoneContact>("smsContacts")
        val emailContacts = getContactsListFromStorage<EmailContact>("emailContacts")
        val senderEmailPassword =
            getContactFromStorage<EmailPassword>("senderEmailPassword").let {
                if (it == null)
                    return@let EmailPassword(email = "alikhubab6@gmail.com", password = "123")
                it
            }
        val message = getMessageFromStorage()

        return SaveMeMessage(
            sender = senderEmailPassword,
            message = message,
            mailTargets = emailContacts,
            phoneTargets = phoneContacts
        )
    }


    override fun onCreate() {
        super.onCreate()
//        askForHelp()


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                        askForHelp()
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

        try {
            speechRecognizer.startListening(recognizerIntent)
        } catch (e: Exception) {

        }
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
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
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
        try {
            speechRecognizer.destroy()
        } catch (e: Exception) {
            Log.w("SpeechRecognitionService>>OnDestroy>>", "Couldn't stop service")
        }
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}