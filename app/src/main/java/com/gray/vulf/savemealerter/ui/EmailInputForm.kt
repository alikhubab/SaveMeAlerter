package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun EmailInputForm(onSubmit: (email: String, name: String) -> Unit = { e, n -> }) {
    var email by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }



    Column(modifier = Modifier.padding(32.dp)) {
        Text(
            text = "Add an email",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "This could be your relative, friend, or a family member. Someone who could help you.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight(300)
        )
        Spacer(Modifier.size(16.dp))


        MyTextField(
            text = email, label = "Enter Email", onTextChange = { email = it },
        )
        Spacer(Modifier.size(8.dp))
        MyTextField(text = name, label = "Enter Name", onTextChange = { name = it })
        Spacer(Modifier.size(24.dp))

        StartButton(
            name = "Save",
            onClick = {
                onSubmit(email.trim(), name.trim())
            },
            backgroundColor = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(48.dp))

    }
}
