package com.example.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.backend.data.local.entity.FoodEntity
import com.example.backend.data.local.entity.FoodPoolEntity
import com.example.frontend.components.AddCustomFoodDialog
import com.example.frontend.components.FoodPoolSelectorContent
import com.example.frontend.theme.NutriTeal
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPoolScreen(
    allFoods: List<FoodEntity>,
    foodPool: List<FoodPoolEntity>,
    onToggleFoodPool: (foodId: Long, inPool: Boolean) -> Unit,
    onCreateCustomFood: (String, String, Double, String, Double, Double, Double, Double, Double, (Long) -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
    onClearAllPool: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    var localPoolIds by remember(foodPool) {
        mutableStateOf(foodPool.map { it.foodId }.toSet())
    }
    var showAddFoodDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (showAddFoodDialog) {
            showAddFoodDialog = false
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = NutriStrings.foodPoolTitle(lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.s(
                                lang,
                                "${localPoolIds.size} of ${allFoods.size} foods selected",
                                "${allFoods.size} ఆహారాలలో ${localPoolIds.size} ఎంపికయ్యాయి"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = NutriStrings.back(lang)
                        )
                    }
                },
                actions = {
                    if (localPoolIds.isNotEmpty() && onClearAllPool != null) {
                        TextButton(
                            onClick = {
                                localPoolIds = emptySet()
                                onClearAllPool()
                            },
                            modifier = Modifier.testTag("btn_clear_all_food_pool")
                        ) {
                            Text(
                                text = NutriStrings.clearAllPool(lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFoodDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(NutriStrings.addCustomFood(lang)) },
                containerColor = NutriTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_custom_food")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        FoodPoolSelectorContent(
            allFoods = allFoods,
            selectedFoodIds = localPoolIds,
            onToggleFood = { foodId ->
                val wasInPool = foodId in localPoolIds
                localPoolIds = if (wasInPool) localPoolIds - foodId else localPoolIds + foodId
                onToggleFoodPool(foodId, wasInPool)
            },
            showTitle = false,
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        )
    }

    if (showAddFoodDialog) {
        AddCustomFoodDialog(
            onDismiss = { showAddFoodDialog = false },
            onSave = { name, category, refQty, refUnit, cals, p, c, f, fib ->
                onCreateCustomFood(name, category, refQty, refUnit, cals, p, c, f, fib) { _ ->
                    showAddFoodDialog = false
                }
            }
        )
    }
}
