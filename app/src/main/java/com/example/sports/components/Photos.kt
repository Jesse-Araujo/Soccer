package com.example.sports.components


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sports.R
import java.io.IOException


@Composable
fun PhotoSelectorView(context: Context,bitmapFromUser : MutableState<Bitmap?>,maxSelectionCount: Int = 1) {
    val bitmap by remember { mutableStateOf(bitmapFromUser) }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            //selectedImages = listOf(uri)
            if(uri != null) bitmap.value = uriToBitmap(context,uri)
            /*if(uri != null && selectedImages.isNotEmpty()){
                if(selectedImages[0] != null){
                    bitmap.value = uriToBitmap(context, selectedImages[0]!!)
                }
            }*/

            Log.d("uri",uri.toString())
        }
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = if (maxSelectionCount > 1) {
            maxSelectionCount
        } else {
            2
        }),
        onResult = { uris ->
            if(uris.isNotEmpty()) {
                bitmap.value = uriToBitmap(context,uris[0])
            }
           // selectedImages = uris
     }
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
        Row {
            Button(onClick = {
                launchPhotoPicker()
            }, colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Image(painter = painterResource(id = R.drawable.picture), contentDescription = "gallery_btn", modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.size(15.dp))
            Button(onClick = {
                bitmap.value = null
            }, colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Image(painter = painterResource(id = R.drawable.delete), contentDescription = "delete_img_btn", modifier = Modifier.size(50.dp))
            }
        }

        ImageLayoutView(context,bitmap = bitmap.value)
    }
}

@Composable
fun ImageLayoutView(context: Context,bitmap: Bitmap?/*List<Uri?>*/) {
    if(/*selectedImages.isNotEmpty()*/ bitmap != null) {
        //LazyRow {
        //items(selectedImages) { uri ->
        //if(selectedImages[0] != null) {
            AsyncImage(
                model = bitmap,//selectedImages[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()/*width(350.dp)*/
                    .height(400.dp)
                    .padding(top = 15.dp, bottom = 15.dp),//.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
       //}

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