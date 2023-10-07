package com.gray.vulf.savemealerter.ui

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gray.vulf.savemealerter.R
import com.gray.vulf.savemealerter.SpeechRecognitionService
import com.gray.vulf.savemealerter.TAG
import com.gray.vulf.savemealerter.ui.theme.SaveMeAlerterTheme


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Contacts(
    onNavigateBack: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var tabState by remember { mutableStateOf(0) }

    Log.e("Contacts>>", navController.currentDestination.toString())
    navController.currentDestination

    val indicator = @Composable { tabPositions: List<TabPosition> ->
        FancyIndicator(
            MaterialTheme.colorScheme.primary,
            Modifier.tabIndicatorOffset(tabPositions[tabState])
        )
    }

    Column {
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,


                )
        }
        PrimaryTabRow(
            selectedTabIndex = tabState,
            divider = {},
            indicator = indicator,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 12.dp)

        ) {
            Tab(selected = tabState == 0,
                modifier = Modifier.padding(vertical = 16.dp),

                onClick = { tabState = 0 }) {
                Text(
                    text = "Contacts",
                    color = if (tabState == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            Tab(selected = tabState == 1,
                modifier = Modifier.padding(vertical = 16.dp),
                onClick = { tabState = 1 }) {
                Text(
                    text = "Emails",
                    color = if (tabState == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
        }
        when (tabState) {
            0 -> SmsContacts()
            1 -> EmailContacts()
        }
    }

}

@Composable
@Preview
fun FancyIndicator(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .padding(5.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = color, shape = MaterialTheme.shapes.small)
            .zIndex(-1f)
    )
}







