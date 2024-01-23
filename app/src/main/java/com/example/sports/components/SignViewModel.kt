package com.example.sports.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.sports.auxiliary.Global
import com.example.sports.auxiliary.writeIdToFile
import com.google.firebase.auth.FirebaseAuth


sealed interface SignUiState {
    object Success : SignUiState
    object Error : SignUiState
    object Loading : SignUiState
}

class SignViewModel : ViewModel() {
    var signsUiState: SignUiState by mutableStateOf(SignUiState.Loading)
        private set
    var error: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String by mutableStateOf("")
        private set

    fun login(context:Context,email: String, password: String,onFinished:() -> Unit){
        FirebaseAuth
            .getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnFailureListener {
                Log.d("LOGIN","Inside_login_failure")
                it.localizedMessage?.let { it1 -> Log.d("LOGIN", it1) }

                error = true
                errorMessage = "something went wrong"
            }
            .addOnCompleteListener {
                Log.d("LOGIN","Inside_login_complete")
                Log.d("LOGIN","${it.isSuccessful}")


                if(it.isSuccessful){
                    error = false
                    errorMessage = ""
                    SignUiState.Success
                } else{
                    error = true
                    errorMessage = "Invalid credentials"
                }
            }.addOnSuccessListener {
                Log.d("LOGIN","${it.user?.uid}")
                Log.d("cred",it.credential.toString())
                Log.d("cred prov",it.credential?.provider.toString())
                Log.d("prov id",it.additionalUserInfo?.providerId.toString())
                writeIdToFile(context,it.user?.uid.toString())
                Global.userId = it.user?.uid.toString()
                onFinished()
            }

    }

    fun register(email: String, password: String,onFinished:() -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
            Log.d("REGISTER","Email $email registered complete")
            Log.d("REGISTER","${it.isSuccessful}")
            error = false
            errorMessage = ""
        }.addOnFailureListener {
            Log.d("REGISTER","Email $email registered failed")
            it.localizedMessage?.let { it1 -> Log.d("REGISTER", it1) }
            SignUiState.Error
            error = true
            errorMessage = "something went wrong"
        }.addOnSuccessListener {
                Log.d("REGISTER","${it.user?.uid}")
                Global.userId = it.user?.uid.toString()
                onFinished()
            }
    }
}