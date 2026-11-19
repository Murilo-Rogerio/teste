package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.data.WorkoutRoutine
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexGold
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondary
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import com.example.viewmodel.ExerciseLibraryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinosScreen(
    profile: UserProfile,
    routines: List<WorkoutRoutine>,
    selectedFilter: String,
    exerciseLibrary: List<ExerciseLibraryItem>,
    onSelectFilter: (String) -> Unit,
    onOpenBiometrics: () -> Unit,
    onSaveNewRoutine: (String, List<String>, Int, String, Double, Int, String) -> Unit,
    onToggleFavorite: (WorkoutRoutine) -> Unit,
    onDeleteRoutine: (String) -> Unit,
    onStartWorkout: (WorkoutRoutine) -> Unit,
    onStartFreeExercise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var showLibrarySheet by remember { mutableStateOf(false) }

    val filteredRoutines = remember(routines, selectedFilter) {
        when (selectedFilter) {
            "favs" -> routines.filter { it.favorite }
            "all" -> routines
            else -> routines.filter { it.category == selectedFilter }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Athlete Streak, XP & Real Biometrics Hero Card
        item(key = "hero_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ApexSurfaceContainerHigh, ApexSurfaceContainer)
                        )
                    )
                    .border(1.dp, ApexPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OLÁ, ${profile.displayName.uppercase()}",
                            style = ApexTextStyles.LabelCaps,
                            color = ApexPrimary
                        )
                        Text(
                            text = "@${profile.username} • ${profile.goalLabel}",
                            style = ApexTextStyles.HeadlineSm,
                            color = ApexOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerLowest)
                            .border(1.dp, ApexGold.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = ApexGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${profile.streakDays} dias",
                            style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp),
                            color = ApexOnSurface
                        )
                    }
                }

                // Real biometric telemetry strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerLowest.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeroMiniStat("PESO", "${String.format("%.1f", profile.weightKg)} kg")
                    HeroMiniStat("ALTURA", "${profile.heightCm.toInt()} cm")
                    HeroMiniStat("META KCAL", ApexViewModel.formatPtBrNumber(profile.targetCalories))
                    HeroMiniStat("FICHAS", routines.size.toString())
                }

                // XP Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "NÍVEL ${profile.level} • EVOLUÇÃO DO ATLETA",
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                            color = ApexOnSurfaceVariant
                        )
                        Text(
                            text = "${profile.currentXp} / ${profile.nextLevelXp} XP",
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                            color = ApexPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            (profile.currentXp.toFloat() / profile.nextLevelXp.coerceAtLeast(1L).toFloat())
                                .coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = ApexPrimaryContainer,
                        trackColor = ApexSurfaceContainerLowest,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // 2. Quick Triggers Row
        item(key = "quick_triggers") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickTriggerCard(
                    title = "+ Criar Ficha",
                    subtitle = "Montar do zero",
                    icon = Icons.Rounded.AddCircle,
                    accent = ApexPrimary,
                    onClick = { showCreateSheet = true },
                    modifier = Modifier.weight(1f).testTag("btn_criar_ficha")
                )
                QuickTriggerCard(
                    title = "Exercícios",
                    subtitle = "${exerciseLibrary.size} no banco",
                    icon = Icons.Rounded.FitnessCenter,
                    accent = ApexSecondary,
                    onClick = { showLibrarySheet = true },
                    modifier = Modifier.weight(1f).testTag("btn_banco_exercicios")
                )
                QuickTriggerCard(
                    title = "Medidas",
                    subtitle = "${String.format("%.1f", profile.weightKg)} kg",
                    icon = Icons.Rounded.Straighten,
                    accent = ApexPrimary,
                    onClick = onOpenBiometrics,
                    modifier = Modifier.weight(1f).testTag("btn_minhas_medidas")
                )
            }
        }

        // 3. Split Filter Chips
        item(key = "filter_chips") {
            val filters = listOf(
                "all" to "Todas (${routines.size})",
                "favs" to "Favoritos",
                "hypertrophy" to "Hipertrofia",
                "strength" to "Força",
                "ppl" to "PPL",
                "upper_lower" to "Upper/Lower",
                "full_body" to "Full Body"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, label) ->
                    val selected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                            .clickable { onSelectFilter(key) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                            .testTag("filter_chip_$key")
                    ) {
                        Text(
                            text = label,
                            style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp),
                            color = if (selected) ApexOnPrimary else ApexOnSurface
                        )
                    }
                }
            }
        }

        // 4. Routines List or Clean Empty State
        if (filteredRoutines.isEmpty()) {
            item(key = "empty_routines") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApexSurfaceContainer)
                        .border(1.dp, ApexOutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ApexPrimaryContainer.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            tint = ApexPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = if (routines.isEmpty()) "Sua conta começa 100% limpa" else "Nenhuma ficha neste filtro",
                        style = ApexTextStyles.HeadlineSm,
                        color = ApexOnSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (routines.isEmpty()) {
                            "Crie sua primeira ficha personalizada com os exercícios, séries e tempos de descanso do seu treino real."
                        } else {
                            "Troque o filtro acima ou adicione uma nova ficha para esta categoria."
                        },
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showCreateSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexPrimaryContainer,
                            contentColor = ApexOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("empty_create_first_routine_btn")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Criar Minha Primeira Ficha",
                            style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        } else {
            items(filteredRoutines, key = { it.id }) { routine ->
                RoutineCard(
                    routine = routine,
                    onToggleFavorite = { onToggleFavorite(routine) },
                    onDelete = { onDeleteRoutine(routine.id) },
                    onStart = { onStartWorkout(routine) }
                )
            }
        }
    }

    // Bottom Sheet: Criar Nova Ficha
    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ApexSurfaceContainerLow,
            contentColor = ApexOnSurface
        ) {
            CreateRoutineSheetContent(
                exerciseLibrary = exerciseLibrary,
                onCancel = { showCreateSheet = false },
                onSave = { name, exercises, sets, reps, rpe, rest, category ->
                    onSaveNewRoutine(name, exercises, sets, reps, rpe, rest, category)
                    showCreateSheet = false
                }
            )
        }
    }

    // Bottom Sheet: Banco de Exercícios
    if (showLibrarySheet) {
        ModalBottomSheet(
            onDismissRequest = { showLibrarySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ApexSurfaceContainerLow,
            contentColor = ApexOnSurface
        ) {
            ExerciseLibrarySheetContent(
                exerciseLibrary = exerciseLibrary,
                onStartWithExercise = { name ->
                    showLibrarySheet = false
                    onStartFreeExercise(name)
                },
                onClose = { showLibrarySheet = false }
            )
        }
    }
}

