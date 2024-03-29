package com.example.sports.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sports.R
import com.example.sports.Screens
import com.example.sports.auxiliary.Global

@Composable
fun BottomNavigator(navController: NavController) {
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!navController.currentDestination
                                    .toString()
                                    .contains(Screens.Home.route)
                            ) navController.navigate(Screens.Home.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_home),
                        contentDescription = "Home icon",
                        modifier = Modifier
                            .size(Global.size.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!navController.currentDestination
                                    .toString()
                                    .contains(Screens.Map.route)
                            ) navController.navigate(Screens.Map.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_map),
                        contentDescription = "Map icon",
                        modifier = Modifier
                            .size(Global.size.dp)
                    )
                }
            }
        }
    }
}