package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.NutritionTargetEntity
import com.example.backend.data.local.entity.UserEntity
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@Composable
fun ProfileScreen(
    user: UserEntity?,
    targets: NutritionTargetEntity?,
    is24HourFormat: Boolean = false,
    onOpenEditProfile: () -> Unit,
    onOpenFoodPool: () -> Unit,
    onOpenMealRoutine: () -> Unit,
    onOpenMealTimings: () -> Unit = {},
    onOpenReminderSettings: () -> Unit = {},
    onOpenWaterSettings: () -> Unit,
    onOpenExercise: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVideoTour: () -> Unit = {},
    onLogOut: () -> Unit,
    onResetApp: () -> Unit = onLogOut,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showLogoutDialog || showResetDialog) {
        showLogoutDialog = false
        showResetDialog = false
    }

    val displayName = user?.name?.ifBlank { "Tilak Kumar" } ?: "Tilak Kumar"
    val initialLetter = displayName.firstOrNull()?.uppercase() ?: "T"
    val ageDisplay = (user?.age ?: 23).toString()
    val heightDisplay = "${user?.heightCm?.toInt() ?: 172} cm"
    val weightDisplay = "${user?.weightKg?.toInt() ?: 68} kg"
    val goalDisplay = user?.bodyBuild?.ifBlank { "Lean Muscle" } ?: "Lean Muscle"

    // App Theme-consistent colors
    val cardBg = MaterialTheme.colorScheme.surface
    val borderCol = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val logoutRed = Color(0xFFEF4444)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar Circle with App Theme Teal Gradient
        item {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NutriTealLight, NutriTealDark)
                        )
                    )
                    .testTag("profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialLetter,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Name & App Brand Subtitle
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.testTag("profile_display_name")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = NutriStrings.s(lang, "NutriPath Member", "నూట్రిపాత్ సభ్యులు"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NutriTeal
                )
            }
        }

        // 4-Column Metric Stat Highlights with modern styling
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStatColumn(
                        value = ageDisplay,
                        label = NutriStrings.s(lang, "Age", "వయస్సు"),
                        valueColor = textPrimary,
                        labelColor = textMuted
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp),
                        color = borderCol.copy(alpha = 0.5f)
                    )

                    ProfileStatColumn(
                        value = heightDisplay,
                        label = NutriStrings.s(lang, "Height", "ఎత్తు"),
                        valueColor = textPrimary,
                        labelColor = textMuted
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp),
                        color = borderCol.copy(alpha = 0.5f)
                    )

                    ProfileStatColumn(
                        value = weightDisplay,
                        label = NutriStrings.s(lang, "Weight", "బరువు"),
                        valueColor = textPrimary,
                        labelColor = textMuted
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp),
                        color = borderCol.copy(alpha = 0.5f)
                    )

                    ProfileStatColumn(
                        value = goalDisplay,
                        label = NutriStrings.s(lang, "Goal", "లక్ష్యం"),
                        valueColor = NutriTeal,
                        labelColor = textMuted
                    )
                }
            }
        }

        // Body Composition & Metabolic Summary Card (BMI, BMR, TDEE & Macro Targets)
        item {
            val metrics = remember(user) {
                com.example.backend.service.NutritionCalculationService.calculateBodyMetrics(
                    age = user?.age ?: 23,
                    heightCm = user?.heightCm ?: 172.0,
                    weightKg = user?.weightKg ?: 68.0,
                    activityLevel = user?.activityLevel ?: "Moderate",
                    bodyBuild = user?.bodyBuild ?: "Average",
                    biologicalSex = user?.biologicalSex ?: "Male"
                )
            }
            val effectiveTarget = remember(user, targets) {
                targets ?: com.example.backend.service.NutritionCalculationService.calculateTargets(
                    userId = user?.id ?: 1L,
                    age = user?.age ?: 23,
                    heightCm = user?.heightCm ?: 172.0,
                    weightKg = user?.weightKg ?: 68.0,
                    activityLevel = user?.activityLevel ?: "Moderate",
                    bodyBuild = user?.bodyBuild ?: "Average",
                    biologicalSex = user?.biologicalSex ?: "Male"
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_metabolic_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = NutriStrings.s(lang, "Body Metrics & Daily Targets", "శరీర కొలతలు & రోజువారీ లక్ష్యాలు"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Mifflin-St Jeor • ${user?.biologicalSex ?: "Male"} • ${user?.activityLevel ?: "Moderate"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NutriTeal.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "BMI ${metrics.bmi} • ${metrics.bmiCategory}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NutriTeal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = borderCol.copy(alpha = 0.45f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfileStatColumn(
                            value = "${metrics.bmrCalories} kcal",
                            label = "BMR",
                            valueColor = textPrimary,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${metrics.tdeeCalories} kcal",
                            label = "TDEE",
                            valueColor = NutriTeal,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${effectiveTarget.caloriesTarget.toInt()} kcal",
                            label = NutriStrings.s(lang, "Target", "లక్ష్యం"),
                            valueColor = NutriAmber,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${metrics.healthyWeightMinKg.toInt()}–${metrics.healthyWeightMaxKg.toInt()} kg",
                            label = NutriStrings.healthyWeightShortLabel(lang),
                            valueColor = textPrimary,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = borderCol.copy(alpha = 0.45f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProfileStatColumn(
                            value = "${effectiveTarget.proteinTarget.toInt()}g",
                            label = NutriStrings.protein(lang),
                            valueColor = NutriProteinRed,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${effectiveTarget.carbsTarget.toInt()}g",
                            label = NutriStrings.carbs(lang),
                            valueColor = NutriCarbGold,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${effectiveTarget.fatTarget.toInt()}g",
                            label = NutriStrings.fats(lang),
                            valueColor = NutriFatOrange,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = "${effectiveTarget.fiberTarget.toInt()}g",
                            label = NutriStrings.fiber(lang),
                            valueColor = NutriFiberGreen,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatColumn(
                            value = String.format(java.util.Locale.US, "%.1fL", effectiveTarget.waterTargetMl / 1000.0),
                            label = NutriStrings.water(lang),
                            valueColor = NutriWaterCyan,
                            labelColor = textMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = NutriStrings.s(lang, "Account & Configuration", "ఖాతా & అమరికలు"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textMuted
                )
            }
        }

        // Menu Item 1: Edit Profile
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.Edit,
                iconTint = NutriTeal,
                iconBg = NutriTeal.copy(alpha = 0.12f),
                title = NutriStrings.editProfile(lang),
                subtitle = NutriStrings.s(lang, "Name, biometric measurements & goal", "పేరు, శరీర కొలతలు & లక్ష్యం"),
                testTag = "menu_edit_profile",
                onClick = onOpenEditProfile,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 2: Food Pool
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.RestaurantMenu,
                iconTint = NutriAmber,
                iconBg = NutriAmber.copy(alpha = 0.12f),
                title = NutriStrings.foodPoolTitle(lang),
                subtitle = NutriStrings.s(lang, "Manage ingredients & custom pantry items", "ఆహార పదార్థాలు & జాబితా నిర్వహణ"),
                testTag = "menu_food_pool",
                onClick = onOpenFoodPool,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 3: Meal Food Routine
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.RestaurantMenu,
                iconTint = NutriTeal,
                iconBg = NutriTeal.copy(alpha = 0.12f),
                title = NutriStrings.s(lang, "Meal Food Routine", "భోజన ఆహార దినచర్య"),
                subtitle = NutriStrings.s(lang, "Assign pantry items & custom quantities to each meal", "ప్రతి పూటకు ఆహారాలు & పరిమాణాలను సెట్ చేయండి"),
                testTag = "menu_meal_routine_food",
                onClick = onOpenMealRoutine,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 4: Meal Timings & Schedule
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.Schedule,
                iconTint = Color(0xFF8B5CF6),
                iconBg = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                title = NutriStrings.mealTimingsSchedule(lang),
                subtitle = NutriStrings.s(lang, "Configure meal hours, weekend schedule & alert offsets", "భోజన సమయాలు & అలర్ట్ సమయాలను సెట్ చేయండి"),
                testTag = "menu_meal_timings",
                onClick = onOpenMealTimings,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 4: Water Settings
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.WaterDrop,
                iconTint = NutriWaterCyan,
                iconBg = NutriWaterCyan.copy(alpha = 0.12f),
                title = NutriStrings.waterSettings(lang),
                subtitle = NutriStrings.s(lang, "Target hydration volume & reminder frequency", "నీటి లక్ష్యం & రిమైండర్ వ్యవధి"),
                testTag = "menu_water_settings",
                onClick = onOpenWaterSettings,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item: Daily Movement / Exercise (under Water Settings)
        item {
            ProfileMenuPill(
                icon = Icons.AutoMirrored.Outlined.DirectionsRun,
                iconTint = NutriTeal,
                iconBg = NutriTeal.copy(alpha = 0.12f),
                title = NutriStrings.exerciseMovement(lang),
                subtitle = NutriStrings.s(lang, "Track activities and calorie burn", "శారీరక శ్రమ మరియు క్యాలరీ ఖర్చును నమోదు చేయండి"),
                badgeText = NutriStrings.s(lang, "Next Version", "తదుపరి వెర్షన్"),
                testTag = "menu_exercise",
                onClick = onOpenExercise,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 5: Settings
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.Settings,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                iconBg = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
                title = NutriStrings.settingsTitle(lang),
                subtitle = if (is24HourFormat)
                    NutriStrings.s(lang, "Time format (24h), units, haptics & simulator", "సమయ ఫార్మాట్ (24h), కొలత ప్రమాణాలు, వైబ్రేషన్ & సిమ్యులేటర్")
                else
                    NutriStrings.s(lang, "Time format (12h AM/PM), units, haptics & simulator", "సమయ ఫార్మాట్ (12h AM/PM), కొలత ప్రమాణాలు, వైబ్రేషన్ & సిమ్యులేటర్"),
                testTag = "menu_settings",
                onClick = onOpenSettings,
                cardBg = cardBg,
                borderColor = borderCol,
                titleColor = textPrimary
            )
        }

        // Menu Item 6: Reset App / Start from Beginning
        item {
            ProfileMenuPill(
                icon = Icons.Outlined.Refresh,
                iconTint = logoutRed,
                iconBg = logoutRed.copy(alpha = 0.10f),
                title = NutriStrings.s(lang, "Reset App to Beginning", "యాప్‌ను మొదటి నుండి ప్రారంభించండి"),
                subtitle = NutriStrings.s(lang, "Start fresh with the onboarding flow", "ప్రొఫైల్ సెటప్‌తో కొత్తగా ప్రారంభించండి"),
                titleColor = logoutRed,
                testTag = "menu_reset_app",
                onClick = { showResetDialog = true },
                cardBg = cardBg,
                borderColor = borderCol.copy(alpha = 0.5f)
            )
        }

        // Menu Item 7: Log Out
        item {
            ProfileMenuPill(
                icon = Icons.AutoMirrored.Outlined.Logout,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                iconBg = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
                title = NutriStrings.logout(lang),
                subtitle = NutriStrings.s(lang, "Sign out of your profile session", "మీ ప్రొఫైల్ ఖాతా నుండి నిష్క్రమించండి"),
                titleColor = MaterialTheme.colorScheme.onSurface,
                testTag = "menu_log_out",
                onClick = { showLogoutDialog = true },
                cardBg = cardBg,
                borderColor = borderCol.copy(alpha = 0.5f)
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = logoutRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(NutriStrings.s(lang, "Reset to Beginning?", "మొదటి నుండి ప్రారంభించాలా?"), fontWeight = FontWeight.Bold) },
            text = { Text(NutriStrings.s(lang, "This will clear your profile and restart the setup from the beginning.", "ఇది మీ ప్రొఫైల్‌ను క్లియర్ చేసి సెటప్‌ను మొదటి నుండి పునఃప్రారంభిస్తుంది.")) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetApp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = logoutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(NutriStrings.s(lang, "Reset Now", "ఇప్పుడే రీసెట్ చేయండి"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(NutriStrings.cancel(lang))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = logoutRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(NutriStrings.logout(lang), fontWeight = FontWeight.Bold) },
            text = { Text(NutriStrings.s(lang, "Are you sure you want to log out?", "మీరు ఖచ్చితంగా లాగౌట్ కావాలనుకుంటున్నారా?")) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = logoutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(NutriStrings.logout(lang), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(NutriStrings.cancel(lang))
                }
            }
        )
    }
}

@Composable
private fun ProfileStatColumn(
    value: String,
    label: String,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileMenuPill(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    cardBg: Color,
    borderColor: Color,
    titleColor: Color,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Icon Badge matching App Design language
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NutriTeal.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NutriTeal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
