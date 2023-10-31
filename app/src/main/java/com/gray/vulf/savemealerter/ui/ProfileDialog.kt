package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gray.vulf.savemealerter.R

@Preview
@Composable
fun ProfileDialog(
    onDismissRequest: () -> Unit = {},
    onSyncDataRequest: () -> Unit = {},
    syncingContacts: Boolean = false,
    onLogoutRequest: () -> Unit = {},
    onLoginRequest: () -> Unit = {},
    painter: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
    imageDescription: String = "Lalal",
    userEmail: String? = null
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "close modal",
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "profile",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (userEmail.isNullOrBlank())
                            "You are not logged In. Login to sync your contacts" else
                            "Logged in as ${userEmail}",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (!userEmail.isNullOrBlank())
                            if (syncingContacts)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .width(32.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        trackColor = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            else
                                TextButton(
                                    onClick = { onSyncDataRequest() },
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    Text("Sync Data")
                                }
                    }
                }
                TextButton(
                    onClick = {
                        if (userEmail.isNullOrBlank()) {
                            onLoginRequest()
                        } else {
                            onLogoutRequest()
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(text = if (userEmail.isNullOrBlank()) "Login" else "Logout")
                }
            }
        }
    }
}