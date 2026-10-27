package com.example.frontend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Exact NutriPath app logo provided directly by the user in File Explorer (app_logo.png),
 * placed as it is without artificial distortion or cropping.
 */
@Composable
fun NutriAppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    elevation: Dp = 0.dp,
    showBackgroundCard: Boolean = false
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "NutriPath Logo",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

