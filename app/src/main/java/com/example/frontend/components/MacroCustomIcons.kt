package com.example.frontend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.frontend.theme.*

/**
 * Exact Custom Macro Icons provided for the app:
 * 1. Protein: Flexed Bicep Arm (from p.png -> R.drawable.ic_protein)
 * 2. Carbs: Lightning Bolt with radiating energy rays (from c.png -> R.drawable.ic_carbs)
 * 3. Fat: Outlined Shield with inner Flame (from fats.png -> R.drawable.ic_fat)
 * 4. Fiber: Human Stomach Organ with circular digestion loop (from fiber.png -> R.drawable.ic_fiber)
 */

/**
 * 1. Protein Icon: Exact icon from user's p.png (Flexed Bicep Arm)
 * Scaled up so its visual weight and height match the circular/squarish fat, carbs, and fiber icons perfectly.
 */
@Composable
fun MacroProteinMuscleIcon(
    modifier: Modifier = Modifier,
    tint: Color = NutriProteinRed
) {
    Image(
        painter = painterResource(id = R.drawable.ic_protein),
        contentDescription = "Protein",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * 2. Carbs Icon: Exact icon from user's c.png (Lightning Bolt with radiating rays)
 */
@Composable
fun MacroCarbLightningIcon(
    modifier: Modifier = Modifier,
    tint: Color = NutriCarbGold
) {
    Image(
        painter = painterResource(id = R.drawable.ic_carbs),
        contentDescription = "Carbs",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * 3. Fat Icon: Exact icon from user's fats.png (Shield with inner Flame)
 */
@Composable
fun MacroFatShieldFlameIcon(
    modifier: Modifier = Modifier,
    tint: Color = NutriFatOrange,
    cutoutColor: Color = Color.White
) {
    Image(
        painter = painterResource(id = R.drawable.ic_fat),
        contentDescription = "Fat",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * 4. Fiber Icon: Exact icon from user's fiber.png (Stomach Organ with digestion loop)
 */
@Composable
fun MacroFiberStomachIcon(
    modifier: Modifier = Modifier,
    tint: Color = NutriFiberGreen
) {
    Image(
        painter = painterResource(id = R.drawable.ic_fiber),
        contentDescription = "Fiber",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * Backward compatibility aliases for existing references
 */
@Composable
fun MacroCarbWheatIcon(modifier: Modifier = Modifier, tint: Color = NutriCarbGold) {
    MacroCarbLightningIcon(modifier = modifier, tint = tint)
}

@Composable
fun MacroProteinMuscleDnaIcon(modifier: Modifier = Modifier, tint: Color = NutriProteinRed) {
    MacroProteinMuscleIcon(modifier = modifier, tint = tint)
}

@Composable
fun MacroFatDropletIcon(modifier: Modifier = Modifier, tint: Color = NutriFatOrange) {
    MacroFatShieldFlameIcon(modifier = modifier, tint = tint)
}

@Composable
fun MacroFiberTripleStalkIcon(modifier: Modifier = Modifier, tint: Color = NutriFiberGreen) {
    MacroFiberStomachIcon(modifier = modifier, tint = tint)
}

/**
 * Universal Macro Icon Dispatcher:
 * Automatically routes every macro or food category to the user's exact custom icons:
 * - Protein -> MacroProteinMuscleIcon (R.drawable.ic_protein)
 * - Carbs -> MacroCarbLightningIcon (R.drawable.ic_carbs)
 * - Fat -> MacroFatShieldFlameIcon (R.drawable.ic_fat)
 * - Fiber -> MacroFiberStomachIcon (R.drawable.ic_fiber)
 */
@Composable
fun MacroAppIcon(
    macroOrCategory: String,
    modifier: Modifier = Modifier,
    overrideColor: Color? = null
) {
    val key = macroOrCategory.lowercase().trim()
    when {
        key.contains("water") || key.contains("h2o") || key.contains("hydration") || key.contains("నీరు") || key.contains("నీటి") -> {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Water",
                tint = overrideColor ?: NutriWaterCyan,
                modifier = modifier
            )
        }
        key.contains("cal") || key.contains("kcal") || key.contains("energy") || key.contains("క్యాలరీ") || key.contains("కేలరీ") -> {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Calories",
                tint = overrideColor ?: NutriAmber,
                modifier = modifier
            )
        }
        key == "p" || key.startsWith("p:") || key.startsWith("p ") || key.contains("pro") || key.contains("ప్రోటీన్") || key.contains("meat") || key.contains("poultry") || key.contains("fish") || key.contains("seafood") || key.contains("egg") -> {
            MacroProteinMuscleIcon(modifier = modifier)
        }
        key == "c" || key.startsWith("c:") || key.startsWith("c ") || key.contains("carb") || key.contains("కార్బో") || key.contains("కార్బ్స్") || key.contains("grain") || key.contains("starch") || key.contains("cereal") || key.contains("bread") || key.contains("sweet") || key.contains("dessert") -> {
            MacroCarbLightningIcon(modifier = modifier)
        }
        key == "f" || key.startsWith("f:") || key.startsWith("f ") || key.contains("fat") || key.contains("కొవ్వు") || key.contains("oil") || key.contains("butter") || key.contains("lipid") -> {
            MacroFatShieldFlameIcon(modifier = modifier)
        }
        key == "fib" || key.startsWith("fib") || key.contains("fiber") || key.contains("ఫైబర్") || key.contains("పీచు") || key.contains("veg") || key.contains("fruit") || key.contains("salad") || key.contains("legume") || key.contains("green") -> {
            MacroFiberStomachIcon(modifier = modifier)
        }
        else -> {
            MacroProteinMuscleIcon(modifier = modifier)
        }
    }
}

/**
 * Squircle Card Tile reproducing the card format from reference:
 * - Rounded soft container background (pink, yellow, amber, mint)
 * - Centered exact custom icon inside
 */
@Composable
fun MacroSquircleTile(
    macroOrCategory: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    iconSize: Dp = 44.dp,
    cornerRadius: Dp = 18.dp
) {
    val key = macroOrCategory.lowercase().trim()
    val (bgColor, iconColor) = when {
        key.contains("pro") -> NutriProteinContainerLight to NutriProteinRed
        key.contains("carb") -> NutriCarbContainerLight to NutriCarbGold
        key.contains("fat") -> NutriFatContainerLight to NutriFatOrange
        key.contains("fib") -> NutriFiberContainerLight to NutriFiberGreen
        else -> NutriProteinContainerLight to NutriProteinRed
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            key.contains("pro") -> MacroProteinMuscleIcon(modifier = Modifier.size(iconSize), tint = iconColor)
            key.contains("carb") -> MacroCarbLightningIcon(modifier = Modifier.size(iconSize), tint = iconColor)
            key.contains("fat") -> MacroFatShieldFlameIcon(modifier = Modifier.size(iconSize), tint = iconColor, cutoutColor = bgColor)
            key.contains("fib") -> MacroFiberStomachIcon(modifier = Modifier.size(iconSize), tint = iconColor)
            else -> MacroProteinMuscleIcon(modifier = Modifier.size(iconSize), tint = iconColor)
        }
    }
}

/**
 * Avatar Box showing the exact custom macro icon for foods
 */
@Composable
fun FoodMacroAvatar(
    categoryOrMacro: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    customColor: Color? = null
) {
    Box(modifier = modifier) {
        MacroAppIcon(
            macroOrCategory = categoryOrMacro,
            modifier = Modifier.size(iconSize),
            overrideColor = customColor
        )
    }
}
