@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.soocer.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Visibility
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soocer.R
import com.example.soocer.Screens
import com.example.soocer.location.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun SignInScreen(
    navController: NavController,
    appContext: Context,
    startService: (Intent) -> ComponentName?
){

    var email by remember { mutableStateOf("z@x.com") }
    var password by remember { mutableStateOf("qwerty") }
    val signViewModel: SignViewModel = viewModel()
        var loading by remember { mutableStateOf(false) }

    var showPassword: Boolean by remember { mutableStateOf(value = false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(loading) {
                Image(painter = painterResource(id = R.drawable.loading), contentDescription = "loadingSignIn", modifier = Modifier.size(50.dp))
            }
            if(signViewModel.error) {
                loading = false
                Text(text = signViewModel.errorMessage,
                    color = Color.Red)

            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (showPassword) {

                    VisualTransformation.None

                } else {

                    PasswordVisualTransformation()

                },
                trailingIcon = {
                    if (showPassword) {
                        IconButton(onClick = { showPassword = false }) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "hide_password"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showPassword = true }) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "hide_password"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier
                    .heightIn(48.dp)
                    .width(280.dp),
                onClick = {
                    loading = true
                    //TODO validate input fields
                    if(email.isNotEmpty() && password.isNotEmpty()) signViewModel.login(appContext,email, password){
                        CoroutineScope(Dispatchers.Main).launch {
                            //withContext(Dispatchers.Main) {
                            loading = false
                            navController.navigate(Screens.Home.route) {
                                popUpTo(0)
                            }
                           // }
                        }
                    }

                    email = ""
                    password = ""
                    Log.d("bt clicked","")
                    //navController.navigate(Screens.Home.route)
                },
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(7.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                modifier = Modifier
                    .heightIn(48.dp),
                onClick = {
                    loading = true
                    if(email.isNotEmpty() && password.isNotEmpty()) signViewModel.register(email, password){
                        loading = false
                        navController.navigate(Screens.Home.route) {
                            popUpTo(0)
                        }
                    }
                    email = ""
                    password = ""
                    Log.d("registered btn clicked","")
                    //navController.navigate(Screens.Home.route)
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Register",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

            }
        }
    }
}

/*
@Preview
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    SignInScreen(navController)
}*/
