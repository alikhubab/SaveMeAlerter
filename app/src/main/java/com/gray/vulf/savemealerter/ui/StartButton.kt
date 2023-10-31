package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun StartButton(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Int? = null,
    backgroundColor: Color
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        shape = MaterialTheme.shapes.small,

        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(name)
            if (icon != null)
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "Mic",
                    modifier = Modifier.scale(0.8f)
                )
        }
    }
}
