package com.example.sports.components

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sports.R
import com.example.sports.Screens
import com.example.sports.auxiliary.Global
import com.example.sports.data.FirebaseFunctions
import com.example.sports.data.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewMarkerWindow(navController: NavController, context: Context, markerName: String) {
    val markerReview = remember { mutableStateOf(Review(markerName, 5, 5, 5, 5, "", "")) }
    val userReview = remember { mutableStateOf(Review(markerName, 5, 5, 5, 5, "", "")) }
    val oldUserReview = remember { mutableStateOf(Review(markerName, 5, 5, 5, 5, "", "")) }
    val globalRating = remember { mutableStateOf(markerReview.value.globalRating) }
    val markerComfort = remember { mutableStateOf(markerReview.value.comfort) }
    val markerAccessibility = remember { mutableStateOf(markerReview.value.accessibility) }
    val markerQuality = remember { mutableStateOf(markerReview.value.quality) }
    /*val markerComments = remember { mutableStateOf(hashSetOf<String>()) }
    val markerPhotos = remember { mutableStateOf(mutableListOf<Bitmap>()) }*/
    val reviews = remember { mutableStateOf(mutableListOf<Triple<String, String, Bitmap?>>()) }
    var userHasAlreadyReviewedThisMarker by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFunctions.getMarkerReview(markerName) { review, triple ->
                Log.d("xiu", "xiu")
                globalRating.value = review.globalRating
                markerComfort.value = review.comfort
                markerAccessibility.value = review.accessibility
                markerQuality.value = review.quality
                markerReview.value = review
                reviews.value = triple.toMutableList()
                /*markerComments.value = comments
                markerPhotos.value = photos*/
            }
        }
    }
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFunctions.getUserReview(markerName) { review, bol ->
                userReview.value = review
                oldUserReview.value = review
                userHasAlreadyReviewedThisMarker = bol
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        val showOverallRating = remember { mutableStateOf(true) }
        if (showOverallRating.value) {
            MarkerReview(
                context = context,
                navController = navController,
                markerReview = markerReview,
                showOverallRating = showOverallRating,
                reviews = reviews,
                globalRating = globalRating,
                comfort = markerComfort,
                accessibility = markerAccessibility,
                quality = markerQuality,
                markerName = markerName
            )
        } else {
            UserReview(
                context = context,
                navController = navController,
                markerName = markerName,
                globalReview = markerReview,
                userReview = userReview,
                showOverallRating = showOverallRating,
                oldUserReview = oldUserReview,
                userHasAlreadyReviewedThisMarker = userHasAlreadyReviewedThisMarker,
            )
        }
        BottomNavigator(navController = navController)
    }

}


@Composable
fun MarkerReview(
    context: Context,
    navController: NavController,
    markerReview: MutableState<Review>,
    reviews: MutableState<MutableList<Triple<String, String, Bitmap?>>>,
    showOverallRating: MutableState<Boolean>,
    globalRating: MutableState<Int>,
    comfort: MutableState<Int>,
    accessibility: MutableState<Int>,
    quality: MutableState<Int>,
    markerName: String
) {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize()
            .padding(bottom = (Global.size).dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()/*.padding(start = 25.dp)*/,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = markerName,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 15.dp)
            )
            Text(text = "Overall review of this place by our users")
            StarRatingSample(globalRating, false)

            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Comfort", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(comfort, false)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Accessibility", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(accessibility, false)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Atmosphere", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(quality, false)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "User reviews", Modifier.padding(start = 25.dp))
            }
            ReviewsBox(reviews = reviews.value)
            Button(onClick = { showOverallRating.value = false },) {
                Text(text = "Make your rating")
            }
        }
    }
}

