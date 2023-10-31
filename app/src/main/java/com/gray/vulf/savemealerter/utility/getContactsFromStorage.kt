package com.gray.vulf.savemealerter.utility

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json

inline fun <reified K> getContactsListFromStorage(
    contactType: String,
    sharedPreferences: SharedPreferences
): List<K> {
    val contacts: List<K>
    val jsonContacts = sharedPreferences.getString(contactType, "");

    contacts = if (jsonContacts.isNullOrBlank()) {
        listOf()
    } else {
        Json.decodeFromString<List<K>>(jsonContacts)
    }
    return contacts
}