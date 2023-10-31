package com.gray.vulf.savemealerter.ui

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gray.vulf.savemealerter.R
import com.gray.vulf.savemealerter.SpeechRecognitionService
import com.gray.vulf.savemealerter.TAG
import com.gray.vulf.savemealerter.ui.theme.SaveMeAlerterTheme

interface SaveMeData {
    val message: String
    val targetPhone: String
    val targetEmail: String
}

fun getSaveMeData(context: Context): SaveMeData {
    val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
    return object : SaveMeData {
        override val message = sharedPreferences.getString("message", "") ?: "Save Me"
        override val targetPhone = sharedPreferences.getString("phone", "") ?: ""
        override val targetEmail = sharedPreferences.getString("email", "") ?: ""
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Home(
    onNavigateToContacts: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onContactsSync: () -> Unit = {},
    syncingContacts: Boolean,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val saveMeData = getSaveMeData(context)

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    var openProfileDialog by remember {
        mutableStateOf(false)
    }

    var isRecognitionRunning by remember {
        mutableStateOf(false)
    }

    // Get a list of currently running services
    fun getIsRecognitionRunning(): Boolean {
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in runningServices) {
            if (serviceInfo.service.className == SpeechRecognitionService::class.java.name)
                return true
        }
        return false
    }

    isRecognitionRunning = getIsRecognitionRunning();

    var message by remember {
        mutableStateOf(saveMeData.message)
    }


    fun saveToPreference(key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, value).apply()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun startSpeechRecognitionService(context: Context) {
        Log.i(TAG, "startSpeechRecognitionService")
        val permissions = arrayOf<String>(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS
        )

        ActivityCompat.requestPermissions(
            context as Activity, permissions, 12
        );

        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_DENIED
            ) {
                with(Toast(context)) {
                    setText("You have denied permission please go to settings and give permissions.")
                    show()
                }
                return
            }
        }


        val intent = Intent(context, SpeechRecognitionService::class.java)
        if (getIsRecognitionRunning()) {
            context.stopService(intent)
            isRecognitionRunning = false
            return
        }

        context.startForegroundService(intent)
        isRecognitionRunning = (true)
    }

    Scaffold { padding ->
        when {
            openProfileDialog -> {
                ProfileDialog(
                    onDismissRequest = {
                        openProfileDialog = false
                    },
                    onLoginRequest = {
                        onNavigateToAuth()
                    },
                    userEmail = Firebase.auth.currentUser?.email,
                    onLogoutRequest = {
                        Firebase.auth.signOut()
                        openProfileDialog = false
                    },
                    onSyncDataRequest = {
                        onContactsSync()
                    },
                    syncingContacts = syncingContacts
                )
            }
        }

        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.slackedlinesback),
                contentDescription = "background art",
                alignment = AbsoluteAlignment.TopRight,
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.padding(horizontal = 32.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "HI!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(
                            onClick = {
                                openProfileDialog = true
                            }, modifier = Modifier
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "profile",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                    }

                    Text(
                        text = "When in danger shout 'Save Me'",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                    MyTextField(
                        modifier = Modifier
                            .height(200.dp)
                            .padding(top = 15.dp),

                        label = "Message you want to send!",
                        text = message,
                        shape = MaterialTheme.shapes.large,
                        onTextChange = {
                            message = it
                            saveToPreference("message", it)
                        }
                    )

                    StartButton(
                        name = if (isRecognitionRunning) "Listening, click to stop" else "Start Listening",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        backgroundColor = if (isRecognitionRunning) Color.Green else MaterialTheme.colorScheme.primary,
                        icon = R.drawable.ic_mic,
                        onClick = fun() {
                            startSpeechRecognitionService(context)

                        }
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text("Contacts") },
                        icon = { Icon(Icons.Filled.AccountBox, contentDescription = "") },
                        onClick = {
                            onNavigateToContacts()
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(0.4f)
                    )
                }

            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun Form() {
    val isRecognitionRunning = false;

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.secondary)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {

            Text(
                text = "HI!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            )
            MyTextField(
                modifier = Modifier
                    .height(200.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 15.dp),

                label = "Message you want to send!",
                text = "",
                shape = MaterialTheme.shapes.large,

                onTextChange = {
//            message = it
//            saveToPreference("message", it)
                }
            )

            Text(
                text = "When in danger shout 'Save Me'",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)
            )
            StartButton(
                name = if (isRecognitionRunning) "Listening, click to stop" else "Start Listening",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 20.dp),
                backgroundColor = if (isRecognitionRunning) Color.Green else MaterialTheme.colorScheme.primary,
                icon = R.drawable.ic_mic,
                onClick = fun() {
                }
            )
        }

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    topStart = 28.0.dp,
                    topEnd = 28.0.dp,
                    bottomEnd = 0.0.dp,
                    bottomStart = 0.0.dp
                )
            ),
        ) {
            Text(
                text = "View and Add Contacts",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
            )
            Text(
                text = "Add SMS contacts and emails to send emergency message to.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)

            )
            Spacer(modifier = Modifier.size(24.dp))
            StartButton(
                name = "View Contacts",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                backgroundColor = MaterialTheme.colorScheme.secondary,
                onClick = fun() {
                }
            )

        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SaveMeAlerterTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "When in danger shout 'Save Me'")

        }
    }
}

@Composable
fun TextureOnBackground(
    texture: ImageBitmap,
    backgroundColor: Color,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)
        drawImage(texture, Offset.Zero)
    }
}

