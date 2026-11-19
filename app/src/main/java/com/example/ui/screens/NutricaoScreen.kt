package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MealRecord
import com.example.data.UserProfile
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondary
import com.example.ui.theme.ApexSuccessGreen
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTertiary
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import com.example.viewmodel.MealScanUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NutricaoScreen(
    profile: UserProfile,
    meals: List<MealRecord>,
    mealScanState: MealScanUiState,
    onToggleMeal: (MealRecord) -> Unit,
    onAddCustomMeal: (String, String, String, Int, Int, Int, Int, Int) -> Unit,
    onDeleteMeal: (String) -> Unit,
    onScanMealWithAi: (String) -> Unit,
    onResetMealScan: () -> Unit,
    onAddWater: (Int) -> Unit,
    onOpenBiometrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualMealDialog by remember { mutableStateOf(false) }
    var showAiScanDialog by remember { mutableStateOf(false) }

    val consumedCalories = remember(meals) { meals.filter { it.completed }.sumOf { it.calories }.toInt() }
    val consumedProtein = remember(meals) { meals.filter { it.completed }.sumOf { it.proteinG }.toInt() }
    val consumedCarbs = remember(meals) { meals.filter { it.completed }.sumOf { it.carbsG }.toInt() }
    val consumedFat = remember(meals) { meals.filter { it.completed }.sumOf { it.fatG }.toInt() }
    val consumedFiber = remember(meals) { meals.filter { it.completed }.sumOf { it.fiberG }.toInt() }

    val targetCalories = profile.targetCalories
    val calorieProgress = (consumedCalories.toFloat() / targetCalories.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val waterGoal = profile.waterGoalMl.coerceAtLeast(1500L).toInt()
    val waterProgress = (profile.waterMl.toFloat() / waterGoal.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Bio-Sync Metabolic Banner (driven by real onboarding measurements)
        item(key = "bio_sync") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .border(1.dp, ApexPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenBiometrics)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Science, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                    Column {
                        Text("METAS CALCULADAS DAS SUAS MEDIDAS", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexPrimary)
                        Text(
                            text = "TMB ${ApexViewModel.formatPtBrNumber(profile.bmrKcal)} kcal • Meta ${ApexViewModel.formatPtBrNumber(targetCalories)} kcal (${profile.goalLabel})",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurface
                        )
                    }
                }
                Text("Editar", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimary)
            }
        }

        // 2. Calorie Donut + 4 Linear Macro Rails
        item(key = "calorie_macros_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(116.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                            drawArc(
                                color = ApexSurfaceContainerHighest,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = stroke
                            )
                            drawArc(
                                color = ApexPrimaryContainer,
                                startAngle = -90f,
                                sweepAngle = 360f * calorieProgress,
                                useCenter = false,
                                style = stroke
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = ApexViewModel.formatPtBrNumber(consumedCalories),
                                style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Black),
                                color = ApexOnSurface
                            )
                            Text(
                                text = "/ ${ApexViewModel.formatPtBrNumber(targetCalories)} kcal",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                                color = ApexOnSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroRail("Proteína", consumedProtein, profile.proteinGoalG, ApexPrimary)
                        MacroRail("Carboidratos", consumedCarbs, profile.carbsGoalG, ApexSecondary)
                        MacroRail("Gorduras", consumedFat, profile.fatGoalG, ApexTertiary)
                        MacroRail("Fibras", consumedFiber, profile.fiberGoalG, ApexSuccessGreen)
                    }
                }

                // Quick register buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showManualMealDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexSurfaceContainerHigh,
                            contentColor = ApexOnSurface
                        ),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("btn_registrar_refeicao")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar", style = ApexTextStyles.LabelInteractive)
                    }
                    Button(
                        onClick = {
                            onResetMealScan()
                            showAiScanDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexPrimaryContainer,
                            contentColor = ApexOnPrimary
                        ),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("btn_scan_ia_refeicao")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan com IA", style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold))
                    }
                }
            }
        }

        // 3. Hydration Monitor (35ml/kg real goal)
        item(key = "hydration_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                        Column {
                            Text("HIDRATAÇÃO CELULAR", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                            Text(
                                text = "${ApexViewModel.formatPtBrLong(profile.waterMl)} / ${ApexViewModel.formatPtBrNumber(waterGoal)} ml",
                                style = ApexTextStyles.HeadlineSm,
                                color = ApexOnSurface
                            )
                        }
                    }
                    Text(
                        text = "${(waterProgress * 100).toInt()}%",
                        style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Black),
                        color = ApexPrimary
                    )
                }

                LinearProgressIndicator(
                    progress = { waterProgress },
                    modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                    color = ApexPrimaryContainer,
                    trackColor = ApexSurfaceContainerLowest,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddWater(250) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexSurfaceContainerHigh, contentColor = ApexPrimary),
                        modifier = Modifier.weight(1f).testTag("btn_water_250")
                    ) {
                        Icon(Icons.Rounded.LocalDrink, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+250 ml", style = ApexTextStyles.LabelInteractive)
                    }
                    Button(
                        onClick = { onAddWater(500) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary),
                        modifier = Modifier.weight(1f).testTag("btn_water_500")
                    ) {
                        Text("+500 ml", style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold))
                    }
                }
            }
        }

        // 4. Meal Schedule (Clean Empty State or Real Logged Meals)
        item(key = "meals_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TIMING DE NUTRIENTES", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                    Text("Refeições Registradas", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                }
                Text(
                    text = "${meals.size} item(ns)",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexPrimary
                )
            }
        }

        if (meals.isEmpty()) {
            item(key = "empty_meals") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApexSurfaceContainer)
                        .border(1.dp, ApexOutlineVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(30.dp))
                    Text("Nenhuma refeição registrada hoje", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    Text(
                        text = "Use 'Registrar' para lançar manualmente ou 'Scan com IA' para descrever seu prato e estimar calorias e macros automaticamente.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(meals, key = { it.id }) { meal ->
                MealItemCard(
                    meal = meal,
                    onToggle = { onToggleMeal(meal) },
                    onDelete = { onDeleteMeal(meal.id) }
                )
            }
        }
    }

    // Dialog 1: Manual Meal
    if (showManualMealDialog) {
        ManualMealDialog(
            onDismiss = { showManualMealDialog = false },
            onSave = { time, title, desc, kcal, p, c, f, fib ->
                onAddCustomMeal(time, title, desc, kcal, p, c, f, fib)
                showManualMealDialog = false
            }
        )
    }

    // Dialog 2: Gemini AI Macro Scanner
    if (showAiScanDialog) {
        AiMealScanDialog(
            scanState = mealScanState,
            onScan = onScanMealWithAi,
            onConfirmSave = { est ->
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                onAddCustomMeal(timeNow, est.title, est.description, est.calories, est.proteinG, est.carbsG, est.fatG, est.fiberG)
                onResetMealScan()
                showAiScanDialog = false
            },
            onDismiss = {
                onResetMealScan()
                showAiScanDialog = false
            }
        )
    }
}

