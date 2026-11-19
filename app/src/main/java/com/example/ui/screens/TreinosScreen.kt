package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserStatsEntity
import com.example.data.local.WorkoutRoutineEntity
import com.example.ui.components.PulsingCyanDot
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
import com.example.viewmodel.ExerciseLibraryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinosScreen(
    routines: List<WorkoutRoutineEntity>,
    stats: UserStatsEntity,
    showPwaBanner: Boolean,
    installState: String,
    selectedFilter: String,
    exerciseLibrary: List<ExerciseLibraryItem>,
    onDismissPwaBanner: () -> Unit,
    onInstallClick: () -> Unit,
    onSelectFilter: (String) -> Unit,
    onImportClick: () -> Unit,
    onSaveNewRoutine: (String, Int, String, Double, Int) -> Unit,
    onStartWorkout: (String, String) -> Unit,
    onExerciseAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showLibrarySheet by remember { mutableStateOf(false) }
    var showNewRoutineSheet by remember { mutableStateOf(false) }
    var previewRoutine by remember { mutableStateOf<WorkoutRoutineEntity?>(null) }

    val filteredRoutines = remember(routines, selectedFilter) {
        when (selectedFilter) {
            "favs" -> routines.filter { it.isFavorite || it.isRecommended }
            "hypertrophy" -> routines.filter { it.category == "hypertrophy" || it.isRecommended }
            "strength" -> routines.filter { it.category == "strength" || it.letter == "B" || it.letter == "C" }
            else -> routines
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. PWA Install & Offline Cache Banner
        if (showPwaBanner) {
            item(key = "pwa_banner") {
                PwaOfflineBanner(
                    installState = installState,
                    onDismiss = onDismissPwaBanner,
                    onInstall = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInstallClick()
                    }
                )
            }
        }

        // 2. Hero Streak, Level & XP Telemetry Card
        item(key = "hero_streak") {
            StreakHeroCard(stats = stats)
        }

        // 3. Quick Action Triggers (Ergonomic Thumb Grid)
        item(key = "quick_actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTriggerTile(
                    icon = Icons.Rounded.AddCircle,
                    label = "Nova Ficha",
                    highlighted = true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showNewRoutineSheet = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_nova_ficha")
                )
                QuickTriggerTile(
                    icon = Icons.Rounded.Download,
                    label = "Importar",
                    highlighted = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onImportClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_importar")
                )
                QuickTriggerTile(
                    icon = Icons.Rounded.MenuBook,
                    label = "Exercícios",
                    highlighted = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showLibrarySheet = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_exercicios_library")
                )
            }
        }

        // 4. Split Filter Tabs Carousel
        item(key = "split_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipPill(
                    label = "Minhas Fichas (ABC)",
                    selected = selectedFilter == "all",
                    onClick = { onSelectFilter("all") }
                )
                FilterChipPill(
                    label = "Hipertrofia",
                    selected = selectedFilter == "hypertrophy",
                    onClick = { onSelectFilter("hypertrophy") }
                )
                FilterChipPill(
                    label = "Força / RPE",
                    selected = selectedFilter == "strength",
                    onClick = { onSelectFilter("strength") }
                )
                FilterChipPill(
                    label = "Favoritos",
                    selected = selectedFilter == "favs",
                    leadingIcon = Icons.Rounded.Star,
                    onClick = { onSelectFilter("favs") }
                )
            }
        }

        // 5. Workouts List Cards
        items(filteredRoutines, key = { it.id }) { routine ->
            WorkoutRoutineCard(
                routine = routine,
                onStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onStartWorkout(
                        "Treino ${routine.letter} • ${routine.title.substringBefore(",")}",
                        routine.title
                    )
                },
                onPreview = { previewRoutine = routine }
            )
        }

        // 6. Visual Muscle Distribution Snapshot
        item(key = "muscle_distribution") {
            MuscleDistributionCard()
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Exercise Library Modal Bottom Sheet
    if (showLibrarySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLibrarySheet = false },
            sheetState = sheetState,
            containerColor = ApexSurfaceContainer,
            dragHandle = null
        ) {
            ExerciseLibrarySheetContent(
                exercises = exerciseLibrary,
                onClose = { showLibrarySheet = false },
                onAddExercise = { name ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onExerciseAdded(name)
                }
            )
        }
    }

    // Create New Routine Modal Bottom Sheet
    if (showNewRoutineSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNewRoutineSheet = false },
            sheetState = sheetState,
            containerColor = ApexSurfaceContainer,
            dragHandle = null
        ) {
            NewRoutineSheetContent(
                onClose = { showNewRoutineSheet = false },
                onSave = { name, sets, reps, rpe, rest ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSaveNewRoutine(name, sets, reps, rpe, rest)
                    showNewRoutineSheet = false
                }
            )
        }
    }

    // Routine Preview Dialog ("Ver Séries")
    previewRoutine?.let { routine ->
        AlertDialog(
            onDismissRequest = { previewRoutine = null },
            containerColor = ApexSurfaceContainer,
            titleContentColor = ApexOnSurface,
            textContentColor = ApexOnSurfaceVariant,
            title = {
                Text(
                    text = "Grade Detalhada • Treino ${routine.letter}",
                    style = ApexTextStyles.HeadlineSm
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = routine.title,
                        style = ApexTextStyles.LabelInteractive,
                        color = ApexPrimary
                    )
                    Text(
                        text = "• Puxada Alta Pronada — 4×10 (RPE 8.5)\n" +
                            "• Remada Curvada Barra — 4×8 (RPE 9.0)\n" +
                            "• Levantamento Terra — 4×5 @ 140 kg\n" +
                            "• Face Pull na Polia — 4×15\n" +
                            "• Rosca Direta Barra W — 4×10\n" +
                            "• Rosca Martelo Halteres — 4×12",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurface
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val r = routine
                        previewRoutine = null
                        onStartWorkout("Treino ${r.letter} • ${r.title.substringBefore(",")}", r.title)
                    }
                ) {
                    Text("Iniciar Agora", color = ApexPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewRoutine = null }) {
                    Text("Fechar", color = ApexOnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun PwaOfflineBanner(
    installState: String,
    onDismiss: () -> Unit,
    onInstall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainerHigh)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PulsingCyanDot()
                    Text(
                        text = "OFFLINE PRONTO • CACHE LOCAL ATIVO",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = ApexOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "App Instalável",
                        style = ApexTextStyles.HeadlineSm,
                        color = ApexOnSurface
                    )
                    Text(
                        text = "Treine no subsolo sem conexão com zero delay.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant
                    )
                }

                val isInstalled = installState == "installed"
                val isInstalling = installState == "installing"
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isInstalled) ApexSurfaceContainerHighest else ApexPrimary)
                        .clickable(enabled = !isInstalling, onClick = onInstall)
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("pwa_install_cta"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isInstalling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = ApexOnPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (isInstalled) Icons.Rounded.Check else Icons.Rounded.AddToHomeScreen,
                            contentDescription = null,
                            tint = if (isInstalled) ApexOnSurface else ApexOnPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Text(
                        text = when (installState) {
                            "installing" -> "Instalando..."
                            "installed" -> "Instalado"
                            else -> "Instalar"
                        },
                        style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp),
                        color = if (isInstalled) ApexOnSurface else ApexOnPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakHeroCard(stats: UserStatsEntity) {
    val xpFraction = (stats.currentXp.toFloat() / stats.nextLevelXp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val xpPct = (xpFraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SEQUÊNCIA ATIVA",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexPrimary
                    )
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stats.streakDays.toString(),
                        style = ApexTextStyles.DisplayHeroMobile,
                        color = ApexOnSurface
                    )
                    Text(
                        text = "Dias Consecutivos",
                        style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Medium),
                        color = ApexOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHigh)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = ApexPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "PRO ATHLETE",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexOnSurface
                )
            }
        }

        // XP Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NÍVEL ${stats.heroLevel}",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexPrimary
                )
                Text(
                    text = "${ApexViewModel.formatPtBrNumber(stats.currentXp)} / ${ApexViewModel.formatPtBrNumber(stats.nextLevelXp)} XP ($xpPct%)",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexOnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xpFraction)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(ApexSecondaryContainer, ApexPrimaryContainer, ApexPrimary)
                            )
                        )
                )
            }
        }

        // Week Frequency Rings
        val days = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { idx, day ->
                val isChecked = idx < 4
                val isToday = idx == 4
                val isRest = idx == 6
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = day,
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = if (isToday) ApexPrimary else ApexOnSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isChecked -> ApexPrimary
                                    isToday -> ApexPrimaryContainer
                                    else -> ApexSurfaceContainerHigh
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(2.dp, ApexPrimary, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isChecked -> Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "$day concluído",
                                tint = ApexOnPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                            isToday -> Text(
                                text = "HOJE",
                                style = ApexTextStyles.LabelCaps.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp
                                ),
                                color = ApexOnPrimary
                            )
                            isRest -> Icon(
                                imageVector = Icons.Rounded.Hotel,
                                contentDescription = "Descanso",
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            else -> Text(
                                text = "-",
                                style = ApexTextStyles.LabelCaps,
                                color = ApexOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTriggerTile(
    icon: ImageVector,
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) ApexPrimary else ApexSurfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (highlighted) ApexOnPrimary else ApexPrimary,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp),
            color = if (highlighted) ApexOnPrimary else ApexOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) ApexPrimary else ApexSurfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected) ApexOnPrimary else ApexOnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            style = ApexTextStyles.LabelInteractive.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (selected) ApexOnPrimary else ApexOnSurfaceVariant
        )
    }
}

