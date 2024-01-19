package com.example.soocer.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.soocer.Screens
import com.example.soocer.auxiliary.Global

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewMarkerWindow(navController: NavController) {
    var showOverallRating by remember { mutableStateOf(true) }
    if (showOverallRating) {
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxSize()
                .padding(bottom = Global.size.dp)
        ) {
            Column(modifier = Modifier.padding(start = 25.dp)) {
                Text(text = "Overall review of this place by our users")
                StarRatingSample()
                Text(text = "Comfort")
                Text(text = "Accessibility")
                Text(text = "Quality")
                Text(text = "Photos...")
                Text(text = "Comments...")
                Text(
                    text = "Make your rating",
                    modifier = Modifier.clickable { showOverallRating = false })
            }
        }
    } else {
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxSize()
                .padding(bottom = Global.size.dp)
        ) {
            var comment by remember { mutableStateOf("") }
            Column(
                modifier = Modifier.fillMaxSize()/*.padding(start = 25.dp)*/,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Global Rating")
                StarRatingSample()
                Text(text = "Comfort")
                StarRatingSample()
                Text(text = "Accessibility")
                StarRatingSample()
                Text(text = "Quality")
                StarRatingSample()
                Text(text = "Comments...")
                PhotoSelectorView()
                TextField(value = comment, onValueChange = { comment = it })
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Submit")
                    }
                    Button(onClick = { showOverallRating = true }) {
                        Text(text = "Back")
                    }
                    Button(onClick = {}) {
                        Text(text = "Gallery")
                    }
                }
            }
        }
    }

}

@Composable
fun uploadPhotos() {

}

@Composable
fun StarRatingSample() {
    var rating by remember { mutableStateOf(5f) } //default rating will be 1
    StarRatingBar(
        maxStars = 5,
        rating = rating,
        onRatingChanged = {
            rating = it
        }
    )
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (12f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0x20FFFFFF)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}