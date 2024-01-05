@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.soocer.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )


            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(48.dp),
                onClick = {
                    //TODO validate input fields
                    if(email.isNotEmpty() && password.isNotEmpty()) signViewModel.login(email, password){
                        CoroutineScope(Dispatchers.Main).launch {
                            //withContext(Dispatchers.Main) {
                                navController.navigate(Screens.Home.route)
                           // }
                        }
                    }
                    email = ""
                    password = ""
                    Log.d("bt clicked","")
                    //navController.navigate(Screens.Home.route)
                },
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(48.dp),
                onClick = {
                    if(email.isNotEmpty() && password.isNotEmpty()) signViewModel.register(email, password){
                        navController.navigate(Screens.Home.route)
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
            LocationServiceControls(appContext, startService)

        }
    }
}

@Composable
fun LocationServiceControls(appContext: Context, startService: (Intent) -> ComponentName?) {
    Button(onClick = {
        Intent(appContext,LocationService::class.java).apply{
            action = LocationService.ACTION_START
            startService(this)
        }
    }) {
        Text(text = "Start")
    }

    Button(onClick = {
        Intent(appContext,LocationService::class.java).apply{
            action = LocationService.ACTION_STOP
            startService(this)
        }
    }) {
        Text(text = "Stop")
    }
}


@Composable
fun showToast() {
    Toast.makeText(LocalContext.current, "Erro no login", Toast.LENGTH_SHORT).show()
}

/*
@Preview
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    SignInScreen(navController)
}*/
