package com.gray.vulf.savemealerter.data.models

import kotlinx.serialization.Serializable


@Serializable
data class EmailContact(
    val name: String,
    val email: String
)

