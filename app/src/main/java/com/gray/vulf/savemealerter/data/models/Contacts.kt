package com.gray.vulf.savemealerter.data.models

import kotlinx.serialization.Serializable

data class Contacts(
    val emailContacts: List<EmailContact> = listOf(),
    val phoneContacts: List<PhoneContact> = listOf()
)