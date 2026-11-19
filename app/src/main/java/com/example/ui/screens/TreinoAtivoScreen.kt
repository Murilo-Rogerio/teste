package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAlarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.local.ActiveSetEntity
import com.example.data.local.UserStatsEntity
import com.example.ui.components.PulsingCyanDot
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutline
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondary
import com.example.ui.theme.ApexSecondaryContainer
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel

private const val IMG_BENCH_BARBELL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCdTL0SE_cGV4YuxXFfyXahUdhTecKm6cI-XUaTg351Q4giWimAjVaYRXwa8k2V1X_zXwr960wp5o0rumyXfQdlRyO1hhKvazSbjrHSbx1ZBBF02HFyN_6uSBZvZe6XtjI9_E13Ui89eY1hs3tEV8cncda8DEyz80JZxq9NEEFy0s-AwAssjDMrtrUy51Vsr5zznQgRN8dPZKKFfuyUhWskoPe-kK94nWC8icP_z2k"
private const val IMG_DUMBBELL_OHP =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAPYmmphNBZqX07Gp-Z4cO2Fzfc8Ymny-S2qFPgkPo3kEH4aribugZM62u9iEzlPBh-XL5HrRlvH-5uZbc-kJDgTQsxVL34F2C473T0m55pv60U9KRnWkPW_iD3j03Nd_AA8C9x4v2FUk2k3bAarJXRx2vk3qtkoRUOq8d1fNjC8Q82wgtZSCq-pWgSSFiWweH2PyCge_lZwxSuPQTKhgOo92SHwpaLo97-QYFRJ5w"
private const val IMG_INCLINE_FLY =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBoHX6atld4jJQIrx3ZSoSJfLyK6d4ARfNDcq_0k4DyveSDL8RvUsFyoJMH9_zdDRHO0wka3LlDzfrGHKzdtdq5MrX2EAfZs65TjsFzzw-CrmQHSXDkEavBTsVManeeEC-goQ5LULVEc3O38QHhUNaKANo04iBaHtybzgCYPfDYXd22Aa4uCvXFgn0bSxbbN0j8sfrXbpPPRpdsVL3ttlLS9raRW5bm0yqaXUEiiVk"

private data class QueuedExercise(
    val number: Int,
    val setsText: String,
    val title: String,
    val target: String,
    val tip: String,
    val imageUrl: String?,
    val useSecondaryAccent: Boolean = false
)

