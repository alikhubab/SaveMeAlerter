package com.gray.vulf.savemealerter.data.models

import kotlinx.serialization.Serializable


@Serializable
data class EmailPassword(
    val email: String,
    val password: String
)
