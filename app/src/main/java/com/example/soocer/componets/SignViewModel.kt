package com.example.soocer.componets

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

sealed interface SignUiState {
    object Success : SignUiState
    object Error : SignUiState
    object Loading : SignUiState
}

class SignViewModel : ViewModel() {
    var signsUiState: SignUiState by mutableStateOf(SignUiState.Loading)
        private set

    fun login(email: String, password: String){
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                Log.d("LOGIN","Inside_login_success")
                Log.d("LOGIN","${it.isSuccessful}")

                if(it.isSuccessful){
                    SignUiState.Success
                }
            }
            .addOnFailureListener {
                Log.d("LOGIN","Inside_login_failure")
                Log.d("LOGIN","${it.localizedMessage}")

                SignUiState.Error
            }
    }
}