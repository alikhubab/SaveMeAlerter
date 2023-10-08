package com.gray.vulf.savemealerter.data.models

data class SaveMeMessage(
    val message: String,
    val sender: EmailPassword,
    val mailTargets: List<EmailContact>,
    val phoneTargets: List<PhoneContact>
)