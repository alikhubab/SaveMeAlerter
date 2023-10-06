package com.gray.vulf.savemealerter.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.JsonWriter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.gray.vulf.savemealerter.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.json.JSONTokener


const val TAG = "SmsContacts"

@SuppressLint("Range")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun SmsContacts() {
    val context = LocalContext.current;

    val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)

    @Serializable
    data class Contact(
        val name: String,
        val phone: String
    )

    //    val contactPicker = ContactPicker(activity = context as Activity);
    var contacts by remember {
        var cnl: MutableList<Contact>
        val jsonContacts = sharedPreferences.getString("smsContacts", "");
        cnl = if (jsonContacts.isNullOrBlank()) {
            mutableListOf()
        } else {
            Json.decodeFromString<MutableList<Contact>>(jsonContacts)
        }

        mutableStateOf(
            cnl
        )
    }


    fun saveContactListToSharedPrefs(contacts: List<Contact>) {
        val sharedPreferences = context.getSharedPreferences("SmaPrefs", Context.MODE_PRIVATE)
        val jsonContacts = Json.encodeToString(contacts)
        Log.i("serialized>>", jsonContacts)
        sharedPreferences.edit().putString("smsContacts", jsonContacts).apply()
    }


    val contactsLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickContact()) {
            it?.let {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                )

                val cursor1 = (context as Activity).contentResolver.query(
                    it,
                    null,
                    null,
                    null,
                    null,
                    null,
                )

                cursor1?.use {
                    if (it.moveToFirst()) {
                        //get contact details
                        val contactId =
                            cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID))
                        val contactName =
                            cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val contactThumbnail =
                            cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                        val idResults =
                            cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        val idResultHold = idResults.toInt()

                        Log.i("contactId", "$contactId")
                        Log.i("contactName", "$contactName")
                        Log.i("contactThumbnail", "$contactThumbnail")
                        Log.i("idResults", "$idResults")
                        Log.i("idResultHold", "$idResultHold")

                        if (idResultHold == 1) {
                            val cursor2 = (context as Activity).contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                null,
                                null
                            )

                            var contactNumber: String? = null;
                            if (cursor2 != null) {
                                while (cursor2.moveToNext()) {
                                    contactNumber = cursor2.getString(
                                        cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    )

                                    Log.i("Number", "$contactNumber")
                                }
                                if (!contactNumber.isNullOrBlank()) {
                                    contacts = (contacts + Contact(
                                        name = contactName,
                                        phone = contactNumber
                                    )).toMutableList()
                                    saveContactListToSharedPrefs(contacts)
                                }
                                Log.i("contacts after adding>>", contacts.size.toString())
                            }
                        }
                    }
                }
            }
            Log.e(TAG, it.toString())
        }


    fun handleContactPick() {


//        startActivityForResult(context as Activity, pickContact, 1, null);
        contactsLauncher.launch()
//        contactPicker.pickContact()
    }


    Box {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(32.dp)

        ) {
            itemsIndexed(contacts) { _, item ->
                ContactItem(name = item.name, phone = item.phone)
                Spacer(modifier = Modifier.size(8.dp))

            }
        }

        FloatingActionButton(
            onClick = { handleContactPick() },
            modifier = Modifier
                .padding(all = 32.dp)
                .align(alignment = Alignment.BottomEnd)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")

        }
    }

}

@Preview(showBackground = true)
@Composable
fun ContactItem(name: String = "", phone: String = "") {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.extraLarge)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Text(text = phone, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


class ContactPicker(activity: Activity) {
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName

    private val activityResultLauncher = (activity as ComponentActivity).registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { contactUri ->
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = activity.contentResolver.query(
                    contactUri,
                    projection,
                    null,
                    null,
                    null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameColumnIndex =
                            it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = it.getString(nameColumnIndex)
                        _contactName.value = name
                    }
                }
            }
        }
    }

    fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        activityResultLauncher.launch(intent)
    }
}