@Composable
fun ReviewsBox(reviews: MutableList<Triple<String, String, Bitmap?>>) {
    var showReviews = true
    reviews.forEach {if(it.second.isEmpty() && it.third == null) showReviews = false }


    if (reviews.isNotEmpty() || showReviews) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(bottom = 15.dp),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.Center
            ) {
                items(reviews.toList()) { review ->
                    if(review.second.isNotEmpty() || review.third != null){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 15.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 15.dp)
                                    .fillMaxSize()
                            ) {
                                Row(Modifier.padding(top = 10.dp, start = 10.dp)) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_user),
                                        contentDescription = "ic_user",
                                        Modifier.size(25.dp)
                                    )
                                    Text(
                                        text = review.first,
                                        modifier = Modifier.padding(start = 10.dp, end = 5.dp)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.star),
                                        contentDescription = "ic_star",
                                        Modifier.size(20.dp)
                                    )
                                }
                                Text(text = review.second, modifier = Modifier.padding(16.dp))
                                if (review.third != null) {
                                    AsyncImage(
                                        model = review.third,
                                        contentDescription = review.first + review.second,
                                        modifier = Modifier
                                            .padding(start = 25.dp, end = 25.dp)
                                            .height(220.dp)
                                            .width(350.dp),
                                        alignment = Alignment.Center,
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReview(
    context: Context,
    navController: NavController,
    markerName: String,
    globalReview: MutableState<Review>,
    userReview: MutableState<Review>,
    showOverallRating: MutableState<Boolean>,
    oldUserReview: MutableState<Review>,
    userHasAlreadyReviewedThisMarker: Boolean
) {
    val globalRating = remember { mutableStateOf(userReview.value.globalRating) }
    val comfort = remember { mutableStateOf(userReview.value.comfort) }
    val accessibility = remember { mutableStateOf(userReview.value.accessibility) }
    val quality = remember { mutableStateOf(userReview.value.quality) }
    val comment = remember { mutableStateOf(userReview.value.comment) }
    val photo = remember { mutableStateOf(userReview.value.photo) }
    val bitmap =
        remember { mutableStateOf(if (photo.value.isNotEmpty()) stringToBitmap(photo.value) else null) }
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize()
            .padding(bottom = Global.size.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())/*.padding(start = 25.dp)*/,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Global Rating", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(globalRating)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Comfort", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(comfort)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Accessibility", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(accessibility)
            Row (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)){
                Text(text = "Atmosphere", Modifier.padding(start = 25.dp))
            }
            StarRatingSample(quality)
            Spacer(modifier = Modifier.size(10.dp))
            PhotoSelectorView(context, bitmap)
            val maxLines = 5
            val maxChars = 100
            TextField(
                value = comment.value,
                onValueChange = { //comment.value = it
                    val lines = it.count { ch -> ch == '\n' } + 1
                    if (lines <= maxLines && comment.value.length < maxChars) {
                        comment.value = it
                    }
                },
                /*keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),*/
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    submit(
                        oldUserReview.value,
                        globalReview,
                        userHasAlreadyReviewedThisMarker,
                        markerName,
                        globalRating,
                        comfort,
                        accessibility,
                        quality,
                        comment,
                        bitmap,
                        showOverallRating,
                        userReview,
                        navController
                    )
                }) {
                    Text(text = "Submit")
                }
                Spacer(modifier = Modifier.size(15.dp))
                Button(onClick = { showOverallRating.value = true }) {
                    Text(text = "Back")
                }
            }
        }
    }
}

fun submit(
    oldReview: Review,
    globalReview: MutableState<Review>,
    userHasAlreadyReviewedThisMarker: Boolean,
    markerName: String,
    globalRating: MutableState<Int>,
    comfort: MutableState<Int>,
    accessibility: MutableState<Int>,
    quality: MutableState<Int>,
    comment: MutableState<String>,
    bitmap: MutableState<Bitmap?>,
    showOverallRating: MutableState<Boolean>,
    userReview: MutableState<Review>,
    navController: NavController,

    ) {
    CoroutineScope(Dispatchers.IO).launch {
        val currentReview = Review(
            markerName,
            globalRating.value,
            comfort.value,
            accessibility.value,
            quality.value,
            comment.value,
            bitmapToString(bitmap.value)
        )
        FirebaseFunctions.updateMarkerReview(
            currentReview,
            userHasAlreadyReviewedThisMarker,
            oldReview
        ) {
            FirebaseFunctions.saveUserReview(
                Review(
                    markerName,
                    currentReview.globalRating,
                    currentReview.comfort,
                    currentReview.accessibility,
                    currentReview.quality,
                    currentReview.comment,
                    currentReview.photo
                )
            ) {
                navController.popBackStack(Screens.Review.route, true)
                navController.navigate(
                    Screens.Review.route.replace(
                        oldValue = "{markerName}",
                        newValue = markerName
                    )
                )
            }

        }
    }


}

@Composable
fun StarRatingSample(rating: MutableState<Int>, isClickable: Boolean = true) {
    StarRatingBar(
        maxStars = 10,
        rating = rating,
        isClickable = isClickable,
        onRatingChanged = {
            if (isClickable) rating.value = it.toInt()
        }
    )
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: MutableState<Int>,
    isClickable: Boolean,
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
            val isSelected = i <= rating.value
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor =
                if (isSelected) Color(0xFFFFC700) else Color.White//Color(0x20FFFFFF)
            var mod = Modifier
                .selectable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    selected = isSelected,
                    onClick = {
                        onRatingChanged(i.toFloat())
                    }
                )
                .width(starSize)
                .height(starSize)
            if (isClickable) {
                mod = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize)
                    .height(starSize)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = mod
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}