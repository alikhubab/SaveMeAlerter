package com.gray.vulf.savemealerter.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gray.vulf.savemealerter.R
import com.gray.vulf.savemealerter.data.models.EmailContact
import com.gray.vulf.savemealerter.data.models.EmailPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Properties
import java.util.concurrent.CancellationException
import javax.mail.AuthenticationFailedException
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import com.gray.vulf.savemealerter.utility.isValidEmail

fun authenticateEmail(email: String, password: String): Boolean {
    try {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com" // Replace with your SMTP server
        props["mail.smtp.port"] = "587" // Replace with the appropriate port

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })
        val transport = session.getTransport("smtp")
        transport.connect("smtp.gmail.com", email, password)
        return true
    } catch (e: AuthenticationFailedException) {
        Log.e("authenticateEmail>>authenticationExn", e.toString())
        return false
    } catch (e: Exception) {
        Log.e("authenticateEmail>>otherException", e.toString())
        return false
    }
}

@SuppressLint("Range")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun EmailContacts() {
    val context = LocalContext.current;
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var openDeleteAlertDialog = remember {
        mutableStateOf(false)
    }
    var itemIdToBeDeleted by remember {
        mutableStateOf(0)
    }

    val senderEmailInputSheetState = rememberModalBottomSheetState()
    var showSenderEmailInputBottomSheet by remember {
        mutableStateOf(false)
    }
    var emailSheetLoading by remember {
        mutableStateOf(false)
    }

    val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)


    //    val contactPicker = ContactPicker(activity = context as Activity);
    var emailContacts by remember {
        var cnl: MutableList<EmailContact>
        val jsonContacts = sharedPreferences.getString("emailContacts", "")
        cnl = if (jsonContacts.isNullOrBlank()) {
            mutableListOf()
        } else {
            Json.decodeFromString<MutableList<EmailContact>>(jsonContacts)
        }

        mutableStateOf(
            cnl
        )
    }

    var senderEmailPassword by remember {
        val ep: EmailPassword
        val jsonEp = sharedPreferences.getString("senderEmailPassword", "")
        ep = if (jsonEp.isNullOrBlank()) {
            EmailPassword(email = "alikhubab6@gmail.com", password = "okhwkeckgfdpuunk")
        } else {
            Json.decodeFromString<EmailPassword>(jsonEp)
        }
        mutableStateOf(ep)
    }


    fun saveContactListToSharedPrefs(emailContacts: List<EmailContact>) {
        val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val jsonContacts = Json.encodeToString<List<EmailContact>>(emailContacts)
        Log.i("serialized>>", jsonContacts)
        sharedPreferences.edit().putString("emailContacts", jsonContacts).apply()
    }

    fun saveSenderEmailPasswordToSharedPrefs(emailPassword: EmailPassword) {
        val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val jsonEp = Json.encodeToString<EmailPassword>(emailPassword)
        sharedPreferences.edit().putString("senderEmailPassword", jsonEp).apply()
    }


    fun handleAddEmail(name: String, email: String) {
        emailContacts = (emailContacts + EmailContact(name.trim(), email.trim())).toMutableList()
        saveContactListToSharedPrefs(emailContacts)
    }

    fun handleRemoveEmail(index: Int) {
        emailContacts.removeAt(index)
        saveContactListToSharedPrefs(emailContacts)
    }


    fun handleUpdateSenderEmail(email: String, password: String) {
        senderEmailPassword = EmailPassword(email, password)
        saveSenderEmailPasswordToSharedPrefs(senderEmailPassword)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Email") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    if (emailContacts.size >= 5) {
                        Toast.makeText(context, "You can only add five emails.", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        showBottomSheet = true
                    }
                }
            )
        },

        ) { contentPadding ->

        when {
            openDeleteAlertDialog.value -> {
                DeleteDialog(
                    onDismissRequest = {
                        openDeleteAlertDialog.value = false
                    },
                    onConfirmation = {
                        handleRemoveEmail(itemIdToBeDeleted)
                        openDeleteAlertDialog.value = false
                    },
                )
            }
        }

        Column {
            SenderEmailCard(email = senderEmailPassword.email) {
                showSenderEmailInputBottomSheet = true
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(32.dp)

            ) {
                itemsIndexed(emailContacts) { index, item ->
                    EmailItem(name = item.name, email = item.email, id = index) {
                        itemIdToBeDeleted = index
                        openDeleteAlertDialog.value = true
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }


        }

        if (showSenderEmailInputBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSenderEmailInputBottomSheet = false
                },
                sheetState = senderEmailInputSheetState,
                modifier = Modifier.padding(contentPadding)
            ) {
                // Sheet content
                SenderEmailInputForm(
                    currentEmail = senderEmailPassword.email,
                    currentPassword = senderEmailPassword.password,
                    loading = emailSheetLoading,
                ) { email, password ->
                    emailSheetLoading = true
                    scope.launch(Dispatchers.IO) {
                        val result = authenticateEmail(email, password)
                        if (result) {
                            handleUpdateSenderEmail(email, password)
                            sheetState.hide()
                        } else {
                            this.cancel(CancellationException("The email or password in incorrect"))
                        }
                        Log.i("Authentication Result>>>", result.toString())
                    }.invokeOnCompletion {
                        emailSheetLoading = false
                        if (it == null) {
                            if (!sheetState.isVisible) {
                                showSenderEmailInputBottomSheet = false
                            }
                        } else {
                            (context as Activity).runOnUiThread {
                                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                modifier = Modifier.padding(contentPadding)
            ) {
                // Sheet content
                EmailInputForm { email, name ->
                    if (!isValidEmail(email)) {
                        Toast.makeText(
                            context,
                            "Please enter a valid email.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else if (name.length == 0) {
                        Toast.makeText(context, "Please enter a name.", Toast.LENGTH_SHORT).show()
                    } else {
                        handleAddEmail(name, email)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                }
            }
        }
    }
}





