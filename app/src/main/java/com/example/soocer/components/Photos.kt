package com.example.soocer.components


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.IOException


@Composable
fun PhotoSelectorView(context: Context,bitmap : MutableState<Bitmap?>,maxSelectionCount: Int = 1) {
    var selectedImages by remember {
        mutableStateOf<List<Uri?>>(emptyList())
    }

    val buttonText = if (maxSelectionCount > 1) {
        "Select up to $maxSelectionCount photos"
    } else {
        "Select a photo"
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImages = listOf(uri)
            if(uri != null && selectedImages.isNotEmpty()){
                if(selectedImages[0] != null){
                    bitmap.value = uriToBitmap(context, selectedImages[0]!!)
                }
            }

            Log.d("uri",uri.toString())
        }
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = if (maxSelectionCount > 1) {
            maxSelectionCount
        } else {
            2
        }),
        onResult = { uris -> selectedImages = uris }
    )

    fun launchPhotoPicker() {
        if (maxSelectionCount > 1) {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            launchPhotoPicker()
        }) {
            Text(buttonText)
        }

        ImageLayoutView(context,selectedImages = selectedImages)
    }
}

@Composable
fun ImageLayoutView(context: Context,selectedImages: List<Uri?>) {
    if(selectedImages.isNotEmpty()) {
        //LazyRow {
        //items(selectedImages) { uri ->
        if(selectedImages[0] != null) {
            AsyncImage(
                model = uriToBitmap(context,selectedImages[0]!!),//selectedImages[0],
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()/*width(350.dp)*/.height(200.dp),//.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }

        // }
        //}
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun bitmapToString(bitmap: Bitmap?): String {
    if(bitmap == null) return ""
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun stringToBitmap(encodedString: String): Bitmap? {
    val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}
/*
fun bitmapToString(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
    val byteArray = outputStream.toByteArray()
    return org.apache.commons.codec.binary.Base64().encode(byteArray).toString(Charsets.UTF_8)
}*/