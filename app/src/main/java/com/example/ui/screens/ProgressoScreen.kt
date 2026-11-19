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
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BodyMeasurementLog
import com.example.data.UserProfile
import com.example.data.WorkoutLog
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
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressoScreen(
    profile: UserProfile,
    logs: List<WorkoutLog>,
    weightLogs: List<BodyMeasurementLog>,
    onRecordBodyMeasurement: (Double, Double, Double, String) -> Unit,
    onBuildExportPayload: (String) -> String,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var show1RmSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    val totalVolumeKg = remember(logs) { logs.sumOf { it.tonnageKg } }
    val realPrs = remember(logs) {
        logs.filter { it.bestExercise.isNotBlank() && it.bestWeightKg > 0.0 }
            .groupBy { it.bestExercise }
            .mapValues { (_, list) -> list.maxBy { it.bestWeightKg } }
            .values
            .sortedByDescending { it.bestWeightKg }
            .take(4)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Real Executive Bento Grid
        item(key = "bento_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoMetricCard(
                        label = "TREINOS CONCLUÍDOS",
                        value = logs.size.toString(),
                        subtitle = "Nível ${profile.level} • ${profile.streakDays}D sequência",
                        accent = ApexPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricCard(
                        label = "VOLUME ACUMULADO",
                        value = "${ApexViewModel.formatPtBrLong(totalVolumeKg)} kg",
                        subtitle = "${logs.sumOf { it.completedSets }} séries válidas",
                        accent = ApexSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoMetricCard(
                        label = "PESO ATUAL",
                        value = "${String.format("%.1f", profile.weightKg)} kg",
                        subtitle = if (profile.waistCm > 0) "Cintura ${String.format("%.1f", profile.waistCm)} cm" else "Altura ${profile.heightCm.toInt()} cm",
                        accent = ApexPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricCard(
                        label = "RECORDES PESSOAIS",
                        value = realPrs.size.toString(),
                        subtitle = "Calculados dos seus treinos",
                        accent = ApexGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Real Overload Curve (Canvas)
        item(key = "overload_chart") {
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
                    Column {
                        Text("CURVA DE SOBRECARGA REAL", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                        Text("Evolução de Tonelagem", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    Icon(Icons.Rounded.ShowChart, contentDescription = null, tint = ApexPrimary)
                }

                val chartPoints = remember(logs) { logs.take(10).reversed().map { it.tonnageKg.toFloat() } }
                if (chartPoints.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerLowest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Conclua pelo menos 2 treinos para visualizar sua curva real de progressão de volume.",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerLowest)
                            .padding(14.dp)
                    ) {
                        val maxVal = (chartPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
                        val minVal = (chartPoints.minOrNull() ?: 0f)
                        val span = (maxVal - minVal).coerceAtLeast(1f)
                        val stepX = size.width / (chartPoints.size - 1).coerceAtLeast(1)

                        val linePath = Path()
                        val fillPath = Path()
                        chartPoints.forEachIndexed { idx, value ->
                            val x = idx * stepX
                            val y = size.height - (((value - minVal) / span) * (size.height * 0.78f)) - (size.height * 0.11f)
                            if (idx == 0) {
                                linePath.moveTo(x, y)
                                fillPath.moveTo(x, size.height)
                                fillPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            if (idx == chartPoints.lastIndex) {
                                fillPath.lineTo(x, size.height)
                                fillPath.close()
                            }
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(ApexPrimaryContainer.copy(alpha = 0.35f), ApexPrimaryContainer.copy(alpha = 0.0f))
                            )
                        )
                        drawPath(
                            path = linePath,
                            color = ApexPrimary,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        chartPoints.forEachIndexed { idx, value ->
                            val x = idx * stepX
                            val y = size.height - (((value - minVal) / span) * (size.height * 0.78f)) - (size.height * 0.11f)
                            drawCircle(color = ApexPrimary, radius = 5.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }
        }

        // 3. Body Measurements & Weight History
        item(key = "body_measurements_section") {
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.MonitorWeight, contentDescription = null, tint = ApexPrimary)
                        Column {
                            Text("HISTÓRICO BIOMÉTRICO", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                            Text("Peso & Medidas", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                        }
                    }
                    Button(
                        onClick = { showLogWeightDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_weight_entry")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp))
                    }
                }

                if (weightLogs.isEmpty()) {
                    Text(
                        text = "Nenhuma medição adicional registrada ainda.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant
                    )
                } else {
                    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
                    weightLogs.take(5).forEach { entry ->
                        val dateStr = entry.createdAt?.toDate()?.let { fmt.format(it) } ?: "Hoje"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexSurfaceContainerLowest)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${String.format("%.1f", entry.weightKg)} kg" +
                                        (if (entry.waistCm > 0) " • Cintura ${String.format("%.1f", entry.waistCm)}cm" else "") +
                                        (if (entry.bodyFatPercent > 0) " • ${String.format("%.1f", entry.bodyFatPercent)}% BF" else ""),
                                    style = ApexTextStyles.LabelInteractive,
                                    color = ApexOnSurface
                                )
                                if (entry.note.isNotBlank()) {
                                    Text(entry.note, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                                }
                            }
                            Text(dateStr, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimary)
                        }
                    }
                }
            }
        }

        // 4. Real Personal Records + 1RM Calculator CTA
        item(key = "prs_section") {
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = ApexGold)
                        Text("Recordes Pessoais (PRs)", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    TextButton(onClick = { show1RmSheet = true }) {
                        Icon(Icons.Rounded.Calculate, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calc 1RM", color = ApexPrimary, style = ApexTextStyles.LabelInteractive)
                    }
                }

                if (realPrs.isEmpty()) {
                    Text(
                        text = "Seus recordes pessoais aparecerão automaticamente conforme você finalizar seus treinos reais.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant
                    )
                } else {
                    realPrs.forEach { pr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ApexSurfaceContainerHigh)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pr.bestExercise, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                                Text("${pr.bestReps} reps registradas", style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                            }
                            Text(
                                text = "${String.format("%.1f", pr.bestWeightKg)} kg",
                                style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Black),
                                color = ApexGold
                            )
                        }
                    }
                }
            }
        }

        // 5. Real Workout Logs History
        item(key = "logs_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LOGS DE TREINOS RECENTES", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                TextButton(onClick = { showExportSheet = true }) {
                    Icon(Icons.Rounded.DownloadForOffline, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exportar", color = ApexPrimary, style = ApexTextStyles.LabelInteractive)
                }
            }
        }

        if (logs.isEmpty()) {
            item(key = "empty_logs") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ApexSurfaceContainer)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum treino finalizado ainda. Inicie uma sessão na aba Treinos ou Ativo.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ApexSurfaceContainer)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ApexPrimaryContainer.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(log.title, style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                            Text(log.metaLine, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${ApexViewModel.formatPtBrLong(log.tonnageKg)} kg",
                            style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold),
                            color = ApexPrimary
                        )
                        Text("${log.completedSets} séries", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                    }
                }
            }
        }
    }

    // Dialog: Record Body Weight / Waist
    if (showLogWeightDialog) {
        var wText by remember { mutableStateOf(String.format("%.1f", profile.weightKg)) }
        var waistText by remember { mutableStateOf(if (profile.waistCm > 0) String.format("%.1f", profile.waistCm) else "") }
        var bfText by remember { mutableStateOf(if (profile.bodyFatPercent > 0) String.format("%.1f", profile.bodyFatPercent) else "") }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            containerColor = ApexSurfaceContainer,
            titleContentColor = ApexOnSurface,
            title = { Text("Registrar Peso e Medidas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wText,
                        onValueChange = { wText = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = waistText,
                        onValueChange = { waistText = it },
                        label = { Text("Cintura (cm) — opcional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bfText,
                        onValueChange = { bfText = it },
                        label = { Text("% Gordura — opcional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it.take(140) },
                        label = { Text("Observação (ex: em jejum)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = wText.replace(",", ".").toDoubleOrNull()
                        if (w != null && w in 30.0..300.0) {
                            onRecordBodyMeasurement(
                                w,
                                waistText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                bfText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                noteText
                            )
                            showLogWeightDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showLogWeightDialog = false }) { Text("Cancelar", color = ApexOnSurfaceVariant) }
            }
        )
    }

    // Bottom Sheet: 1RM Scientific Calculator (Epley & Brzycki)
    if (show1RmSheet) {
        var loadText by remember { mutableStateOf("80") }
        var repsText by remember { mutableStateOf("8") }
        val load = loadText.replace(",", ".").toDoubleOrNull() ?: 0.0
        val reps = repsText.toIntOrNull()?.coerceIn(1, 30) ?: 1
        val epley = if (reps == 1) load else load * (1.0 + (reps / 30.0))
        val brzycki = if (reps == 1) load else load * (36.0 / (37.0 - reps.coerceAtMost(36)))

        ModalBottomSheet(
            onDismissRequest = { show1RmSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ApexSurfaceContainerLow,
            contentColor = ApexOnSurface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("CALCULADORA 1RM CIENTÍFICA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = loadText,
                        onValueChange = { loadText = it },
                        label = { Text("Carga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Repetições") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainer)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("EPLEY 1RM", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                        Text("${epley.roundToInt()} kg", style = ApexTextStyles.HeadlineMd, color = ApexPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BRZYCKI 1RM", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                        Text("${brzycki.roundToInt()} kg", style = ApexTextStyles.HeadlineMd, color = ApexSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Bottom Sheet: Export JSON/CSV
    if (showExportSheet) {
        var format by remember { mutableStateOf("JSON") }
        val payload = remember(format, logs, profile) { onBuildExportPayload(format) }
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ApexSurfaceContainerLow,
            contentColor = ApexOnSurface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("EXPORTAR TELEMETRIA DA CONTA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("JSON", "CSV").forEach { fmt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (format == fmt) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                                .clickable { format = fmt }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(fmt, style = ApexTextStyles.LabelInteractive, color = if (format == fmt) ApexOnPrimary else ApexOnSurface)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexSurfaceContainerLowest)
                        .padding(12.dp)
                ) {
                    Text(payload, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
                }
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(payload))
                        onShowToast("Dados $format copiados para a área de transferência!")
                        showExportSheet = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar $format", style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BentoMetricCard(
    label: String,
    value: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .border(1.dp, ApexOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
        Text(value, style = ApexTextStyles.HeadlineMd.copy(fontWeight = FontWeight.Black), color = accent)
        Text(subtitle, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
    }
}
