package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SenderEmailCard(email: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceDim,
                shape = MaterialTheme.shapes.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(
                text = "Sender Email and Password",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight(700)
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight(500)
            )
            Text(
                text = "********************",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight(500)


            )
            Text(
                text = "This requires an App Password for Gmail. Please visit www.google.com/app-passwords to create one.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight(300)

            )
        }
        IconButton(
            onClick = { onEdit() },
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Icon(
                Icons.Filled.Create, contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }


    }

}
