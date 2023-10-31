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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gray.vulf.savemealerter.R
import com.gray.vulf.savemealerter.SpeechRecognitionService
import com.gray.vulf.savemealerter.TAG
import com.gray.vulf.savemealerter.data.models.Contacts
import com.gray.vulf.savemealerter.data.models.EmailContact
import com.gray.vulf.savemealerter.data.models.PhoneContact
import com.gray.vulf.savemealerter.ui.theme.SaveMeAlerterTheme
import com.gray.vulf.savemealerter.utility.getContactsListFromStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmaApp(
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = Firebase.auth
    val db = Firebase.firestore
    val scope = rememberCoroutineScope()

    var syncingContacts by remember {
        mutableStateOf(false)
    }

    fun mergeContacts(localContacts: Contacts, dbContacts: Contacts): Contacts {
        val mergedEmailContacts = dbContacts?.emailContacts?.plus(localContacts.emailContacts)
        val mergedPhoneContacts = dbContacts?.phoneContacts?.plus(localContacts.phoneContacts)

        val distinctEmailContacts = mergedEmailContacts?.distinctBy {
            it.email
        }

        val distinctPhoneContacts = mergedPhoneContacts?.distinctBy {
            it.phone
        }
        return Contacts(
            emailContacts = distinctEmailContacts ?: listOf(),
            phoneContacts = distinctPhoneContacts ?: listOf()
        )
    }

    fun getContactsFromStorage(): Contacts {
        val sharedPrefs = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val localEmailContacts =
            getContactsListFromStorage<EmailContact>("emailContacts", sharedPrefs)
        val localPhoneContacts =
            getContactsListFromStorage<PhoneContact>("smsContacts", sharedPrefs)


        return Contacts(localEmailContacts, localPhoneContacts)
    }

    fun saveContactListToStorage(contacts: Contacts) {
        val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)

        val jsonEmailContacts = Json.encodeToString<List<EmailContact>>(contacts.emailContacts)
        val jsonPhoneContacts = Json.encodeToString<List<PhoneContact>>(contacts.phoneContacts)
        sharedPreferences.edit().putString("emailContacts", jsonEmailContacts).apply()
        sharedPreferences.edit().putString("smsContacts", jsonPhoneContacts).apply()
    }

    suspend fun syncContacts() {
        auth.currentUser?.uid?.let {
            Log.i(TAG, "Syching Started")
            val doc = db.collection("contacts").document(it).get().await()

            val dbContacts = doc.toObject(Contacts::class.java)
            val localContacts = getContactsFromStorage()

            val mergedContacts =
                mergeContacts(localContacts, dbContacts ?: Contacts())

            saveContactListToStorage(mergedContacts)

            db.collection("contacts").document(it).set(mergedContacts).await()
            Log.i(TAG, "Synching Complete")

            Log.i(TAG, "syncContacts>>mergedEmailContacts>>${mergedContacts.emailContacts}")
            Log.i(TAG, "syncContacts>>mergedPhoneContacts>>${mergedContacts.phoneContacts}")
        }
    }

    if (auth.currentUser != null) {
        LaunchedEffect(key1 = true) {
            scope.launch {
                syncingContacts = true
                syncContacts()
                syncingContacts = false
            }
        }
    }


    // Get a list of currently running services

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            Home(onNavigateToContacts = {
                navController.navigate("contacts")
            }, onNavigateToAuth = {
                navController.navigate("auth")
            }, onContactsSync = {
                scope.launch {
                    syncingContacts = true
                    syncContacts()
                    syncingContacts = false
                }
            },
                syncingContacts = syncingContacts
            )
        }
        composable("contacts") {
            Contacts(onNavigateBack = {
                navController.popBackStack()
            },
                onNavigateToAuth = {

                }
            )
        }

        composable("auth") {
            Auth(onNavigateBack = {
                navController.popBackStack()
            },
                onAuthSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }

}