@Composable
private fun WorkoutRoutineCard(
    routine: WorkoutRoutineEntity,
    onStart: () -> Unit,
    onPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (routine.isRecommended) 12.dp else 6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .then(
                if (routine.isRecommended) {
                    Modifier.border(1.dp, ApexPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (routine.isRecommended || routine.statusBadge == "RECÉM-CRIADA")
                                    ApexPrimary.copy(alpha = 0.20f)
                                else ApexSurfaceContainerHigh
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = routine.letter,
                            style = ApexTextStyles.LabelCaps,
                            color = if (routine.isRecommended || routine.statusBadge == "RECÉM-CRIADA")
                                ApexPrimary else ApexOnSurfaceVariant
                        )
                    }
                    Text(
                        text = routine.statusBadge,
                        style = ApexTextStyles.LabelCaps,
                        color = if (routine.isRecommended || routine.statusBadge == "RECÉM-CRIADA")
                            ApexPrimary else ApexOnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = routine.title,
                    style = ApexTextStyles.HeadlineMd,
                    color = ApexOnSurface
                )
                Text(
                    text = routine.subtitle,
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHigh)
                    .clickable(onClick = onPreview),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Detalhes da ficha",
                    tint = ApexOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Telemetry Data Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ApexSurfaceContainerHigh.copy(alpha = 0.65f))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricMiniColumn(
                label = "EXERCÍCIOS",
                value = routine.exerciseCount.toString(),
                unit = if (routine.isRecommended) "itens" else "",
                modifier = Modifier.weight(1f)
            )
            MetricMiniColumn(
                label = "VOLUME",
                value = routine.totalSets.toString(),
                unit = "séries",
                modifier = Modifier.weight(1f)
            )
            MetricMiniColumn(
                label = "DURAÇÃO",
                value = routine.durationMin.toString(),
                unit = "min",
                modifier = Modifier.weight(1f)
            )
        }

        // Micro PR / Progress Highlight
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                val highlightIcon = when (routine.letter) {
                    "B" -> Icons.Rounded.MilitaryTech
                    "C" -> Icons.Rounded.FitnessCenter
                    else -> Icons.Rounded.TrendingUp
                }
                val accent = if (routine.letter == "B") ApexSecondary else ApexPrimary
                Icon(
                    imageVector = highlightIcon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = buildAnnotatedString {
                        append(routine.highlightLabel)
                        append(" ")
                        withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
                            append(routine.highlightValue)
                        }
                    },
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = routine.rightTag,
                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                color = ApexOnSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (routine.isRecommended) ApexSurfaceContainerHighest else Color.Transparent)
                    .padding(horizontal = if (routine.isRecommended) 7.dp else 0.dp, vertical = 2.dp)
            )
        }

        // Action CTAs
        if (routine.letter == "B") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerHigh)
                        .clickable(onClick = onPreview),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = ApexOnSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ver Séries",
                        style = ApexTextStyles.LabelInteractive,
                        color = ApexOnSurface
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerHigh)
                        .clickable(onClick = onStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Replay,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Repetir",
                        style = ApexTextStyles.LabelInteractive,
                        color = ApexPrimary
                    )
                }
            }
        } else {
            val isPrimaryCta = routine.isRecommended || routine.statusBadge == "RECÉM-CRIADA"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(if (isPrimaryCta) 10.dp else 0.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPrimaryCta) ApexPrimary else ApexSurfaceContainerHigh)
                    .clickable(onClick = onStart)
                    .testTag("start_routine_${routine.letter}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isPrimaryCta) Icons.Rounded.PlayArrow else Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = if (isPrimaryCta) ApexOnPrimary else ApexOnSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPrimaryCta) routine.ctaLabel.uppercase() else routine.ctaLabel,
                    style = ApexTextStyles.LabelInteractive.copy(
                        fontWeight = if (isPrimaryCta) FontWeight.ExtraBold else FontWeight.SemiBold
                    ),
                    color = if (isPrimaryCta) ApexOnPrimary else ApexOnSurface
                )
            }
        }
    }
}

