package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.DownhillSkiing
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsHandball
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.UserStatsEntity
import com.example.data.local.WorkoutLogEntity
import com.example.ui.theme.ApexGold
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnPrimaryContainer
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutline
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondaryContainer
import com.example.ui.theme.ApexSuccessGreen
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexTertiary
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import kotlin.math.abs

private data class ChartPoint(
    val xFraction: Float,
    val yFraction: Float, // 0 = top (50K), 1 = bottom (20K)
    val xLabel: String,
    val tooltipVal: String,
    val isDeload: Boolean = false,
    val isPeak: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressoScreen(
    stats: UserStatsEntity,
    logs: List<WorkoutLogEntity>,
    onBuildExportPayload: (String) -> String,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("semanal") }
    var selectedNodeInfo by remember { mutableStateOf<String?>(null) }
    var showCalcSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    val weeklyPoints = remember {
        listOf(
            ChartPoint(0.06f, 0.59f, "S1", "38.000 kg"),
            ChartPoint(0.22f, 0.47f, "S2", "41.000 kg"),
            ChartPoint(0.38f, 0.42f, "S3", "42.500 kg"),
            ChartPoint(0.53f, 0.74f, "S4", "34.000 kg (Deload)", isDeload = true),
            ChartPoint(0.71f, 0.34f, "S5", "45.000 kg"),
            ChartPoint(0.90f, 0.20f, "S6 (Hoje)", "48.650 kg", isPeak = true)
        )
    }
    val monthlyPoints = remember {
        listOf(
            ChartPoint(0.06f, 0.69f, "M1", "142.000 kg"),
            ChartPoint(0.22f, 0.56f, "M2", "156.400 kg"),
            ChartPoint(0.38f, 0.47f, "M3", "164.800 kg"),
            ChartPoint(0.53f, 0.53f, "M4", "159.200 kg (Deload)", isDeload = true),
            ChartPoint(0.71f, 0.28f, "M5", "182.500 kg"),
            ChartPoint(0.90f, 0.16f, "M6", "194.600 kg", isPeak = true)
        )
    }
    val annualPoints = remember {
        listOf(
            ChartPoint(0.06f, 0.81f, "2021", "1.45M kg"),
            ChartPoint(0.22f, 0.72f, "2022", "1.68M kg"),
            ChartPoint(0.38f, 0.59f, "2023", "1.92M kg"),
            ChartPoint(0.53f, 0.47f, "2024", "2.15M kg"),
            ChartPoint(0.71f, 0.31f, "2025", "2.38M kg"),
            ChartPoint(0.90f, 0.13f, "2026", "2.56M kg", isPeak = true)
        )
    }

    val currentPoints = when (selectedPeriod) {
        "mensal" -> monthlyPoints
        "anual" -> annualPoints
        else -> weeklyPoints
    }
    val chartSubLabel = when (selectedPeriod) {
        "mensal" -> "Média de volume por microciclo mensal (kg)"
        "anual" -> "Progressão macroanual de sobrecarga linear (kg)"
        else -> "Volume total acumulado (6 últimas semanas)"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. EXECUTIVE PERFORMANCE BANNER + BENTO GRID
        item(key = "executive_banner") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(ApexPrimaryContainer)
                            )
                            Text(
                                text = "TELEMETRIA DE CARGA & VOLUME",
                                style = ApexTextStyles.LabelCaps,
                                color = ApexPrimary
                            )
                        }
                        Text(
                            text = "Progresso & Tonelagem",
                            style = ApexTextStyles.HeadlineLgMobile,
                            color = ApexOnSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerHigh)
                            .clickable { showExportSheet = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("btn_export_telemetry"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadForOffline,
                            contentDescription = "Exportar",
                            tint = ApexPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("EXPORTAR", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                    }
                }

                // Full-width Tonelagem Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(ApexSurfaceContainer)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VOLUME SEMANAL BRUTO", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ApexSurfaceContainerHighest)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = ApexSuccessGreen, modifier = Modifier.size(14.dp))
                            Text("+12%", style = ApexTextStyles.LabelCaps, color = ApexSuccessGreen)
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = ApexViewModel.formatPtBrNumber(stats.weeklyBaseVolumeKg),
                            style = ApexTextStyles.MetricHuge,
                            color = ApexOnSurface
                        )
                        Text(
                            text = "KG",
                            style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold),
                            color = ApexPrimary,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    Text(
                        text = "+5.210 kg superados em relação ao microciclo S5.",
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(ApexSurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(ApexSecondaryContainer, ApexPrimaryContainer, ApexPrimary)
                                        )
                                    )
                            )
                        }
                        Text("82% DA META", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                    }
                }

                // 2-Col Bento: Assiduidade + Sob Tensão
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ApexSurfaceContainer)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ASSIDUIDADE", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("5", style = ApexTextStyles.MetricLg, color = ApexOnSurface)
                                Text("/ 5", style = ApexTextStyles.HeadlineSm, color = ApexOnSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
                            }
                            Text("100% Executado", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.SemiBold), color = ApexSuccessGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(ApexPrimaryContainer)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ApexSurfaceContainer)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SOB TENSÃO", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                            Icon(Icons.Rounded.Timelapse, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("4h 38m", style = ApexTextStyles.MetricLg, color = ApexOnSurface)
                            Text("Média 55m / sessão", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("RPE 8.2", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ApexOutlineVariant))
                            Text("Hipertrofia", style = ApexTextStyles.LabelCaps, color = ApexSuccessGreen)
                        }
                    }
                }
            }
        }

        // 2. INTERACTIVE VOLUME CHART (CURVA DE SOBRECARGA)
        item(key = "volume_chart") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Analytics, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(20.dp))
                        Text("Curva de Sobrecarga", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerHighest)
                            .padding(2.dp)
                    ) {
                        listOf("semanal" to "Semanal", "mensal" to "Mensal", "anual" to "Anual").forEach { (key, label) ->
                            val active = selectedPeriod == key
                            Text(
                                text = label,
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                color = if (active) ApexOnPrimary else ApexOnSurfaceVariant,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (active) ApexPrimary else Color.Transparent)
                                    .clickable {
                                        selectedPeriod = key
                                        selectedNodeInfo = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(chartSubLabel, style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("Pico: S6 (48.6k)", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimaryContainer)
                }

                // Canvas High-Precision Telemetry Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .padding(top = 8.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(currentPoints) {
                                detectTapGestures { offset ->
                                    val tapXFraction = offset.x / size.width.toFloat()
                                    val nearest = currentPoints.minByOrNull { abs(it.xFraction - tapXFraction) }
                                    if (nearest != null) {
                                        selectedNodeInfo = nearest.tooltipVal
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val bottomY = h * 0.94f
                        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))

                        // Gridlines
                        listOf(0.12f, 0.40f, 0.68f).forEach { yF ->
                            drawLine(
                                color = ApexSurfaceContainerHighest.copy(alpha = 0.7f),
                                start = Offset(0f, h * yF),
                                end = Offset(w, h * yF),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dash
                            )
                        }
                        drawLine(
                            color = ApexSurfaceContainerHighest,
                            start = Offset(0f, bottomY),
                            end = Offset(w, bottomY),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Deload shaded column around index 3 (S4)
                        val deloadX = w * currentPoints[3].xFraction
                        drawRoundRect(
                            color = ApexSurfaceContainerLow.copy(alpha = 0.85f),
                            topLeft = Offset(deloadX - 24.dp.toPx(), h * 0.14f),
                            size = Size(48.dp.toPx(), bottomY - h * 0.14f),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        val coords = currentPoints.map { pt -> Offset(w * pt.xFraction, h * pt.yFraction) }

                        // Area Fill
                        val areaPath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            coords.drop(1).forEach { lineTo(it.x, it.y) }
                            lineTo(coords.last().x, bottomY)
                            lineTo(coords.first().x, bottomY)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    ApexPrimaryContainer.copy(alpha = 0.36f),
                                    Color(0xFF2563EB).copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )

                        // Glowing outer line + crisp cyan stroke line
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            coords.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = linePath,
                            color = ApexPrimaryContainer.copy(alpha = 0.3f),
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        drawPath(
                            path = linePath,
                            color = ApexPrimaryContainer,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Data Nodes
                        currentPoints.forEachIndexed { idx, pt ->
                            val c = coords[idx]
                            if (pt.isPeak) {
                                drawCircle(color = ApexPrimaryContainer.copy(alpha = 0.35f), radius = 11.dp.toPx(), center = c)
                                drawCircle(color = ApexPrimaryContainer, radius = 6.dp.toPx(), center = c)
                                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = c, style = Stroke(2.dp.toPx()))
                            } else {
                                drawCircle(color = ApexSurface, radius = 5.dp.toPx(), center = c)
                                drawCircle(
                                    color = if (pt.isDeload) ApexOutline else ApexPrimaryContainer,
                                    radius = 5.dp.toPx(),
                                    center = c,
                                    style = Stroke(2.5.dp.toPx())
                                )
                            }
                        }
                    }

                    // Reference right Y-axis labels
                    Text("50K", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOutline, modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp))
                    Text("40K", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOutline, modifier = Modifier.align(Alignment.CenterEnd).padding(bottom = 26.dp))
                    Text("30K", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOutline, modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 44.dp))
                    Text("DELOAD", style = ApexTextStyles.LabelCaps.copy(fontSize = 8.sp), color = ApexOutline, modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp, start = 18.dp))
                    Text("48.65K", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimaryContainer, modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 24.dp))
                }

                // Interactive X-Axis Labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    currentPoints.forEach { pt ->
                        Text(
                            text = pt.xLabel,
                            style = ApexTextStyles.LabelCaps.copy(
                                fontSize = 10.sp,
                                fontWeight = if (pt.isPeak || pt.isDeload) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = when {
                                pt.isPeak -> ApexPrimary
                                pt.isDeload -> ApexOnSurface
                                else -> ApexOnSurfaceVariant
                            },
                            modifier = Modifier.clickable { selectedNodeInfo = pt.tooltipVal }
                        )
                    }
                }

                // Dynamic Tooltip Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexSurfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selectedNodeInfo != null) Icons.Rounded.QueryStats else Icons.Rounded.Info,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (selectedNodeInfo != null) {
                        Text(
                            text = buildAnnotatedString {
                                append("Tonelagem Registrada: ")
                                withStyle(SpanStyle(color = ApexPrimary, fontWeight = FontWeight.Bold)) {
                                    append(selectedNodeInfo!!)
                                }
                            },
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurface
                        )
                    } else {
                        Text(
                            text = "Toque nos pontos da curva para inspecionar a tonelagem por microciclo.",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurface
                        )
                    }
                }
            }
        }

        // 3. RECORDES PESSOAIS (PRs) & CALCULADORA 1RM
        item(key = "prs_section") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(21.dp))
                        Text("Recordes Pessoais (PRs)", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerHigh)
                            .clickable { showCalcSheet = true }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("btn_open_1rm_calc"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Calculate, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
                        Text("CALCULADORA 1RM", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexPrimary)
                    }
                }

                PersonalRecordCard(
                    title = "Supino Reto",
                    badge = "NOVO PR (+5kg) 🔥",
                    subtitle = "Batido há 2 dias com RPE 9.0",
                    realWeight = "100 kg",
                    estimated1Rm = "118 kg",
                    highlightWeight = true
                )
                PersonalRecordCard(
                    title = "Agachamento Livre",
                    badge = null,
                    subtitle = "Constância de profundidade paralela",
                    realWeight = "140 kg",
                    estimated1Rm = "162 kg"
                )
                PersonalRecordCard(
                    title = "Levantamento Terra",
                    badge = null,
                    subtitle = "Pegada mista, lockout rígido",
                    realWeight = "165 kg",
                    estimated1Rm = "185 kg"
                )
                PersonalRecordCard(
                    title = "Desenvolvimento Militar (OHP)",
                    badge = null,
                    subtitle = "Postura estrita sem impulso",
                    realWeight = "62 kg",
                    estimated1Rm = "72 kg"
                )
            }
        }

        // 4. MURAL DE CONQUISTAS
        item(key = "achievements_section") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                        Text("Mural de Conquistas", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    Text("3 / 5 DESBLOQUEADAS", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                }

                AchievementCard(
                    icon = Icons.Rounded.WorkspacePremium,
                    iconTint = ApexGold,
                    title = "Primeira Tonelada",
                    tier = "OURO",
                    tierColor = ApexGold,
                    description = "Erga mais de 1.000 kg somados em uma única sessão de treino.",
                    unlocked = true
                )
                AchievementCard(
                    icon = Icons.Rounded.Diamond,
                    iconTint = ApexPrimary,
                    title = "Clube dos 100kg Supino",
                    tier = "PLATINA",
                    tierColor = ApexPrimary,
                    description = "Quebre a barreira de dois dígitos em série válida no Supino Reto.",
                    unlocked = true
                )
                AchievementCard(
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = ApexPrimaryContainer,
                    title = "Disciplina Espartana",
                    tier = "DIAMANTE",
                    tierColor = ApexPrimaryContainer,
                    description = "Mantenha 21 dias consecutivos de constância nutricional e registro.",
                    unlocked = true
                )

                // Achievement 4: In Progress (85%)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ApexSurfaceContainer)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(ApexSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Hardware, contentDescription = null, tint = ApexTertiary, modifier = Modifier.size(25.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Titã da Terra", style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold), color = ApexOnSurface)
                                Text("85%", style = ApexTextStyles.LabelCaps, color = ApexTertiary)
                            }
                            Text("Alcançar 180 kg no Levantamento Terra.", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Atual: 165 kg", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                            Text("Meta: 180 kg (-15kg)", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
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
                                    .fillMaxWidth(0.85f)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(ApexPrimary)
                            )
                        }
                    }
                }

                // Achievement 5: Locked
                AchievementCard(
                    icon = Icons.Rounded.Lock,
                    iconTint = ApexOutline,
                    title = "Monstro dos 500kg",
                    tier = "BLOQUEADO",
                    tierColor = ApexOutline,
                    description = "Total somado Supino + Agacho + Terra > 500 kg (Progresso atual: 405 kg / 500 kg).",
                    unlocked = false,
                    isLocked = true
                )
            }
        }

        // 5. LOGS DE TREINOS RECENTES
        item(key = "logs_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.HistoryEdu, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                    Text("Logs de Treinos Recentes", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                }
                Text("MICROCICLO 42", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
            }
        }

        items(logs, key = { it.id }) { log ->
            val icon = when (log.iconType) {
                "pull" -> Icons.AutoMirrored.Rounded.DirectionsRun
                "legs" -> Icons.Rounded.DownhillSkiing
                else -> Icons.Rounded.SportsHandball
            }
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = log.title,
                            style = ApexTextStyles.LabelInteractive.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            color = ApexOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = log.metaLine,
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${ApexViewModel.formatPtBrNumber(log.tonnageKg)} kg",
                        style = ApexTextStyles.MetricMd.copy(fontSize = 18.sp),
                        color = if (log.isRecentHighlight) ApexPrimary else ApexOnSurface
                    )
                    Text(
                        text = log.seriesLabel,
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = if (log.isRecentHighlight) ApexSuccessGreen else ApexOnSurfaceVariant
                    )
                }
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(14.dp)) }
    }

    // Modal Bottom Sheet: Calculadora 1RM Científica
    if (showCalcSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCalcSheet = false },
            sheetState = sheetState,
            containerColor = ApexSurfaceContainer,
            dragHandle = null
        ) {
            OneRmCalculatorSheetContent(onClose = { showCalcSheet = false })
        }
    }

    // Modal Bottom Sheet: Exportar Telemetria Offline
    if (showExportSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = sheetState,
            containerColor = ApexSurfaceContainer,
            dragHandle = null
        ) {
            ExportTelemetrySheetContent(
                onClose = { showExportSheet = false },
                onExportFormat = { format ->
                    val payload = onBuildExportPayload(format)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (format == "CSV") "text/csv" else "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Apex Performance Telemetria ($format)")
                        putExtra(Intent.EXTRA_TEXT, payload)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(sendIntent, "Exportar Telemetria ($format)"))
                    }
                    onShowToast("Telemetria $format gerada com sucesso offline!")
                }
            )
        }
    }
}

