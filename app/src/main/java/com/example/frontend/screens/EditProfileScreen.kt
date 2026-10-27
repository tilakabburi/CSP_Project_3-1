package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.UserEntity
import com.example.backend.service.NutritionCalculationService
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.dismissKeyboardOnTap
import com.example.frontend.util.trackKeyboardFocusBounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: UserEntity?,
    onSaveProfile: (name: String, age: Int, height: Double, weight: Double, activity: String, build: String) -> Unit = { _, _, _, _, _, _ -> },
    onSaveProfileWithSex: ((name: String, age: Int, height: Double, weight: Double, activity: String, build: String, biologicalSex: String) -> Unit)? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(user) { mutableStateOf(user?.name?.ifBlank { "Tilak Kumar" } ?: "Tilak Kumar") }
    var biologicalSex by remember(user) { mutableStateOf(user?.biologicalSex?.ifBlank { "Male" } ?: "Male") }
    var age by remember(user) { mutableStateOf((user?.age ?: 23).toString()) }
    var height by remember(user) { mutableStateOf((user?.heightCm?.toInt() ?: 172).toString()) }
    var weight by remember(user) { mutableStateOf((user?.weightKg?.toInt() ?: 68).toString()) }
    var goalBuild by remember(user) { mutableStateOf(user?.bodyBuild ?: "Average") }
    var activityLevel by remember(user) { mutableStateOf(user?.activityLevel ?: "Moderate") }

    val goalOptions = listOf("Average", "Lean", "Athletic", "Stocky", "Lean Muscle", "Fat Loss", "Bulking")
    val activityOptions = listOf("Low", "Lightly Active", "Moderate", "High")

    val previewAge = age.toIntOrNull()?.coerceIn(15, 100) ?: (user?.age ?: 23)
    val previewHeight = height.toDoubleOrNull()?.coerceIn(100.0, 240.0) ?: (user?.heightCm ?: 172.0)
    val previewWeight = weight.toDoubleOrNull()?.coerceIn(30.0, 250.0) ?: (user?.weightKg ?: 68.0)

    val liveMetrics = remember(previewAge, previewHeight, previewWeight, activityLevel, goalBuild, biologicalSex) {
        NutritionCalculationService.calculateBodyMetrics(
            age = previewAge,
            heightCm = previewHeight,
            weightKg = previewWeight,
            activityLevel = activityLevel,
            bodyBuild = goalBuild,
            biologicalSex = biologicalSex
        )
    }

    val liveTargets = remember(previewAge, previewHeight, previewWeight, activityLevel, goalBuild, biologicalSex) {
        NutritionCalculationService.calculateTargets(
            userId = user?.id ?: 1L,
            age = previewAge,
            heightCm = previewHeight,
            weightKg = previewWeight,
            activityLevel = activityLevel,
            bodyBuild = goalBuild,
            biologicalSex = biologicalSex
        )
    }

    val lang = LocalAppLanguage.current

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = NutriStrings.editProfileTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_profile_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = NutriStrings.back(lang))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (onSaveProfileWithSex != null) {
                                onSaveProfileWithSex(name.trim(), previewAge, previewHeight, previewWeight, activityLevel, goalBuild, biologicalSex)
                            } else {
                                onSaveProfile(name.trim(), previewAge, previewHeight, previewWeight, activityLevel, goalBuild)
                            }
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("save_profile_action_btn")
                    ) {
                        Text(NutriStrings.save(lang), fontWeight = FontWeight.Bold, color = NutriTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        LaunchedEffect(listState.isScrollInProgress) {
            if (listState.isScrollInProgress) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .dismissKeyboardOnTap(focusManager, keyboardController),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // Avatar Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NutriTealLight, NutriTealDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = name.firstOrNull()?.uppercase() ?: "T"
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Name Field
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(NutriStrings.fullNameField(lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackKeyboardFocusBounds()
                        .testTag("edit_profile_name_input")
                )
            }

            // Biological Sex Selector
            item {
                Text(
                    text = NutriStrings.biologicalSex(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Male", "Female").forEach { sexOpt ->
                        val isSelected = biologicalSex.equals(sexOpt, ignoreCase = true)
                        val displaySex = if (sexOpt == "Male") NutriStrings.male(lang) else NutriStrings.female(lang)
                        FilterChip(
                            selected = isSelected,
                            onClick = { biologicalSex = sexOpt },
                            label = {
                                Text(
                                    text = displaySex,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("edit_profile_sex_${sexOpt.lowercase()}")
                        )
                    }
                }
            }

            // Age & Height Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text(NutriStrings.s(lang, "Age (yrs)", "వయస్సు (సంవత్సరాలు)")) },
                        placeholder = { Text("23") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .trackKeyboardFocusBounds()
                            .testTag("edit_profile_age_input")
                    )

                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text(NutriStrings.heightLabel(lang)) },
                        placeholder = { Text("172") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .trackKeyboardFocusBounds()
                            .testTag("edit_profile_height_input")
                    )
                }
            }

            // Weight Field
            item {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(NutriStrings.weightLabel(lang)) },
                    placeholder = { Text("68") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackKeyboardFocusBounds()
                        .testTag("edit_profile_weight_input")
                )
            }

            // Primary Goal & Body Build Selection
            item {
                Text(
                    text = NutriStrings.s(lang, "Body Build & Primary Goal", "శరీర రకం & లక్ష్యం"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(goalOptions) { opt ->
                        val isSelected = goalBuild.equals(opt, ignoreCase = true)
                        val displayOpt = when (opt.lowercase()) {
                            "average" -> NutriStrings.s(lang, "Average", "సాధారణ")
                            "lean" -> NutriStrings.s(lang, "Lean", "సన్నని")
                            "athletic" -> NutriStrings.s(lang, "Athletic", "దృఢమైన")
                            "stocky" -> NutriStrings.s(lang, "Stocky", "బలమైన")
                            "lean muscle" -> NutriStrings.s(lang, "Lean Muscle", "కండర నిర్మాణం")
                            "fat loss" -> NutriStrings.s(lang, "Fat Loss", "కొవ్వు తగ్గింపు")
                            "bulking" -> NutriStrings.s(lang, "Bulking", "బరువు పెంపు")
                            else -> opt
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { goalBuild = opt },
                            label = { Text(displayOpt, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Activity Level
            item {
                Text(
                    text = NutriStrings.activityLevel(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activityOptions) { opt ->
                        val isSelected = activityLevel.equals(opt, ignoreCase = true)
                        val displayAct = when (opt.lowercase()) {
                            "low" -> NutriStrings.s(lang, "Low", "తక్కువ")
                            "lightly active" -> NutriStrings.s(lang, "Lightly Active", "తేలికపాటి")
                            "moderate" -> NutriStrings.s(lang, "Moderate", "మధ్యస్థం")
                            "high" -> NutriStrings.s(lang, "High", "ఎక్కువ")
                            else -> opt
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { activityLevel = opt },
                            label = { Text(displayAct, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Live Calculated BMI, BMR, TDEE & Macro Preview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_live_preview_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NutriTealContainer.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, NutriTeal.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = NutriStrings.s(lang, "Live Body Requirement Preview", "పోషకాహార అవసరాల ప్రివ్యూ"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NutriTeal.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "BMI ${liveMetrics.bmi} (${liveMetrics.bmiCategory})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriTeal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BMR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text("${liveMetrics.bmrCalories} kcal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TDEE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text("${liveMetrics.tdeeCalories} kcal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(NutriStrings.dailyTarget(lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text("${liveTargets.caloriesTarget.toInt()} kcal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = NutriTeal, maxLines = 1)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("P: ${liveTargets.proteinTarget.toInt()}g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NutriProteinRed, maxLines = 1)
                            Text("C: ${liveTargets.carbsTarget.toInt()}g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NutriCarbGold, maxLines = 1)
                            Text("F: ${liveTargets.fatTarget.toInt()}g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NutriFatOrange, maxLines = 1)
                            Text("Fib: ${liveTargets.fiberTarget.toInt()}g", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NutriFiberGreen, maxLines = 1)
                            Text("H₂O: ${String.format(java.util.Locale.US, "%.1fL", liveTargets.waterTargetMl / 1000.0)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NutriWaterCyan, maxLines = 1)
                        }
                    }
                }
            }

            // Save Changes CTA
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (onSaveProfileWithSex != null) {
                            onSaveProfileWithSex(name.trim(), previewAge, previewHeight, previewWeight, activityLevel, goalBuild, biologicalSex)
                        } else {
                            onSaveProfile(name.trim(), previewAge, previewHeight, previewWeight, activityLevel, goalBuild)
                        }
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NutriTeal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("edit_profile_save_btn")
                ) {
                    Text(NutriStrings.s(lang, "Save Changes", "మార్పులను సేవ్ చేయండి"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