@Composable
private fun MetricMiniColumn(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
            color = ApexOnSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = value,
                style = ApexTextStyles.MetricMd,
                color = ApexOnSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MuscleDistributionCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(
                text = "Equilíbrio Muscular Semanal",
                style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                color = ApexOnSurface
            )
            Text(
                text = "VOLUME ÓTIMO",
                style = ApexTextStyles.LabelCaps,
                color = ApexPrimary
            )
        }
        MuscleProgressBarRow("Peitoral / Tríceps", "100% Meta", 1.0f, ApexPrimary, ApexOnSurface)
        MuscleProgressBarRow("Dorsal / Bíceps", "92% Meta", 0.92f, ApexPrimaryContainer, ApexOnSurface)
        MuscleProgressBarRow(
            "Membros Inferiores (Legs)",
            "60% Meta (Treino C Pendente)",
            0.60f,
            ApexSecondary,
            ApexOnSurfaceVariant
        )
    }
}

@Composable
private fun MuscleProgressBarRow(
    label: String,
    rightText: String,
    progress: Float,
    barColor: Color,
    rightColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
            Text(text = rightText, style = ApexTextStyles.LabelCaps, color = rightColor)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(ApexSurfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun ExerciseLibrarySheetContent(
    exercises: List<ExerciseLibraryItem>,
    onClose: () -> Unit,
    onAddExercise: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("all") }
    val addedMap = remember { mutableStateMapOf<String, Boolean>() }

    val muscleGroups = listOf(
        "all" to "Todos",
        "peitoral" to "Peitoral",
        "dorsais" to "Dorsais",
        "quadriceps" to "Quadríceps",
        "ombros" to "Ombros",
        "biceps" to "Bíceps",
        "triceps" to "Tríceps",
        "core" to "Core"
    )

    val visibleItems = remember(exercises, searchQuery, selectedMuscle) {
        exercises.filter { item ->
            val matchGroup = selectedMuscle == "all" || item.muscleKey == selectedMuscle
            val matchQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)
            matchGroup && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(48.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(ApexOutline)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = ApexPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Banco de Exercícios",
                    style = ApexTextStyles.HeadlineMd,
                    color = ApexOnSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHigh)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = ApexOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Buscar exercício por nome ou máquina...",
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = ApexOnSurfaceVariant
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ApexSurfaceContainerHigh,
                unfocusedContainerColor = ApexSurfaceContainerHigh,
                focusedBorderColor = ApexPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = ApexOnSurface,
                unfocusedTextColor = ApexOnSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            muscleGroups.forEach { (key, label) ->
                val isSelected = selectedMuscle == key
                Text(
                    text = label,
                    style = ApexTextStyles.LabelCaps,
                    color = if (isSelected) ApexOnPrimary else ApexOnSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) ApexPrimary else ApexSurfaceContainerHigh)
                        .clickable { selectedMuscle = key }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleItems.forEach { item ->
                val added = addedMap[item.name] == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerHigh)
                        .padding(10.dp),
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
                                .background(ApexSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                tint = if (item.accentSecondary) ApexSecondary else ApexPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = item.name,
                                style = ApexTextStyles.LabelInteractive,
                                color = ApexOnSurface
                            )
                            Text(
                                text = item.subtitle,
                                style = ApexTextStyles.BodySm,
                                color = ApexOnSurfaceVariant
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (added) ApexPrimary else ApexSurfaceContainerLowest)
                            .clickable {
                                addedMap[item.name] = true
                                onAddExercise(item.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (added) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = "Adicionar ${item.name}",
                            tint = if (added) ApexOnPrimary else ApexPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NewRoutineSheetContent(
    onClose: () -> Unit,
    onSave: (String, Int, String, Double, Int) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    var targetSets by remember { mutableIntStateOf(4) }
    val repRanges = listOf("6-8", "8-12", "12-15", "15-20")
    var repIndex by remember { mutableIntStateOf(1) }
    var targetRpe by remember { mutableDoubleStateOf(8.5) }
    var targetRest by remember { mutableIntStateOf(90) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                Icon(
                    imageVector = Icons.Rounded.AddTask,
                    contentDescription = null,
                    tint = ApexPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Criar Nova Ficha",
                    style = ApexTextStyles.HeadlineMd,
                    color = ApexOnSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHigh)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = ApexOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "NOME DA DIVISÃO / FICHA",
                style = ApexTextStyles.LabelCaps,
                color = ApexOnSurfaceVariant
            )
            OutlinedTextField(
                value = routineName,
                onValueChange = { routineName = it },
                placeholder = {
                    Text(
                        "Ex: Treino D - Ombros e Braços",
                        style = ApexTextStyles.BodyMd,
                        color = ApexOnSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ApexSurfaceContainerHigh,
                    unfocusedContainerColor = ApexSurfaceContainerHigh,
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_routine_name")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StepperTile(
                label = "SÉRIES / EXERCÍCIO",
                valueText = targetSets.toString(),
                onDec = { targetSets = (targetSets - 1).coerceAtLeast(1) },
                onInc = { targetSets = (targetSets + 1).coerceAtMost(10) },
                modifier = Modifier.weight(1f)
            )
            StepperTile(
                label = "FAIXA DE REPETIÇÕES",
                valueText = repRanges[repIndex],
                onDec = { repIndex = (repIndex - 1).coerceAtLeast(0) },
                onInc = { repIndex = (repIndex + 1).coerceAtMost(repRanges.lastIndex) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StepperTile(
                label = "META DE RPE",
                valueText = String.format("%.1f", targetRpe),
                onDec = { targetRpe = (targetRpe - 0.5).coerceAtLeast(6.0) },
                onInc = { targetRpe = (targetRpe + 0.5).coerceAtMost(10.0) },
                modifier = Modifier.weight(1f)
            )
            StepperTile(
                label = "DESCANSO PADRÃO",
                valueText = "${targetRest}s",
                onDec = { targetRest = (targetRest - 15).coerceAtLeast(30) },
                onInc = { targetRest = (targetRest + 15).coerceAtMost(300) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ApexPrimary)
                .clickable {
                    onSave(routineName, targetSets, repRanges[repIndex], targetRpe, targetRest)
                }
                .testTag("btn_save_routine"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = null,
                tint = ApexOnPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SALVAR E ADICIONAR EXERCÍCIOS",
                style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.Bold),
                color = ApexOnPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StepperTile(
    label: String,
    valueText: String,
    onDec: () -> Unit,
    onInc: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurfaceContainerHigh)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
            color = ApexOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ApexSurfaceContainerHighest)
                    .clickable(onClick = onDec),
                contentAlignment = Alignment.Center
            ) {
                Text("-", style = ApexTextStyles.MetricMd, color = ApexOnSurface)
            }
            Text(
                text = valueText,
                style = ApexTextStyles.MetricMd,
                color = ApexPrimary
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ApexSurfaceContainerHighest)
                    .clickable(onClick = onInc),
                contentAlignment = Alignment.Center
            ) {
                Text("+", style = ApexTextStyles.MetricMd, color = ApexOnSurface)
            }
        }
    }
}