@Composable
private fun PersonalRecordCard(
    title: String,
    badge: String?,
    subtitle: String,
    realWeight: String,
    estimated1Rm: String,
    highlightWeight: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    Text(title, style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold), color = ApexOnSurface)
                    if (badge != null) {
                        Text(
                            text = badge,
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                            color = ApexPrimary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ApexPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(subtitle, style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(realWeight, style = ApexTextStyles.MetricMd, color = if (highlightWeight) ApexPrimary else ApexOnSurface)
                Text("CARGA REAL", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ApexSurfaceContainerLow)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Speed, contentDescription = null, tint = ApexTertiary, modifier = Modifier.size(16.dp))
                Text("1RM Estimado (Epley):", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
            }
            Text(estimated1Rm, style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.ExtraBold), color = ApexOnSurface)
        }
    }
}

@Composable
private fun AchievementCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    tier: String,
    tierColor: Color,
    description: String,
    unlocked: Boolean,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.62f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLocked) ApexSurfaceContainerLow else ApexSurfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            if (unlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(iconTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (iconTint == ApexPrimaryContainer) ApexOnPrimaryContainer else ApexSurface
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold), color = ApexOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(tier, style = ApexTextStyles.LabelCaps, color = tierColor)
            }
            Text(description, style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OneRmCalculatorSheetContent(onClose: () -> Unit) {
    var weightInput by remember { mutableStateOf("100") }
    var repsInput by remember { mutableStateOf("5") }

    val w = weightInput.toDoubleOrNull() ?: 0.0
    val r = (repsInput.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
    val epley = if (w > 0) w * (1.0 + r / 30.0) else 0.0
    val brzycki = if (w > 0 && r < 37) w / (1.0278 - 0.0278 * r) else epley

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Calculate, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(24.dp))
                Text("Calculadora 1RM Científica", style = ApexTextStyles.HeadlineMd, color = ApexOnSurface)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHighest)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close), tint = ApexOnSurface, modifier = Modifier.size(18.dp))
            }
        }

        Text(
            text = "Estime sua repetição máxima com base em carga e repetições executadas sem ir até a falha total concêntrica.",
            style = ApexTextStyles.BodySm,
            color = ApexOnSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CARGA LEVANTADA (KG)", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = ApexTextStyles.HeadlineSm.copy(color = ApexOnSurface),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexSurfaceContainerHighest,
                        unfocusedContainerColor = ApexSurfaceContainerHighest,
                        focusedBorderColor = ApexPrimary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("REPS EXECUTADAS", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                OutlinedTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it.filter(Char::isDigit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = ApexTextStyles.HeadlineSm.copy(color = ApexOnSurface),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexSurfaceContainerHighest,
                        unfocusedContainerColor = ApexSurfaceContainerHighest,
                        focusedBorderColor = ApexPrimary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ApexSurfaceContainerLow)
                    .padding(12.dp)
            ) {
                Text("FÓRMULA EPLEY", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Text("${String.format("%.1f", epley)} kg", style = ApexTextStyles.HeadlineLgMobile, color = ApexOnSurface)
                Text("Ideal para 1-8 repetições", style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ApexSurfaceContainerLow)
                    .padding(12.dp)
            ) {
                Text("FÓRMULA BRZYCKI", style = ApexTextStyles.LabelCaps, color = ApexTertiary)
                Text("${String.format("%.1f", brzycki)} kg", style = ApexTextStyles.HeadlineLgMobile, color = ApexOnSurface)
                Text("Conservador para força pura", style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(ApexPrimaryContainer, ApexSecondaryContainer)))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("Concluído", style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.Bold), color = ApexSurface)
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun ExportTelemetrySheetContent(
    onClose: () -> Unit,
    onExportFormat: (String) -> Unit
) {
    var exportedOnce by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Backup, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(24.dp))
                Text("Exportar Telemetria Offline", style = ApexTextStyles.HeadlineMd, color = ApexOnSurface)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHighest)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_close), tint = ApexOnSurface, modifier = Modifier.size(18.dp))
            }
        }

        Text(
            text = "Gere uma cópia de segurança dos seus dados biométricos e tonelagem acumulada para uso local em planilhas ou importação.",
            style = ApexTextStyles.BodySm,
            color = ApexOnSurfaceVariant
        )

        ExportOptionRow(
            icon = Icons.Rounded.DataObject,
            iconTint = ApexPrimary,
            title = "Exportar formato JSON",
            subtitle = "Backup completo com timestamps brutos",
            onClick = {
                exportedOnce = true
                onExportFormat("JSON")
            }
        )
        ExportOptionRow(
            icon = Icons.Rounded.TableChart,
            iconTint = ApexPrimaryContainer,
            title = "Exportar formato CSV",
            subtitle = "Compatível com Excel, Notion e Google Sheets",
            onClick = {
                exportedOnce = true
                onExportFormat("CSV")
            }
        )

        if (exportedOnce) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ApexPrimary.copy(alpha = 0.12f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download iniciado com sucesso! Arquivo gerado offline.", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.SemiBold), color = ApexPrimary)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun ExportOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ApexSurfaceContainerHighest)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Column {
                Text(title, style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                Text(subtitle, style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
            }
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = ApexOnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}
