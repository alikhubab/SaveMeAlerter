package com.gray.vulf.savemealerter.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gray.vulf.savemealerter.R
import com.gray.vulf.savemealerter.utility.isValidEmail
import com.gray.vulf.savemealerter.utility.isValidName
import com.gray.vulf.savemealerter.utility.isValidPassword

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun Login(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current;
    val auth: FirebaseAuth = Firebase.auth

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    fun isInputValid(): Boolean = if (!isValidEmail(email)) {
        Toast.makeText(context, "Email is invalid", Toast.LENGTH_SHORT).show()
        false
    } else if (!isValidPassword(password)) {
        Toast.makeText(
            context,
            "Password must be atleast 6 characters",
            Toast.LENGTH_SHORT
        ).show()
        false
    } else true


    fun handleLoginUser() {
        if (isInputValid()) {
            Log.d("handleLoginUser>>", email + " >" + password)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(context as Activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        onLoginSuccess()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            context,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }


    Column {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            IconButton(
                onClick = { onNavigateBack() },
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,


                    )
            }
            MyTextField(text = email, label = "Email", onTextChange = {
                email = it
            })
            Spacer(Modifier.size(12.dp))

            MyTextField(text = password, label = "Password", onTextChange = {
                password = it
            })
            Spacer(Modifier.size(16.dp))

            StartButton(
                name = "Login",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = fun() {
                    handleLoginUser()
                }
            )
            Spacer(modifier = Modifier.size(12.dp))

            OutlinedButton(
                onClick = { onNavigateToRegister() },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Register")
            }

        }
    }


}