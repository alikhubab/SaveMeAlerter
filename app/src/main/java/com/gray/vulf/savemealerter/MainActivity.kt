package com.gray.vulf.savemealerter

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.core.app.ActivityCompat
import com.gray.vulf.savemealerter.ui.theme.SaveMeAlerterTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SpeechRecognitionService::class.java)
        applicationContext.startForegroundService(intent)
        ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), 12);

        startService(intent)


        setContent {
            SaveMeAlerterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Mak")
                    StartButton(name = "Save Me", modifier = Modifier.size(Dp(32F))) {
                        stopService(intent)
                        startService(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun StartButton(name: String, modifier: Modifier = Modifier, onClick: () -> Unit){
    Button(onClick = { onClick() }, modifier = modifier) {
        Text(name)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SaveMeAlerterTheme {
        Greeting("Android")
    }
}