package com.gray.vulf.savemealerter.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showBackground = true)
@Composable
fun MyTextField(
    modifier: Modifier = Modifier,
    label: String = "Enter Text",
    text: String,
    shape: Shape = MaterialTheme.shapes.large,
    onTextChange: (text: String) -> Unit
) {

    TextField(
        value = text,
        onValueChange = { onTextChange(it) },
        label = { Text(label) },
        maxLines = 5,
        shape = shape,
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .fillMaxWidth()

            .then(modifier),


        )
}
