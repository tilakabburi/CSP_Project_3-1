package com.example.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.data.local.entity.FoodEntity
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import com.example.frontend.util.dismissKeyboardOnTap

/**
 * Unified Food Pool Component shared across Onboarding Step 4 and Profile Food Pool Manager.
 * Features:
 * - Instant per-letter filtering with top-right expandable search
 * - Macro category filter chips
 * - Selected foods prioritized at the top of the list
 * - Smooth standard item animation: remaining items step down into place
 * - Reduced size, space-efficient, subtle macro mentions (P, C, F, Fib)
 * - Immediate, reliable click & checkbox selection response
 */
@Composable
fun FoodPoolSelectorContent(
    allFoods: List<FoodEntity>,
    selectedFoodIds: Set<Long>,
    onToggleFood: (foodId: Long) -> Unit,
    modifier: Modifier = Modifier,
    headerTitle: String = "Food Pool",
    headerSubtitle: String? = "Select the foods you eat regularly. You can plan with them and swap them anytime.",
    showTitle: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(bottom = 80.dp)
) {
    val lang = LocalAppLanguage.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Protein") }

    val categories = listOf("Protein", "Carbohydrates", "Fiber", "Fats")

    val displayFoods = remember(allFoods, searchQuery, selectedCategory, selectedFoodIds) {
        val trimmed = searchQuery.trim()
        val matches = allFoods.filter { food ->
            val matchesSearch = trimmed.isBlank() ||
                food.name.contains(trimmed, ignoreCase = true) ||
                food.iconKey.contains(trimmed, ignoreCase = true)
            val matchesCategory = food.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
        val selected = matches.filter { it.id in selectedFoodIds }.sortedBy { it.name }
        val unselected = matches.filter { it.id !in selectedFoodIds }.sortedBy { it.name }
        selected + unselected
    }

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
        modifier = modifier
            .fillMaxSize()
            .dismissKeyboardOnTap(focusManager, keyboardController),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding
    ) {
        // Top Section: Header & Top-Right Expandable Search Bar
        item(key = "food_pool_expandable_header") {
            ExpandableSearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholderText = NutriStrings.s(lang, "Item name...", "ఆహారం పేరు..."),
                titleContent = {
                    if (showTitle) {
                        Column {
                            Text(
                                text = headerTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = NutriStrings.s(
                                    lang,
                                    "${selectedFoodIds.size} of ${allFoods.size} foods selected",
                                    "${allFoods.size} ఆహారాలలో ${selectedFoodIds.size} ఎంపికయ్యాయి"
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (headerSubtitle != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = headerSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = NutriStrings.s(
                                lang,
                                "${selectedFoodIds.size} of ${allFoods.size} foods selected",
                                "${allFoods.size} ఆహారాలలో ${selectedFoodIds.size} ఎంపికయ్యాయి"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NutriTeal
                        )
                    }
                }
            )
        }

        // Category Filter Chips Row
        item(key = "food_pool_categories_row") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { "cat_chip_$it" }) { cat ->
                    val catLabel = when (cat.lowercase()) {
                        "protein" -> NutriStrings.protein(lang)
                        "carbohydrates" -> NutriStrings.carbs(lang)
                        "fiber" -> NutriStrings.fiber(lang)
                        "fats" -> NutriStrings.fats(lang)
                        else -> cat
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(catLabel) },
                        leadingIcon = {
                            MacroAppIcon(
                                macroOrCategory = cat,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NutriTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (displayFoods.isEmpty()) {
            item(key = "empty_matching_foods") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔍", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = NutriStrings.s(lang, "No matching items found", "ఎలాంటి ఆహారాలు కనుగొనబడలేదు"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = NutriStrings.s(
                            lang,
                            "Try typing another item name or checking a different category.",
                            "మరొక ఆహారం పేరు టైప్ చేయండి లేదా వేరే విభాగాన్ని ఎంచుకోండి."
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Unified items list: persistent key ensures items above come down a step smoothly
            items(displayFoods, key = { it.id }) { food ->
                val isSelected = food.id in selectedFoodIds
                FoodInventoryCard(
                    food = food,
                    inPool = isSelected,
                    onToggle = { onToggleFood(food.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun FoodInventoryCard(
    food: FoodEntity,
    inPool: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .testTag("food_card_${food.id}")
            .border(
                width = if (inPool) 1.5.dp else 1.dp,
                color = if (inPool) NutriTeal else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inPool) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (inPool) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (inPool) NutriTeal.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                MacroAppIcon(
                    macroOrCategory = food.category,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (food.iconKey.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = food.iconKey,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (food.isCustom) {
                        val cardLang = LocalAppLanguage.current
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = NutriAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = NutriStrings.s(cardLang, "CUSTOM", "కస్టమ్"),
                                style = MaterialTheme.typography.labelSmall,
                                color = NutriAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Reduced size, space-efficient, subtle macro mentions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${food.calories.toInt()} kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (inPool) NutriTeal else NutriAmber
                    )
                    Text(text = "•", fontSize = 9.sp, color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "${food.referenceQuantity.toInt()}${food.referenceUnit.ifBlank { "g" }}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "•", fontSize = 9.sp, color = MaterialTheme.colorScheme.outlineVariant)
                    Text(text = "P: ${food.protein.toInt()}g", fontSize = 9.5.sp, color = NutriProteinRed, fontWeight = FontWeight.SemiBold)
                    Text(text = "C: ${food.carbs.toInt()}g", fontSize = 9.5.sp, color = NutriCarbGold, fontWeight = FontWeight.SemiBold)
                    Text(text = "F: ${food.fat.toInt()}g", fontSize = 9.5.sp, color = NutriFatOrange, fontWeight = FontWeight.SemiBold)
                    Text(text = "Fib: ${food.fiber.toInt()}g", fontSize = 9.5.sp, color = NutriFiberGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkbox: Passive visual indicator so Card.onClick handles 100% of touches reliably
            Checkbox(
                checked = inPool,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = NutriTeal,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.testTag("food_pool_checkbox_${food.id}")
            )
        }
    }
}
