package com.gray.vulf.savemealerter.data.models

import kotlinx.serialization.Serializable


@Serializable
data class PhoneContact(
    val name: String,
    val phone: String
)