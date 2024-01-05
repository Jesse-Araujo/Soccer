package com.example.soocer.components

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.soocer.auxiliary.Global
import com.google.firebase.auth.FirebaseAuth

sealed interface SignUiState {
    object Success : SignUiState
    object Error : SignUiState
    object Loading : SignUiState
}

class SignViewModel : ViewModel() {
    var signsUiState: SignUiState by mutableStateOf(SignUiState.Loading)
        private set

    fun login(email: String, password: String,onFinished:() -> Unit){
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnFailureListener {
                Log.d("LOGIN","Inside_login_failure")
                it.localizedMessage?.let { it1 -> Log.d("LOGIN", it1) }

                SignUiState.Error
            }
            .addOnCompleteListener {
                Log.d("LOGIN","Inside_login_complete")
                Log.d("LOGIN","${it.isSuccessful}")

                if(it.isSuccessful){
                    SignUiState.Success
                } else SignUiState.Error
            }.addOnSuccessListener {
                Log.d("LOGIN","${it.user?.uid}")
                Global.userId = it.user?.uid.toString()
                onFinished()
            }

    }

    fun register(email: String, password: String,onFinished:() -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
            Log.d("REGISTER","Email $email registered complete")
            Log.d("REGISTER","${it.isSuccessful}")

        }.addOnFailureListener {
            Log.d("REGISTER","Email $email registered failed")
            it.localizedMessage?.let { it1 -> Log.d("REGISTER", it1) }
            SignUiState.Error
        }.addOnSuccessListener {
                Log.d("REGISTER","${it.user?.uid}")
                Global.userId = it.user?.uid.toString()
                onFinished()
            }
    }
}