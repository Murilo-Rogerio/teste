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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActiveSet
import com.example.data.UserProfile
import com.example.data.WorkoutRoutine
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
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import kotlin.math.roundToInt

@Composable
fun TreinoAtivoScreen(
    profile: UserProfile,
    routines: List<WorkoutRoutine>,
    sets: List<ActiveSet>,
    elapsedSeconds: Int,
    restRemainingSeconds: Int,
    restTotalPresetSeconds: Int,
    soundEnabled: Boolean,
    onSelectRestPreset: (Int) -> Unit,
    onAdd30Seconds: () -> Unit,
    onSkipRest: () -> Unit,
    onToggleSound: () -> Unit,
    onAdjustWeight: (ActiveSet, Double) -> Unit,
    onAdjustReps: (ActiveSet, Long) -> Unit,
    onCycleRpe: (ActiveSet) -> Unit,
    onToggleSetCompleted: (ActiveSet) -> Unit,
    onAddSet: (String) -> Unit,
    onDeleteSet: (String) -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    onFinishWorkout: () -> Unit,
    onStartRoutine: (WorkoutRoutine) -> Unit,
    onStartFreeExercise: (String) -> Unit,
    onNavigateToRoutines: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notesDraft by remember(profile.exerciseNotes) { mutableStateOf(profile.exerciseNotes) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    val realSessionVolume = remember(sets) {
        sets.filter { it.completed }.sumOf { (it.weightKg * it.reps).roundToInt() }
    }
    val groupedByExercise = remember(sets) { sets.groupBy { it.exerciseName } }

    if (sets.isEmpty()) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "empty_active") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApexSurfaceContainer)
                        .border(1.dp, ApexPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
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
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = ApexPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "Nenhum Treino Ativo",
                        style = ApexTextStyles.HeadlineSm,
                        color = ApexOnSurface
                    )
                    Text(
                        text = "Inicie uma das suas fichas criadas ou comece um treino livre registrando o primeiro exercício.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddExerciseDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexPrimaryContainer,
                                contentColor = ApexOnPrimary
                            ),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_start_free_workout")
                        ) {
                            Text("Treino Livre", style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = onNavigateToRoutines,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexSurfaceContainerHigh,
                                contentColor = ApexOnSurface
                            ),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Ver Fichas", style = ApexTextStyles.LabelInteractive)
                        }
                    }
                }
            }

            if (routines.isNotEmpty()) {
                item(key = "quick_start_header") {
                    Text(
                        text = "INICIAR RAPIDAMENTE UMA FICHA SALVA",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexOnSurfaceVariant
                    )
                }
                items(routines, key = { it.id }) { routine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ApexSurfaceContainer)
                            .clickable { onStartRoutine(routine) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(routine.title, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                            Text(routine.subtitle, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                        }
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Iniciar", tint = ApexPrimary)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live HUD
            item(key = "session_hud") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(ApexSurfaceContainer)
                        .border(1.dp, ApexPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SESSÃO EM ANDAMENTO", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                            Text(
                                text = profile.activeWorkoutName.ifBlank { "Treino Ativo" },
                                style = ApexTextStyles.HeadlineSm,
                                color = ApexOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexPrimaryContainer.copy(alpha = 0.16f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = ApexViewModel.formatTimer(elapsedSeconds),
                                style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.ExtraBold),
                                color = ApexPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerLowest)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        HudMetric("VOLUME REAL", "${ApexViewModel.formatPtBrNumber(realSessionVolume)} kg", ApexPrimary)
                        HudMetric("SÉRIES FEITAS", "${sets.count { it.completed }} / ${sets.size}", ApexSuccessGreen)
                        HudMetric("EXERCÍCIOS", groupedByExercise.size.toString(), ApexSecondary)
                    }
                }
            }

            // 2. Smart Rest Timer Card
            item(key = "rest_timer") {
                val progress = if (restTotalPresetSeconds > 0) {
                    (restRemainingSeconds.toFloat() / restTotalPresetSeconds.toFloat()).coerceIn(0f, 1f)
                } else 0f

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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                    drawArc(
                                        color = ApexSurfaceContainerHighest,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = stroke
                                    )
                                    drawArc(
                                        color = ApexPrimary,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = stroke
                                    )
                                }
                                Text(
                                    text = ApexViewModel.formatTimer(restRemainingSeconds),
                                    style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                                    color = ApexOnSurface
                                )
                            }
                            Column {
                                Text("SMART REST TIMER", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                                Text(
                                    text = if (restRemainingSeconds > 0) "Descanso em contagem" else "Pronto para a próxima série",
                                    style = ApexTextStyles.BodySm,
                                    color = if (restRemainingSeconds > 0) ApexPrimary else ApexSuccessGreen
                                )
                            }
                        }

                        IconButton(onClick = onToggleSound) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                contentDescription = "Alternar som",
                                tint = if (soundEnabled) ApexPrimary else ApexOnSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(60, 90, 120, 180).forEach { sec ->
                            val selected = restTotalPresetSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                                    .clickable { onSelectRestPreset(sec) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    style = ApexTextStyles.LabelCaps.copy(fontSize = 11.sp),
                                    color = if (selected) ApexOnPrimary else ApexOnSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAdd30Seconds,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ApexSurfaceContainerHigh, contentColor = ApexOnSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+30s", style = ApexTextStyles.LabelInteractive)
                        }
                        Button(
                            onClick = onSkipRest,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ApexSurfaceContainerHighest, contentColor = ApexPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pular", style = ApexTextStyles.LabelInteractive)
                        }
                    }
                }
            }

            // 3. Exercise Set Tables (grouped by real exercises)
            groupedByExercise.forEach { (exerciseName, exerciseSets) ->
                item(key = "exercise_group_$exerciseName") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ApexSurfaceContainer)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(20.dp))
                                Text(
                                    text = exerciseName,
                                    style = ApexTextStyles.HeadlineSm,
                                    color = ApexOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = { onAddSet(exerciseName) }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
                                Text("Série", color = ApexPrimary, style = ApexTextStyles.LabelInteractive)
                            }
                        }

                        // Table header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant, modifier = Modifier.width(26.dp))
                            Text("CARGA (KG)", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                            Text("REPS", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("RPE", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
                            Text("OK", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                        }

                        exerciseSets.forEach { set ->
                            ActiveSetRow(
                                set = set,
                                onAdjustWeight = { delta -> onAdjustWeight(set, delta) },
                                onAdjustReps = { delta -> onAdjustReps(set, delta) },
                                onCycleRpe = { onCycleRpe(set) },
                                onToggleComplete = { onToggleSetCompleted(set) },
                                onDelete = { onDeleteSet(set.id) }
                            )
                        }
                    }
                }
            }

            // 4. Add Another Exercise + Technical Notes + Finish CTA
            item(key = "session_footer") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showAddExerciseDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexSurfaceContainerHigh,
                            contentColor = ApexPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_add_exercise_to_session")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar Outro Exercício na Sessão", style = ApexTextStyles.LabelInteractive)
                    }

                    OutlinedTextField(
                        value = notesDraft,
                        onValueChange = {
                            notesDraft = it.take(500)
                            onUpdateNotes(profile.activeExerciseName, notesDraft)
                        },
                        label = { Text("Notas técnicas da sessão") },
                        placeholder = { Text("Ex: cadência controlada, foco na amplitude...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ApexPrimary,
                            unfocusedBorderColor = ApexOutlineVariant,
                            focusedTextColor = ApexOnSurface,
                            unfocusedTextColor = ApexOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onFinishWorkout,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ApexPrimaryContainer,
                            contentColor = ApexOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_finish_workout")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finalizar e Salvar Treino",
                            style = ApexTextStyles.LabelInteractive.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            containerColor = ApexSurfaceContainer,
            titleContentColor = ApexOnSurface,
            textContentColor = ApexOnSurfaceVariant,
            title = { Text("Adicionar Exercício") },
            text = {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it.take(80) },
                    label = { Text("Nome do exercício") },
                    placeholder = { Text("Ex: Supino Reto com Barra") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ApexPrimary,
                        unfocusedBorderColor = ApexOutlineVariant,
                        focusedTextColor = ApexOnSurface,
                        unfocusedTextColor = ApexOnSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newExerciseName.trim()
                        if (clean.isNotEmpty()) {
                            if (sets.isEmpty()) onStartFreeExercise(clean) else onAddSet(clean)
                            newExerciseName = ""
                            showAddExerciseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Cancelar", color = ApexOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun HudMetric(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
        Text(value, style = ApexTextStyles.LabelInteractive.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun ActiveSetRow(
    set: ActiveSet,
    onAdjustWeight: (Double) -> Unit,
    onAdjustReps: (Long) -> Unit,
    onCycleRpe: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (set.completed) ApexSuccessGreen.copy(alpha = 0.12f) else ApexSurfaceContainerHigh)
            .border(
                1.dp,
                if (set.completed) ApexSuccessGreen.copy(alpha = 0.45f) else ApexOutlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = set.setLabel,
            style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = ApexPrimary,
            modifier = Modifier.width(22.dp),
            textAlign = TextAlign.Center
        )

        // Weight stepper
        Row(
            modifier = Modifier.weight(1.25f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MiniStepperButton(icon = Icons.Rounded.Remove, cd = "Diminuir carga") { onAdjustWeight(-2.5) }
            Text(
                text = if (set.weightKg % 1.0 == 0.0) "${set.weightKg.toInt()}" else String.format("%.1f", set.weightKg),
                style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                color = ApexOnSurface,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            MiniStepperButton(icon = Icons.Rounded.Add, cd = "Aumentar carga") { onAdjustWeight(2.5) }
        }

        // Reps stepper
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MiniStepperButton(icon = Icons.Rounded.Remove, cd = "Diminuir reps") { onAdjustReps(-1L) }
            Text(
                text = set.reps.toString(),
                style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = ApexOnSurface,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            MiniStepperButton(icon = Icons.Rounded.Add, cd = "Aumentar reps") { onAdjustReps(1L) }
        }

        // RPE badge
        Box(
            modifier = Modifier
                .width(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurfaceContainerLowest)
                .clickable(onClick = onCycleRpe)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.1f", set.rpe),
                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                color = ApexSecondary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Complete button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (set.completed) ApexSuccessGreen else ApexSurfaceContainerLowest)
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Marcar série concluída",
                tint = if (set.completed) ApexOnPrimary else ApexOnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "Excluir série",
                tint = ApexError.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MiniStepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cd: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(ApexSurfaceContainerLowest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = cd, tint = ApexPrimary, modifier = Modifier.size(14.dp))
    }
}