@Composable
private fun HeroMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
        Text(
            text = value,
            style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
            color = ApexOnSurface
        )
    }
}

@Composable
private fun QuickTriggerCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .border(1.dp, ApexOutlineVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(22.dp))
        Text(
            text = title,
            style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = ApexOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = ApexTextStyles.BodySm.copy(fontSize = 10.sp),
            color = ApexOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RoutineCard(
    routine: WorkoutRoutine,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurfaceContainer)
            .border(1.dp, ApexPrimary.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("routine_card_${routine.id}"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexPrimaryContainer.copy(alpha = 0.16f))
                        .border(1.dp, ApexPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = routine.letter,
                        style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Black),
                        color = ApexPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.statusBadge,
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                        color = ApexPrimary
                    )
                    Text(
                        text = routine.title,
                        style = ApexTextStyles.HeadlineSm,
                        color = ApexOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (routine.favorite) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = "Favoritar ficha",
                        tint = if (routine.favorite) ApexGold else ApexOnSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Remover ficha",
                        tint = ApexError
                    )
                }
            }
        }

        Text(
            text = routine.subtitle,
            style = ApexTextStyles.BodySm,
            color = ApexOnSurfaceVariant
        )

        if (routine.exerciseNames.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApexSurfaceContainerLowest.copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                routine.exerciseNames.take(4).forEachIndexed { idx, ex ->
                    Text(
                        text = "${idx + 1}. $ex",
                        style = ApexTextStyles.BodySm.copy(fontSize = 12.sp),
                        color = ApexOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (routine.exerciseNames.size > 4) {
                    Text(
                        text = "+${routine.exerciseNames.size - 4} exercícios",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${routine.exerciseCount.coerceAtLeast(1)} exerc.",
                    style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                    color = ApexOnSurfaceVariant
                )
                Text(
                    text = "${routine.totalSets} séries",
                    style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                    color = ApexOnSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(13.dp))
                    Text(
                        text = "~${routine.estimatedDurationMin} min",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexPrimary
                    )
                }
            }

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ApexPrimaryContainer,
                    contentColor = ApexOnPrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Iniciar",
                    style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun CreateRoutineSheetContent(
    exerciseLibrary: List<ExerciseLibraryItem>,
    onCancel: () -> Unit,
    onSave: (String, List<String>, Int, String, Double, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var customExercise by remember { mutableStateOf("") }
    var targetSets by remember { mutableIntStateOf(4) }
    var targetReps by remember { mutableStateOf("8-12") }
    var targetRpe by remember { mutableDoubleStateOf(8.0) }
    var targetRest by remember { mutableIntStateOf(90) }
    var category by remember { mutableStateOf("hypertrophy") }
    val selectedExercises = remember { mutableStateListOf<String>() }

    val categories = listOf(
        "hypertrophy" to "Hipertrofia",
        "strength" to "Força",
        "ppl" to "PPL",
        "upper_lower" to "Upper/Lower",
        "full_body" to "Full Body"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NOVA FICHA PERSONALIZADA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Text("Montar Treino", style = ApexTextStyles.HeadlineMd, color = ApexOnSurface)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Rounded.Close, contentDescription = "Fechar", tint = ApexOnSurfaceVariant)
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(80) },
            label = { Text("Nome da ficha (ex: Push A, Pernas)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ApexPrimary,
                unfocusedBorderColor = ApexOutlineVariant,
                focusedTextColor = ApexOnSurface,
                unfocusedTextColor = ApexOnSurface
            ),
            modifier = Modifier.fillMaxWidth().testTag("sheet_routine_name_input")
        )

        // Category pills
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (key, label) ->
                val active = category == key
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (active) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                        .clickable { category = key }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp),
                        color = if (active) ApexOnPrimary else ApexOnSurface
                    )
                }
            }
        }

        // Sets / Reps / Rest row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = targetSets.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { targetSets = it.coerceIn(1, 15) } },
                label = { Text("Séries") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = ApexOutlineVariant,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = targetReps,
                onValueChange = { targetReps = it.take(12) },
                label = { Text("Reps") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = ApexOutlineVariant,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = targetRest.toString(),
                onValueChange = { v -> v.toIntOrNull()?.let { targetRest = it.coerceIn(15, 600) } },
                label = { Text("Desc(s)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = ApexOutlineVariant,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Add custom exercise
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customExercise,
                onValueChange = { customExercise = it.take(80) },
                label = { Text("Adicionar exercício personalizado") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = ApexOutlineVariant,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val clean = customExercise.trim()
                    if (clean.isNotEmpty() && !selectedExercises.contains(clean)) {
                        selectedExercises.add(clean)
                        customExercise = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Adicionar exercício")
            }
        }

        // Quick select from catalog
        Text(
            text = "SELECIONE EXERCÍCIOS (${selectedExercises.size} selecionados)",
            style = ApexTextStyles.LabelCaps,
            color = ApexOnSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            exerciseLibrary.forEach { item ->
                val isSelected = selectedExercises.contains(item.name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ApexPrimaryContainer.copy(alpha = 0.16f) else ApexSurfaceContainer)
                        .border(
                            1.dp,
                            if (isSelected) ApexPrimary else ApexOutlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (isSelected) selectedExercises.remove(item.name)
                            else if (selectedExercises.size < 20) selectedExercises.add(item.name)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                        Text(item.subtitle, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                    }
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = ApexPrimary)
                    }
                }
            }
        }

        Button(
            onClick = {
                val finalTitle = name.trim().ifBlank { "Treino Personalizado" }
                onSave(finalTitle, selectedExercises.toList(), targetSets, targetReps, targetRpe, targetRest, category)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("sheet_save_routine_btn")
        ) {
            Text(
                text = "Salvar Ficha na Conta",
                style = ApexTextStyles.LabelInteractive.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ExerciseLibrarySheetContent(
    exerciseLibrary: List<ExerciseLibraryItem>,
    onStartWithExercise: (String) -> Unit,
    onClose: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, exerciseLibrary) {
        if (search.isBlank()) exerciseLibrary
        else exerciseLibrary.filter {
            it.name.contains(search, ignoreCase = true) || it.subtitle.contains(search, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CATÁLOGO BIOMECÂNICO", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Text("Banco de Exercícios", style = ApexTextStyles.HeadlineMd, color = ApexOnSurface)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Fechar", tint = ApexOnSurfaceVariant)
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Buscar por músculo ou exercício...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ApexPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ApexPrimary,
                unfocusedBorderColor = ApexOutlineVariant,
                focusedTextColor = ApexOnSurface,
                unfocusedTextColor = ApexOnSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        filtered.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ApexSurfaceContainer)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                    Text(item.subtitle, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                }
                Button(
                    onClick = { onStartWithExercise(item.name) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApexPrimaryContainer.copy(alpha = 0.2f),
                        contentColor = ApexPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Treinar", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