@Composable
private fun MacroRail(label: String, consumed: Int, goal: Int, color: Color) {
    val ratio = (consumed.toFloat() / goal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
            Text("${consumed}g / ${goal}g", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurface)
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = ApexSurfaceContainerLowest,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MealItemCard(
    meal: MealRecord,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .border(
                1.dp,
                if (meal.completed) ApexPrimary.copy(alpha = 0.25f) else ApexOutlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (meal.completed) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (meal.completed) {
                Icon(Icons.Rounded.Check, contentDescription = "Consumida", tint = ApexOnPrimary, modifier = Modifier.size(18.dp))
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(meal.timeLabel, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimary)
                Text(meal.title, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
            }
            if (meal.description.isNotBlank()) {
                Text(meal.description, style = ApexTextStyles.BodySm.copy(fontSize = 12.sp), color = ApexOnSurfaceVariant)
            }
            Text(
                text = "${meal.calories} kcal • P ${meal.proteinG}g • C ${meal.carbsG}g • G ${meal.fatG}g",
                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                color = ApexSecondary
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remover refeição", tint = ApexError)
        }
    }
}

@Composable
private fun ManualMealDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, Int, Int, Int) -> Unit
) {
    val defaultTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    var time by remember { mutableStateOf(defaultTime) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ApexSurfaceContainer,
        titleContentColor = ApexOnSurface,
        title = { Text("Registrar Refeição") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it.take(5) },
                        label = { Text("Hora") },
                        singleLine = true,
                        modifier = Modifier.width(88.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(80) },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it.take(180) },
                    label = { Text("Alimentos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kcal,
                        onValueChange = { kcal = it.filter(Char::isDigit).take(5) },
                        label = { Text("kcal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter(Char::isDigit).take(4) },
                        label = { Text("Prot(g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter(Char::isDigit).take(4) },
                        label = { Text("Carb(g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter(Char::isDigit).take(4) },
                        label = { Text("Gord(g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = protein.toIntOrNull() ?: 0
                    val c = carbs.toIntOrNull() ?: 0
                    val f = fat.toIntOrNull() ?: 0
                    val k = kcal.toIntOrNull() ?: ((p * 4) + (c * 4) + (f * 9))
                    onSave(time, title.ifBlank { "Refeição" }, desc, k, p, c, f, 4)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = ApexOnSurfaceVariant) }
        }
    )
}

@Composable
private fun AiMealScanDialog(
    scanState: MealScanUiState,
    onScan: (String) -> Unit,
    onConfirmSave: (com.example.data.ScannedMealEstimate) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ApexSurfaceContainer,
        titleContentColor = ApexOnSurface,
        title = { Text("Scan Nutricional com Gemini IA") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Descreva os alimentos e quantidades (ex: 150g frango grelhado, 200g arroz e salada).",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(240) },
                    label = { Text("O que você comeu?") },
                    modifier = Modifier.fillMaxWidth()
                )
                when (scanState) {
                    MealScanUiState.Idle -> Unit
                    MealScanUiState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ApexPrimary, strokeWidth = 2.dp)
                            Text("Calculando macros com Gemini...", style = ApexTextStyles.BodySm, color = ApexPrimary)
                        }
                    }
                    is MealScanUiState.Error -> {
                        Text(scanState.message, style = ApexTextStyles.BodySm, color = ApexError)
                    }
                    is MealScanUiState.Success -> {
                        val e = scanState.estimate
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexSurfaceContainerLowest)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(e.title, style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
                            Text("${e.calories} kcal • P ${e.proteinG}g • C ${e.carbsG}g • G ${e.fatG}g • F ${e.fiberG}g", style = ApexTextStyles.BodySm, color = ApexOnSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (scanState) {
                is MealScanUiState.Success -> {
                    Button(
                        onClick = { onConfirmSave(scanState.estimate) },
                        colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
                    ) { Text("Salvar Refeição") }
                }
                else -> {
                    Button(
                        onClick = { onScan(description) },
                        enabled = description.isNotBlank() && scanState !is MealScanUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
                    ) { Text("Estimar Macros") }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar", color = ApexOnSurfaceVariant) }
        }
    )
}
