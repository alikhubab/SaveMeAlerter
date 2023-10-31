package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun SenderEmailInputForm(
    currentEmail: String = "",
    currentPassword: String = "",
    loading: Boolean = false,
    onSubmit: (email: String, password: String) -> Unit = { e, n -> }
) {
    var email by remember {
        mutableStateOf(currentEmail)
    }
    var password by remember {
        mutableStateOf(currentPassword)
    }
    Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(modifier = Modifier.size(64.dp))
        }
        Text(
            text = "The email from which you want to send the emergency email.",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "This requires an App Password for Gmail. Please visit www.google.com/app-passwords to create one.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight(300)
        )
        Spacer(Modifier.size(16.dp))

        MyTextField(text = email, label = "Enter Email", onTextChange = { email = it })
        Spacer(Modifier.size(8.dp))
        MyTextField(text = password, label = "Enter Password", onTextChange = { password = it })
        Spacer(Modifier.size(24.dp))
        StartButton(
            name = "Save",
            onClick = { if (!loading) onSubmit(email.trim(), password.trim()) },
            backgroundColor = if (loading) MaterialTheme.colorScheme.surfaceDim else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(48.dp))


    }
}
