package com.gray.vulf.savemealerter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gray.vulf.savemealerter.ui.SmaApp
import com.gray.vulf.savemealerter.ui.theme.SaveMeAlerterTheme


class SplashScreenActivity : ComponentActivity() {
    private val SPLASH_SCREEN_TIME_OUT =
        2000 // After completion of 2000 ms, the next activity will get started.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This method is used so that your splash activity can cover the entire screen.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContent {
            SaveMeAlerterTheme {
                SplashScreen()
            }
        }
        Handler().postDelayed(Runnable {
            val i = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(i) // invoke the SecondActivity.
            finish() // the current activity will get finished.
        }, SPLASH_SCREEN_TIME_OUT.toLong())
    }
}

@Preview
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "Save Me Alerter Logo",
            Modifier.size(240.dp)
        )
    }

}