@Composable
fun TreinoAtivoScreen(
    stats: UserStatsEntity,
    sets: List<ActiveSetEntity>,
    elapsedSeconds: Int,
    totalVolumeKg: Int,
    restRemainingSeconds: Int,
    restTotalPresetSeconds: Int,
    soundEnabled: Boolean,
    showPrBanner: Boolean,
    onSelectRestPreset: (Int) -> Unit,
    onAdd30Seconds: () -> Unit,
    onSkipRest: () -> Unit,
    onToggleSound: () -> Unit,
    onDismissPrBanner: () -> Unit,
    onAdjustWeight: (ActiveSetEntity, Int) -> Unit,
    onAdjustReps: (ActiveSetEntity, Int) -> Unit,
    onCycleRpe: (ActiveSetEntity) -> Unit,
    onToggleSetCompleted: (ActiveSetEntity) -> Unit,
    onAddSet: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showFinishDialog by remember { mutableStateOf(false) }
    var showBiomechanicsDialog by remember { mutableStateOf(false) }
    var notesText by remember(stats.exerciseNotes) { mutableStateOf(stats.exerciseNotes) }
    val expandedQueue = remember { mutableStateMapOf<Int, Boolean>() }

    val queuedExercises = remember {
        listOf(
            QueuedExercise(
                number = 2,
                setsText = "4 Séries",
                title = "Desenvolvimento Militar com Halteres",
                target = "Alvo: 28kg x 10 reps",
                tip = "Mantenha cotovelos 30° à frente do plano escapular e abdômen contraído.",
                imageUrl = IMG_DUMBBELL_OHP
            ),
            QueuedExercise(
                number = 3,
                setsText = "3 Séries",
                title = "Crucifixo Inclinado com Halteres",
                target = "Alvo: 20kg x 12 reps",
                tip = "Banco a 30°, controle excêntrico de 3s com alongamento total peitoral.",
                imageUrl = IMG_INCLINE_FLY
            ),
            QueuedExercise(
                number = 4,
                setsText = "4 Séries",
                title = "Elevação Lateral com Cabos",
                target = "Alvo: 12.5kg x 15 reps",
                tip = "Polia na altura do punho para tensão constante desde o início da abdução.",
                imageUrl = null
            ),
            QueuedExercise(
                number = 5,
                setsText = "3 Séries",
                title = "Tríceps Corda no Pulley",
                target = "Alvo: 35kg x 12 reps",
                tip = "Afaste as pontas da corda no final da extensão mantendo úmeros fixos.",
                imageUrl = null,
                useSecondaryAccent = true
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP WORKOUT TELEMETRY HUD
        item(key = "telemetry_hud") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainerHigh.copy(alpha = 0.85f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PulsingCyanDot()
                            Text(
                                text = "SESSÃO ATIVA",
                                style = ApexTextStyles.LabelCaps,
                                color = ApexPrimary
                            )
                        }
                        Text(
                            text = stats.activeWorkoutName,
                            style = ApexTextStyles.HeadlineSm,
                            color = ApexOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stats.activeWorkoutSubtitle,
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApexSurfaceContainerHighest)
                            .clickable { showFinishDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("btn_encerrar_top"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.StopCircle,
                            contentDescription = "Encerrar",
                            tint = ApexError,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Encerrar",
                            style = ApexTextStyles.LabelInteractive,
                            color = ApexError
                        )
                    }
                }

                // Telemetry Badges Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApexSurfaceContainerLowest.copy(alpha = 0.78f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ApexPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = ApexPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "TEMPO DECORRIDO",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                                color = ApexOnSurfaceVariant
                            )
                            Text(
                                text = ApexViewModel.formatTimer(elapsedSeconds),
                                style = ApexTextStyles.MetricMd,
                                color = ApexPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApexSurfaceContainerLowest.copy(alpha = 0.78f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ApexSecondaryContainer.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                tint = ApexSecondary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "VOLUME TOTAL",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                                color = ApexOnSurfaceVariant
                            )
                            Text(
                                text = "${ApexViewModel.formatPtBrNumber(totalVolumeKg)} kg",
                                style = ApexTextStyles.MetricMd,
                                color = ApexOnSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. SMART REST TIMER WIDGET (CRONÔMETRO INTELIGENTE)
        item(key = "rest_timer") {
            SmartRestTimerCard(
                remainingSeconds = restRemainingSeconds,
                totalPresetSeconds = restTotalPresetSeconds,
                soundEnabled = soundEnabled,
                onSelectPreset = onSelectRestPreset,
                onAdd30Seconds = onAdd30Seconds,
                onSkip = onSkipRest,
                onToggleSound = onToggleSound
            )
        }

        // 3. PR CELEBRATION BANNER
        if (showPrBanner) {
            item(key = "pr_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ApexSecondaryContainer, ApexSurfaceContainerHigh, ApexSurfaceContainer)
                            )
                        )
                        .padding(12.dp)
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
                                    .clip(CircleShape)
                                    .background(ApexPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = ApexPrimary,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "NOVO RECORDE (PR)!",
                                        style = ApexTextStyles.LabelCaps,
                                        color = ApexPrimary
                                    )
                                    Text(
                                        text = "+2.5kg",
                                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                        color = ApexPrimary,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(ApexPrimary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "Supino Reto: Carga histórica superada hoje!",
                                    style = ApexTextStyles.BodySm,
                                    color = ApexOnSurface
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismissPrBanner,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_close),
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. CURRENT ACTIVE EXERCISE: SUPINO RETO COM BARRA
        item(key = "active_exercise_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainerLow)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCÍCIO 1 DE 5",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showBiomechanicsDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ApexOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BIOMECÂNICA",
                            style = ApexTextStyles.LabelCaps,
                            color = ApexOnSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Text(
                            text = "Supino Reto com Barra",
                            style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                            color = ApexOnSurface
                        )
                        Text(
                            text = "Barbell Flat Bench Press • Peitoral Maior",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                    }
                    SubcomposeAsyncImage(
                        model = IMG_BENCH_BARBELL,
                        contentDescription = "Supino Reto com Barra",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        error = {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ApexSurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FitnessCenter,
                                    contentDescription = null,
                                    tint = ApexPrimary
                                )
                            }
                        }
                    )
                }

                // AI Suggestion & History
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApexSurfaceContainerHigh.copy(alpha = 0.85f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = ApexPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "IA Sugestão: 84kg x 8 reps (RPE 8.5)",
                            style = ApexTextStyles.LabelInteractive,
                            color = ApexPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = ApexOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Último treino: 82kg x 8 reps (100% batido)",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                    }
                }

                // DYNAMIC SETS TABLE
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SET", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant, modifier = Modifier.weight(1.6f), textAlign = TextAlign.Center)
                        Text("KG", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant, modifier = Modifier.weight(3.2f), textAlign = TextAlign.Center)
                        Text("REPS", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant, modifier = Modifier.weight(3.2f), textAlign = TextAlign.Center)
                        Text("RPE", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                        Text("FEITO", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
                    }

                    sets.forEach { setItem ->
                        if (setItem.isActive) {
                            ActiveSetRow(
                                set = setItem,
                                onDecWeight = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAdjustWeight(setItem, -2)
                                },
                                onIncWeight = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAdjustWeight(setItem, 2)
                                },
                                onDecReps = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAdjustReps(setItem, -1)
                                },
                                onIncReps = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onAdjustReps(setItem, 1)
                                },
                                onCycleRpe = { onCycleRpe(setItem) },
                                onToggleDone = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleSetCompleted(setItem)
                                }
                            )
                        } else {
                            StandardSetRow(
                                set = setItem,
                                onToggleDone = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleSetCompleted(setItem)
                                }
                            )
                        }
                    }
                }

                // Quick Micro Actions & Notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexSurfaceContainerHigh.copy(alpha = 0.85f))
                                .clickable(onClick = onAddSet)
                                .testTag("btn_add_set"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = ApexPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Adicionar Série",
                                style = ApexTextStyles.LabelInteractive,
                                color = ApexPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexSurfaceContainerHigh.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Reordenar",
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            notesText = it
                            onUpdateNotes(it)
                        },
                        placeholder = {
                            Text(
                                "Anotação técnica (ex: pino 3, pegada aberta)...",
                                style = ApexTextStyles.BodySm,
                                color = ApexOnSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = null,
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        textStyle = ApexTextStyles.BodySm.copy(color = ApexOnSurface),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ApexSurfaceContainerLowest.copy(alpha = 0.85f),
                            unfocusedContainerColor = ApexSurfaceContainerLowest.copy(alpha = 0.85f),
                            focusedBorderColor = ApexPrimary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 5. UPCOMING EXERCISES ACCORDION QUEUE
        item(key = "upcoming_queue") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximos no Treino",
                        style = ApexTextStyles.HeadlineSm,
                        color = ApexOnSurface
                    )
                    Text(
                        text = "4 EXERCÍCIOS RESTANTES",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexOnSurfaceVariant
                    )
                }

                queuedExercises.forEach { ex ->
                    val isExpanded = expandedQueue[ex.number] == true
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ApexSurfaceContainerLow)
                            .clickable { expandedQueue[ex.number] = !isExpanded }
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                if (ex.imageUrl != null) {
                                    SubcomposeAsyncImage(
                                        model = ex.imageUrl,
                                        contentDescription = ex.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        error = {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(ApexSurfaceContainerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = ApexPrimary
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ApexSurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (ex.useSecondaryAccent) Icons.Rounded.LinearScale else Icons.Rounded.FitnessCenter,
                                            contentDescription = null,
                                            tint = if (ex.useSecondaryAccent) ApexSecondary else ApexPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "EXERCÍCIO ${ex.number}",
                                            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                            color = ApexPrimary
                                        )
                                        Text(
                                            text = "• ${ex.setsText}",
                                            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                            color = ApexOnSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = ex.title,
                                        style = ApexTextStyles.LabelInteractive.copy(fontSize = 15.sp),
                                        color = ApexOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ex.target,
                                        style = ApexTextStyles.BodySm,
                                        color = ApexOnSurfaceVariant
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ApexSurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Expandir",
                                    tint = ApexOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Text(
                                text = "Dica técnica: ${ex.tip}",
                                style = ApexTextStyles.BodySm,
                                color = ApexPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ApexSurfaceContainerHigh.copy(alpha = 0.65f))
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. ACTION ZONE: FINISH WORKOUT TRIGGER CTA
        item(key = "finish_cta") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(14.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ApexPrimaryContainer, ApexPrimary, ApexSecondaryContainer)
                            )
                        )
                        .clickable { showFinishDialog = true }
                        .testTag("btn_finish_workout"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = ApexSurfaceContainerLowest,
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FINALIZAR E SALVAR TREINO",
                        style = ApexTextStyles.HeadlineSm.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = ApexSurfaceContainerLowest
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Armazenamento local persistido automaticamente",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexOnSurfaceVariant
                    )
                }
            }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = ApexSurfaceContainer,
            titleContentColor = ApexOnSurface,
            textContentColor = ApexOnSurfaceVariant,
            title = { Text("Concluir Sessão de Treino?", style = ApexTextStyles.HeadlineSm) },
            text = {
                Text(
                    "Deseja concluir ${stats.activeWorkoutName} e registrar ${ApexViewModel.formatPtBrNumber(totalVolumeKg)} kg de volume total e +180 XP no seu histórico offline?",
                    style = ApexTextStyles.BodyMd
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                        onFinishWorkout()
                    }
                ) {
                    Text("Finalizar e Salvar", color = ApexPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Continuar Treinando", color = ApexOnSurfaceVariant)
                }
            }
        )
    }

    if (showBiomechanicsDialog) {
        AlertDialog(
            onDismissRequest = { showBiomechanicsDialog = false },
            containerColor = ApexSurfaceContainer,
            titleContentColor = ApexOnSurface,
            textContentColor = ApexOnSurfaceVariant,
            title = { Text("Biomecânica • Supino Reto", style = ApexTextStyles.HeadlineSm) },
            text = {
                Text(
                    "• Retração e depressão escapular constantes.\n" +
                        "• Ângulo dos cotovelos entre 45° e 60° em relação ao tronco.\n" +
                        "• Leg drive firme no solo sem retirar glúteos do banco.\n" +
                        "• Trajetória leve em 'J' tocando na linha inferior do esterno.",
                    style = ApexTextStyles.BodyMd
                )
            },
            confirmButton = {
                TextButton(onClick = { showBiomechanicsDialog = false }) {
                    Text("Entendido", color = ApexPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SmartRestTimerCard(
    remainingSeconds: Int,
    totalPresetSeconds: Int,
    soundEnabled: Boolean,
    onSelectPreset: (Int) -> Unit,
    onAdd30Seconds: () -> Unit,
    onSkip: () -> Unit,
    onToggleSound: () -> Unit
) {
    val progress = (remainingSeconds.toFloat() / totalPresetSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val presets = listOf(60, 90, 120, 180)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassTop,
                    contentDescription = null,
                    tint = ApexPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "DESCANSO BIOMECÂNICO",
                    style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.Bold),
                    color = ApexOnSurface
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ApexSurfaceContainerHigh)
                        .clickable(onClick = onToggleSound),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = stringResource(R.string.cd_toggle_sound),
                        tint = if (soundEnabled) ApexPrimary else ApexOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "PULAR",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexOnSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ApexSurfaceContainerHigh)
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("btn_skip_rest")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Timer Ring
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val inset = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    drawArc(
                        color = ApexSurfaceContainerHighest,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ApexPrimaryContainer,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ApexViewModel.formatTimer(remainingSeconds),
                        style = ApexTextStyles.MetricMd,
                        color = ApexPrimary
                    )
                    Text(
                        text = "REST",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                        color = ApexOnSurfaceVariant
                    )
                }
            }

            // Presets + +30s
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { sec ->
                        val isSelected = totalPresetSeconds == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                                .clickable { onSelectPreset(sec) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sec}s",
                                style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp),
                                color = if (isSelected) ApexOnPrimary else ApexOnSurface
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ApexSurfaceContainerHighest)
                        .clickable(onClick = onAdd30Seconds)
                        .testTag("btn_add_30s"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddAlarm,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+30 Segundos",
                        style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp),
                        color = ApexPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSetRow(
    set: ActiveSetEntity,
    onDecWeight: () -> Unit,
    onIncWeight: () -> Unit,
    onDecReps: () -> Unit,
    onIncReps: () -> Unit,
    onCycleRpe: () -> Unit,
    onToggleDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurfaceContainerHighest)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Cyan Indicator + Set number
        Row(
            modifier = Modifier.weight(1.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(ApexPrimary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = set.setLabel,
                    style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                    color = ApexPrimary
                )
                Text(
                    text = "ATIVA",
                    style = ApexTextStyles.LabelCaps.copy(fontSize = 8.sp),
                    color = ApexPrimary
                )
            }
        }

        // Weight Stepper
        Row(
            modifier = Modifier
                .weight(3.2f)
                .padding(horizontal = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurfaceContainerLow)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onDecWeight)
                    .testTag("weight_dec"),
                contentAlignment = Alignment.Center
            ) {
                Text("-", style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
            }
            Text(
                text = set.weightKg.toString(),
                style = ApexTextStyles.MetricMd.copy(fontSize = 17.sp),
                color = ApexOnSurface
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onIncWeight)
                    .testTag("weight_inc"),
                contentAlignment = Alignment.Center
            ) {
                Text("+", style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
            }
        }

        // Reps Stepper
        Row(
            modifier = Modifier
                .weight(3.2f)
                .padding(horizontal = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurfaceContainerLow)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onDecReps)
                    .testTag("reps_dec"),
                contentAlignment = Alignment.Center
            ) {
                Text("-", style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
            }
            Text(
                text = set.reps.toString(),
                style = ApexTextStyles.MetricMd.copy(fontSize = 17.sp),
                color = ApexOnSurface
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onIncReps)
                    .testTag("reps_inc"),
                contentAlignment = Alignment.Center
            ) {
                Text("+", style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
            }
        }

        // RPE Pill
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.1f", set.rpe),
                style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp),
                color = ApexPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ApexSurfaceContainerLow)
                    .clickable(onClick = onCycleRpe)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        // Complete Set Checkbox
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (set.isCompleted) ApexPrimaryContainer else ApexSurfaceContainer)
                    .clickable(onClick = onToggleDone)
                    .testTag("complete_active_set_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (set.isCompleted) Icons.Rounded.Check else Icons.Rounded.Done,
                    contentDescription = stringResource(R.string.cd_complete_set),
                    tint = if (set.isCompleted) ApexOnPrimary else ApexPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun StandardSetRow(
    set: ActiveSetEntity,
    onToggleDone: () -> Unit
) {
    val rowAlpha = if (set.isCompleted) 0.78f else 0.58f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (set.isCompleted) ApexSurfaceContainerHigh.copy(alpha = 0.55f)
                else ApexSurfaceContainer.copy(alpha = 0.45f)
            )
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = set.setLabel,
            style = ApexTextStyles.LabelCaps,
            color = if (set.setLabel == "2") ApexPrimary else ApexOnSurfaceVariant,
            modifier = Modifier.weight(1.6f),
            textAlign = TextAlign.Center
        )
        Text(
            text = set.weightKg.toString(),
            style = ApexTextStyles.MetricMd.copy(fontSize = 18.sp),
            color = if (set.isCompleted) ApexOnSurface else ApexOnSurfaceVariant,
            modifier = Modifier.weight(3.2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = set.repsDisplay,
            style = ApexTextStyles.MetricMd.copy(fontSize = 18.sp),
            color = if (set.isCompleted) ApexOnSurface else ApexOnSurfaceVariant,
            modifier = Modifier.weight(3.2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = String.format("%.1f", set.rpe),
            style = ApexTextStyles.BodySm,
            color = ApexOnSurfaceVariant,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (set.isCompleted) ApexPrimaryContainer else ApexSurfaceContainer)
                    .clickable(onClick = onToggleDone),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (set.isCompleted) Icons.Rounded.Check else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Série ${set.setLabel}",
                    tint = if (set.isCompleted) ApexOnPrimary else ApexOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
