package com.example.frontend.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.frontend.InsightSummary
import com.example.frontend.MainViewModel
import com.example.frontend.WeeklyDayStat
import com.example.frontend.components.MacroAppIcon
import com.example.frontend.theme.*
import com.example.frontend.util.AppLanguage
import com.example.frontend.util.LocalAppLanguage
import com.example.frontend.util.NutriStrings
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf("Weekly") }
    var insightSummary by remember { mutableStateOf<InsightSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }

    val refreshTrigger by viewModel.dataRefreshTrigger.collectAsStateWithLifecycle()
    val currentUser by viewModel.user.collectAsStateWithLifecycle()
    val currentTargets by viewModel.targets.collectAsStateWithLifecycle()
    val mealLogs by viewModel.mealLogsForDate.collectAsStateWithLifecycle()
    val waterLogs by viewModel.waterLogsForDate.collectAsStateWithLifecycle()

    LaunchedEffect(viewMode, refreshTrigger, currentUser, currentTargets, mealLogs, waterLogs) {
        isLoading = true
        val days = if (viewMode == "Monthly") 30 else 7
        insightSummary = viewModel.computeWeeklyInsights(days)
        isLoading = false
    }

    val lang = LocalAppLanguage.current

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = NutriStrings.s(lang, "Reset Insights & Trends?", "విశ్లేషణ డేటాను రీసెట్ చేయాలా?"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = NutriStrings.s(
                        lang,
                        "This will clear your logged meals and water history, resetting your charts to zero.",
                        "ఇది మీ భోజన మరియు నీటి లాగ్‌లను క్లియర్ చేసి, చార్ట్‌లను సున్నాకి రీసెట్ చేస్తుంది."
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetInsights()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_reset_insights_btn")
                ) {
                    Text(
                        NutriStrings.s(lang, "Reset to Zero", "సున్నాకి రీసెట్ చేయండి"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(NutriStrings.cancel(lang))
                }
            }
        )
    }

    InsightContent(
        summary = insightSummary,
        viewMode = viewMode,
        onViewModeChange = { viewMode = it },
        onResetClick = { showResetDialog = true },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightContent(
    summary: InsightSummary?,
    viewMode: String = "Weekly",
    onViewModeChange: (String) -> Unit = {},
    onResetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = NutriStrings.s(lang, "Insights & Trends", "పోషకాహార విశ్లేషణ"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NutriStrings.s(lang, "Your daily habits, calorie trends, and nutrition progress", "మీ అలవాట్లు, క్యాలరీలు మరియు పోషకాల పురోగతి"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier.testTag("reset_insights_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = NutriStrings.s(lang, "Reset Insights Data", "డేటా రీసెట్"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // View Mode Toggle (Weekly / Monthly)
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = viewMode == "Weekly",
                        onClick = { onViewModeChange("Weekly") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("toggle_weekly_view")
                    ) {
                        Text(NutriStrings.s(lang, "Weekly View (7 Days)", "వారం వీక్షణ (7 రోజులు)"), fontWeight = FontWeight.SemiBold)
                    }
                    SegmentedButton(
                        selected = viewMode == "Monthly",
                        onClick = { onViewModeChange("Monthly") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("toggle_monthly_view")
                    ) {
                        Text(NutriStrings.s(lang, "Monthly View (30 Days)", "నెల వీక్షణ (30 రోజులు)"), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (summary != null) {
                // Reset State Notification Banner when values are 0
                if (summary.totalCalories == 0.0 && summary.totalWaterMl == 0) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("insights_reset_notice_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📊", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = NutriStrings.s(lang, "Insights Reset to Initial State", "విశ్లేషణ ప్రారంభ స్థితికి రీసెట్ అయింది"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = NutriStrings.s(
                                            lang,
                                            "All consumption and hydration metrics are reset. As you log meals in Home or Daily Log, your habit rings, curves, and macro energy donuts will calibrate in real time.",
                                            "భోజనం మరియు నీటి రికార్డులు రీసెట్ అయ్యాయి. మీరు హోమ్ లేదా డైలీ లాగ్‌లో భోజనాలు నమోదు చేసినప్పుడు, ఇక్కడ చార్ట్‌లు ఆటోమేటిక్‌గా అప్‌డేట్ అవుతాయి."
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Visualizer 1: Multi-Ring Radial Progress Gauge (Smart Rings)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("multi_ring_progress_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (viewMode == "Monthly") NutriStrings.s(lang, "Monthly Habit Rings", "నెలవారీ అలవాట్ల రింగులు") else NutriStrings.s(lang, "Weekly Habit Rings", "వారపు అలవాట్ల రింగులు"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (viewMode == "Monthly") NutriStrings.s(lang, "30-day cumulative adherence", "30 రోజుల లక్ష్యాల సాధన") else NutriStrings.s(lang, "7-day cumulative adherence", "7 రోజుల లక్ష్యాల సాధన"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NutriTeal.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = NutriStrings.s(lang, "${summary.completionRatePercent}% Score", "${summary.completionRatePercent}% స్కోర్"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriTeal,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Concentric Radial Canvas Rings
                                MultiRingProgressCanvas(
                                    calorieRatio = if (summary.periodTargetCalories > 0) (summary.totalCalories / summary.periodTargetCalories).coerceIn(0.0, 1.0).toFloat() else 0f,
                                    proteinRatio = if (summary.periodTargetProtein > 0) (summary.totalProtein / summary.periodTargetProtein).coerceIn(0.0, 1.0).toFloat() else 0f,
                                    waterRatio = if (summary.periodTargetWaterMl > 0) (summary.totalWaterMl.toFloat() / summary.periodTargetWaterMl.toFloat()).coerceIn(0f, 1f) else 0f,
                                    fiberRatio = if (summary.periodTargetFiber > 0) (summary.totalFiber / summary.periodTargetFiber).coerceIn(0.0, 1.0).toFloat() else 0f,
                                    centerPercentage = summary.completionRatePercent,
                                    modifier = Modifier.size(165.dp)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                // Meaningful & Rich Ring Legend Items
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RingLegendItem(
                                        name = NutriStrings.calories(lang),
                                        currentValue = "${summary.totalCalories.toInt()} kcal",
                                        targetValue = "${summary.periodTargetCalories.toInt()} kcal",
                                        percent = summary.caloriePercent,
                                        color = NutriAmber
                                    )
                                    RingLegendItem(
                                        name = NutriStrings.protein(lang),
                                        currentValue = "${summary.totalProtein.toInt()}g",
                                        targetValue = "${summary.periodTargetProtein.toInt()}g",
                                        percent = summary.proteinPercent,
                                        color = NutriProteinRed
                                    )
                                    RingLegendItem(
                                        name = NutriStrings.water(lang),
                                        currentValue = "${summary.totalWaterMl} ml",
                                        targetValue = "${summary.periodTargetWaterMl} ml",
                                        percent = summary.waterPercent,
                                        color = NutriWaterCyan
                                    )
                                    RingLegendItem(
                                        name = NutriStrings.fiber(lang),
                                        currentValue = "${summary.totalFiber.toInt()}g",
                                        targetValue = "${summary.periodTargetFiber.toInt()}g",
                                        percent = summary.fiberPercent,
                                        color = NutriFiberGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Visualizer 2: Spline Area Flow Curve
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spline_trend_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (viewMode == "Monthly") NutriStrings.s(lang, "Monthly Calorie Flow Curve", "నెలవారీ క్యాలరీల కర్వ్") else NutriStrings.s(lang, "Daily Calorie Flow Curve", "రోజువారీ క్యాలరీల కర్వ్"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (viewMode == "Monthly") {
                                            NutriStrings.s(lang, "30-Day Total: ${summary.totalCalories.toInt()} / ${summary.periodTargetCalories.toInt()} kcal (${summary.caloriePercent}%)", "30 రోజుల మొత్తం: ${summary.totalCalories.toInt()} / ${summary.periodTargetCalories.toInt()} kcal (${summary.caloriePercent}%)")
                                        } else {
                                            NutriStrings.s(lang, "Weekly Total: ${summary.totalCalories.toInt()} / ${summary.periodTargetCalories.toInt()} kcal (${summary.caloriePercent}%)", "వారపు మొత్తం: ${summary.totalCalories.toInt()} / ${summary.periodTargetCalories.toInt()} kcal (${summary.caloriePercent}%)")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NutriAmber.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = NutriStrings.s(lang, "Goal: ${summary.periodTargetCalories.toInt()} kcal", "లక్ష్యం: ${summary.periodTargetCalories.toInt()} kcal"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriAmber,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Spline Canvas Chart
                            SplineAreaTrendChart(
                                days = summary.weeklyDays,
                                targetCalories = summary.targetCalories,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }

                // Visualizer 3: Macronutrient Calorie Distribution Donut Chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("macro_donut_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (viewMode == "Monthly") NutriStrings.s(lang, "Monthly Macronutrient Breakdown", "నెలవారీ పోషకాల విభజన") else NutriStrings.s(lang, "Weekly Macronutrient Breakdown", "వారపు పోషకాల విభజన"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (viewMode == "Monthly") NutriStrings.s(lang, "Cumulative caloric breakdown vs 30-day targets", "30 రోజుల లక్ష్యాలతో పోషకాల పోలిక") else NutriStrings.s(lang, "Cumulative caloric breakdown vs 7-day targets", "7 రోజుల లక్ష్యాలతో పోషకాల పోలిక"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MacroDonutCanvas(
                                    proteinGrams = summary.totalProtein,
                                    carbsGrams = summary.totalCarbs,
                                    fatGrams = summary.totalFat,
                                    totalCalories = summary.totalCalories,
                                    periodLabel = if (viewMode == "Monthly") NutriStrings.s(lang, "kcal / month", "kcal / నెల") else NutriStrings.s(lang, "kcal / week", "kcal / వారం"),
                                    modifier = Modifier.size(160.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                val totalMacroCals = (summary.totalProtein * 4) + (summary.totalCarbs * 4) + (summary.totalFat * 9)
                                val pPct = if (totalMacroCals > 0) ((summary.totalProtein * 4 / totalMacroCals) * 100).toInt() else 0
                                val cPct = if (totalMacroCals > 0) ((summary.totalCarbs * 4 / totalMacroCals) * 100).toInt() else 0
                                val fPct = if (totalMacroCals > 0) ((summary.totalFat * 9 / totalMacroCals) * 100).toInt() else 0

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    DonutLegendRow(
                                        label = NutriStrings.protein(lang),
                                        consumed = "${summary.totalProtein.toInt()}g",
                                        target = "${summary.periodTargetProtein.toInt()}g",
                                        percentageStr = NutriStrings.s(
                                            lang,
                                            "$pPct% energy share • ${summary.proteinPercent}% of ${if (viewMode == "Monthly") "monthly" else "weekly"} goal",
                                            "$pPct% శక్తి భాగస్వామ్యం • ${summary.proteinPercent}% లక్ష్యం"
                                        ),
                                        color = NutriProteinRed
                                    )
                                    DonutLegendRow(
                                        label = NutriStrings.carbs(lang),
                                        consumed = "${summary.totalCarbs.toInt()}g",
                                        target = "${summary.periodTargetCarbs.toInt()}g",
                                        percentageStr = NutriStrings.s(
                                            lang,
                                            "$cPct% energy share • ${summary.carbsPercent}% of ${if (viewMode == "Monthly") "monthly" else "weekly"} goal",
                                            "$cPct% శక్తి భాగస్వామ్యం • ${summary.carbsPercent}% లక్ష్యం"
                                        ),
                                        color = NutriCarbGold
                                    )
                                    DonutLegendRow(
                                        label = NutriStrings.fats(lang),
                                        consumed = "${summary.totalFat.toInt()}g",
                                        target = "${summary.periodTargetFat.toInt()}g",
                                        percentageStr = NutriStrings.s(
                                            lang,
                                            "$fPct% energy share • ${summary.fatPercent}% of ${if (viewMode == "Monthly") "monthly" else "weekly"} goal",
                                            "$fPct% శక్తి భాగస్వామ్యం • ${summary.fatPercent}% లక్ష్యం"
                                        ),
                                        color = NutriFatOrange
                                    )
                                    DonutLegendRow(
                                        label = NutriStrings.fiber(lang),
                                        consumed = "${summary.totalFiber.toInt()}g",
                                        target = "${summary.periodTargetFiber.toInt()}g",
                                        percentageStr = NutriStrings.s(
                                            lang,
                                            "${summary.fiberPercent}% of ${if (viewMode == "Monthly") "monthly" else "weekly"} goal",
                                            "${summary.fiberPercent}% లక్ష్యం"
                                        ),
                                        color = NutriFiberGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Visualizer 4: Circular Constellation Nodes (Day Orbit)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_orbit_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (viewMode == "Monthly") NutriStrings.s(lang, "30-Day Completion Overview", "30 రోజుల సాధన వివరాలు") else NutriStrings.s(lang, "7-Day Circular Habit Orbit", "7 రోజుల అలవాట్ల చక్రం"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewMode == "Monthly") NutriStrings.s(lang, "Showing latest days across the monthly cycle", "నెలవారీ చక్రంలో తాజా రోజులు") else NutriStrings.s(lang, "Circular completion nodes with target indicators", "రోజువారీ లక్ష్యాల సాధన చిహ్నాలు"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val daysToShow = if (summary.weeklyDays.size > 7) summary.weeklyDays.takeLast(7) else summary.weeklyDays
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysToShow.forEach { day ->
                                    CircularDayNode(day = day)
                                }
                            }
                        }
                    }
                }

                // Visualizer 5: Speedometer Adherence Dial
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("speedometer_adherence_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = NutriStrings.s(lang, "Consistency Gauge Dial", "క్రమశిక్షణ స్కేల్"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = NutriStrings.s(lang, "180° radial consistency indicator", "మీ రోజువారీ అలవాట్ల క్రమశిక్షణ"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            RadialSpeedometerGauge(
                                score = summary.completionRatePercent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (summary.completionRatePercent >= 80) {
                                    NutriStrings.s(lang, "🌟 Strong Sustainable Adherence", "🌟 అద్భుతమైన క్రమశిక్షణ")
                                } else if (summary.completionRatePercent > 0) {
                                    NutriStrings.s(lang, "🌱 Building Regular Momentum", "🌱 క్రమంగా పురోగతి చెందుతున్నారు")
                                } else {
                                    NutriStrings.s(lang, "⏳ Fresh Slate — Ready for Tracking", "⏳ తాజా ఆరంభం — ట్రాకింగ్‌కి సిద్ధం")
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.completionRatePercent >= 80) NutriTeal else NutriAmber
                            )
                            Text(
                                text = if (summary.completionRatePercent > 0) {
                                    NutriStrings.s(lang, "Daily habits remain consistent across scheduled meals and substitutions.", "భోజనాలు మరియు మార్పులలో మీ రోజువారీ అలవాట్లు స్థిరంగా ఉన్నాయి.")
                                } else {
                                    NutriStrings.s(lang, "No consumption logged yet. Your consistency dial will calibrate as you hit daily targets.", "ఇంకా భోజనం నమోదు చేయలేదు. మీరు ఆహారాలను నమోదు చేస్తుంటే ఈ స్కేల్ పెరుగుతుంది.")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Encouraging Habit Takeaway
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NutriTealContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = NutriStrings.s(lang, "NutriPath Wisdom", "నూట్రిపాత్ సలహా"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriOnTealContainer
                                )
                                Text(
                                    text = if (summary.totalCalories > 0) {
                                        NutriStrings.s(
                                            lang,
                                            "Your habit rings reflect your cumulative ${if (viewMode == "Monthly") "30-day" else "weekly"} nutrient and hydration goals. As you log meals, your progress scales towards your full period targets.",
                                            "మీ అలవాట్ల రింగులు ${if (viewMode == "Monthly") "30 రోజుల" else "వారపు"} లక్ష్యాలను చూపుతాయి. మీరు భోజనాలను నమోదు చేస్తున్నప్పుడు, మీ లక్ష్య సాధన పెరుగుతుంది."
                                        )
                                    } else {
                                        NutriStrings.s(
                                            lang,
                                            "Starting fresh allows you to build steady dietary consistency. Log your meals or water to see progress towards your ${if (viewMode == "Monthly") "monthly" else "weekly"} goals.",
                                            "తాజా ఆరంభంతో క్రమశిక్షణను నిర్మించుకోవచ్చు. మీ ${if (viewMode == "Monthly") "నెలవారీ" else "వారపు"} లక్ష్యాల పురోగతిని చూడటానికి ఆహారం లేదా నీటిని నమోదు చేయండి."
                                        )
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visualizer 1: Multi-Ring Concentric Progress Gauge (Smart Rings)
 */
@Composable
fun MultiRingProgressCanvas(
    calorieRatio: Float,
    proteinRatio: Float,
    waterRatio: Float,
    fiberRatio: Float,
    centerPercentage: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            val spacing = 12.dp.toPx()
            val maxRadius = (size.minDimension / 2f) - (strokeWidth / 2f)

            val rings = listOf(
                Pair(calorieRatio, NutriAmber),
                Pair(proteinRatio, NutriProteinRed),
                Pair(waterRatio, NutriWaterCyan),
                Pair(fiberRatio, NutriFiberGreen)
            )

            rings.forEachIndexed { index, (ratio, color) ->
                val currentRadius = maxRadius - (index * spacing)
                val topLeft = Offset(center.x - currentRadius, center.y - currentRadius)
                val arcSize = Size(currentRadius * 2, currentRadius * 2)

                // Track (Background)
                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress Arc
                if (ratio > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * ratio.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$centerPercentage%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun RingLegendItem(
    name: String,
    currentValue: String,
    targetValue: String,
    percent: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (name.contains("water", ignoreCase = true) || name.contains("hydrat", ignoreCase = true)) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        MacroAppIcon(
                            macroOrCategory = name,
                            modifier = Modifier.size(15.dp),
                            overrideColor = color
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$currentValue / $targetValue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.5.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

/**
 * Visualizer 2: Spline Area Flow Curve
 */
@Composable
fun SplineAreaTrendChart(
    days: List<WeeklyDayStat>,
    targetCalories: Double,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    val tealColor = NutriTeal
    val amberColor = NutriAmber

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (days.size - 1).coerceAtLeast(1)

            val maxRecorded = days.maxOfOrNull { it.calories } ?: 0.0
            val maxCal = maxOf(maxRecorded, targetCalories, 1000.0).toFloat() * 1.15f

            fun getY(cals: Double): Float {
                val normalized = (cals.toFloat() / maxCal).coerceIn(0f, 1f)
                return height - (normalized * (height - 16.dp.toPx()))
            }

            // Draw Target Baseline Dashed Line
            val targetY = getY(targetCalories)
            drawLine(
                color = amberColor.copy(alpha = 0.5f),
                start = Offset(0f, targetY),
                end = Offset(width, targetY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            val points = days.mapIndexed { index, day ->
                Offset(index * stepX, getY(day.calories))
            }

            // Build Smooth Spline Curve Path
            val path = Path()
            val fillPath = Path()

            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, height)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                    val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                    path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                // Draw Area Fill Gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(tealColor.copy(alpha = 0.35f), tealColor.copy(alpha = 0.02f)),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Smooth Stroke
                drawPath(
                    path = path,
                    color = tealColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Point Glowing Circles
                points.forEachIndexed { i, pt ->
                    val day = days[i]
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = if (day.isTargetHit) tealColor else if (day.calories > 0) amberColor else Color.LightGray,
                        radius = 3.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day Labels Row below the chart
        val daysForLabels = if (days.size > 7) {
            days.filterIndexed { index, _ -> index % 4 == 0 || index == days.size - 1 }
        } else {
            days
        }

        val lang = LocalAppLanguage.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysForLabels.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = localizedInsightDay(day.dayLabel, lang),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (day.calories == 0.0) "0k" else "${(day.calories / 1000.0).let { String.format(java.util.Locale.US, "%.1f", it) }}k",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (day.isTargetHit) NutriTeal else if (day.calories > 0) NutriAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Visualizer 3: Macro Donut Canvas Chart
 */
@Composable
fun MacroDonutCanvas(
    proteinGrams: Double,
    carbsGrams: Double,
    fatGrams: Double,
    totalCalories: Double,
    modifier: Modifier = Modifier,
    periodLabel: String = "kcal total"
) {
    val proteinCals = proteinGrams * 4.0
    val carbsCals = carbsGrams * 4.0
    val fatCals = fatGrams * 9.0
    val totalMacroCals = proteinCals + carbsCals + fatCals
    val hasData = totalMacroCals > 0.0

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val radius = (size.minDimension / 2f) - (stroke / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            if (!hasData) {
                // Empty Track Ring when reset or zero
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            } else {
                val totalCals = totalMacroCals
                val proteinSweep = ((proteinCals / totalCals) * 360f).toFloat()
                val carbsSweep = ((carbsCals / totalCals) * 360f).toFloat()
                val fatSweep = 360f - proteinSweep - carbsSweep

                var startAngle = -90f

                // Protein Arc
                drawArc(
                    color = NutriProteinRed,
                    startAngle = startAngle,
                    sweepAngle = proteinSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                startAngle += proteinSweep

                // Carbs Arc
                drawArc(
                    color = NutriCarbGold,
                    startAngle = startAngle,
                    sweepAngle = carbsSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                startAngle += carbsSweep

                // Fat Arc
                drawArc(
                    color = NutriFatOrange,
                    startAngle = startAngle,
                    sweepAngle = fatSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${totalCalories.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DonutLegendRow(
    label: String,
    consumed: String,
    target: String,
    percentageStr: String,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MacroAppIcon(
                    macroOrCategory = label,
                    modifier = Modifier.size(18.dp),
                    overrideColor = color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "$consumed / $target",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = percentageStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

fun localizedInsightDay(label: String, lang: AppLanguage): String {
    if (lang != AppLanguage.TELUGU) return label
    return when (label.trim().take(3).lowercase()) {
        "mon" -> "సోమ"
        "tue" -> "మంగళ"
        "wed" -> "బుధ"
        "thu" -> "గురు"
        "fri" -> "శుక్ర"
        "sat" -> "శని"
        "sun" -> "ఆది"
        else -> label
    }
}

/**
 * Visualizer 4: 7-Day Circular Constellation Nodes
 */
@Composable
fun CircularDayNode(day: WeeklyDayStat) {
    val lang = LocalAppLanguage.current
    val isZero = day.calories == 0.0 && !day.isTargetHit
    val nodeBg = when {
        day.isTargetHit -> NutriTeal.copy(alpha = 0.15f)
        day.calories > 0 -> NutriAmber.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val nodeBorder = when {
        day.isTargetHit -> NutriTeal
        day.calories > 0 -> NutriAmber
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(nodeBg)
                .border(
                    width = if (isZero) 1.dp else 2.dp,
                    color = nodeBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isTargetHit) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = NutriTeal,
                    modifier = Modifier.size(20.dp)
                )
            } else if (day.calories > 0) {
                Text(
                    text = "${(day.calories / 1000).toInt()}k",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriAmber
                )
            } else {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedInsightDay(day.dayLabel, lang),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Visualizer 5: 180° Speedometer Radial Gauge
 */
@Composable
fun RadialSpeedometerGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "gauge_animation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension / 1.3f)
            val centerPoint = Offset(size.width / 2f, size.height - 10.dp.toPx())
            val topLeft = Offset(centerPoint.x - radius, centerPoint.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            // Background Track Arc (180 degrees, from 180 to 0)
            drawArc(
                color = Color.LightGray.copy(alpha = 0.25f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Gradient Arc
            val sweep = (animatedScore / 100f).coerceIn(0f, 1f) * 180f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(NutriAmber, NutriTeal),
                        startX = centerPoint.x - radius,
                        endX = centerPoint.x + radius
                    ),
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Needle Dot marker at end of arc
            val angleRad = Math.toRadians((180f + sweep).toDouble())
            val markerX = (centerPoint.x + radius * cos(angleRad)).toFloat()
            val markerY = (centerPoint.y + radius * sin(angleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(markerX, markerY)
            )
            drawCircle(
                color = if (score > 0) NutriTeal else Color.Gray,
                radius = 5.dp.toPx(),
                center = Offset(markerX, markerY)
            )
        }

        Column(
            modifier = Modifier.padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = if (score > 0) NutriTeal else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = NutriStrings.s(lang, "Adherence Rate", "లక్ష్య సాధన రేటు"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
