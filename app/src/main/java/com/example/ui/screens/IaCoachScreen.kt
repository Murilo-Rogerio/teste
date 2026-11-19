package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Troubleshoot
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.ui.components.PulsingCyanDot
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexErrorContainer
import com.example.ui.theme.ApexOnErrorContainer
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSecondaryContainer
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondaryContainer
import com.example.ui.theme.ApexSecondaryFixed
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.CoachMode
import com.example.viewmodel.CoachSubView
import com.example.viewmodel.PlateauScanState
import java.util.Locale

private const val IMG_BIOMECH_PRESS =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCVFLGl1xPWEX2DWHCObEqjhJHzRLGu7hbQXU8o2VRNx7ODIbcx7zgPPUARjrS7bUCR-Ck6ARa9TDFBH8KMyOqk1BmZS8rIR3iMSmdHwoI_Mpl3Exm9kd7e7oQGAJP0iOrz7Pf4wyzgBP4ORfB7DFqgXzPbSdM5T_vEch8GBHcqQmxMvL3ZSiXtYavbHjtizNfQRpxQr-qBPdOBaLVsXgXV8pw7diHXf0U0WAcrqqo"
private const val IMG_BIOMECH_KNURL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBrVD64s7JK9U9aumIWj2x0oMhVhI9FElP1T5a4Kke5q9y3C6STNV1z7Wl72KuGNNufvWs1bi6xxq9W_jt_d7fBNVhhFytqvvN75vZzBN2RaX3lgzSk5aUbB7sKkcgoRxxsu2GwBjV6BPwWmtcpxAATFP-ZoRNQW5J5n09JtI-zCqU5Dei5KOujwGfhrQyFd0Jx_lOcLNLr8tiHTM5SIMgALVd5Id5WsoQEaCJUTzo"

@Composable
fun IaCoachScreen(
    subView: CoachSubView,
    selectedMode: CoachMode,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    isWeight80Applied: Boolean,
    plateauScan: PlateauScanState,
    activeTelemetryLine: String,
    onSelectSubView: (CoachSubView) -> Unit,
    onSelectMode: (CoachMode) -> Unit,
    onApply80Kg: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRunPlateauScan: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = spoken
                onSendMessage(spoken)
                inputText = ""
            }
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (subView == CoachSubView.CHAT && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Assistant HUD + Sub-View Switcher (Chat Gemini Pro | Análise & Platôs)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ApexSurfaceContainerLowest.copy(alpha = 0.94f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = ApexPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        PulsingCyanDot(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Apex Gemini 1.5 Pro",
                                style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.ExtraBold),
                                color = ApexOnSurface
                            )
                            Text(
                                text = "NEURAL V3",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                                color = ApexSecondaryFixed,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ApexSecondaryContainer.copy(alpha = 0.45f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ApexPrimaryContainer)
                            )
                            Text(
                                text = "ONLINE • VINCULADO AO TREINO ATIVO",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                color = ApexPrimary
                            )
                        }
                    }
                }

                // Toggle between Chat & Analytics (both HTML screens seamlessly accessible)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (subView == CoachSubView.ANALYTICS) ApexPrimary else ApexSurfaceContainer
                        )
                        .clickable {
                            onSelectSubView(
                                if (subView == CoachSubView.CHAT) CoachSubView.ANALYTICS else CoachSubView.CHAT
                            )
                        }
                        .testTag("coach_subview_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Alternar painel de análise e platôs",
                        tint = if (subView == CoachSubView.ANALYTICS) ApexOnPrimary else ApexOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Sub-view clean segmented pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApexSurfaceContainer)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubViewPill(
                    label = "Consultoria Gemini",
                    selected = subView == CoachSubView.CHAT,
                    onClick = { onSelectSubView(CoachSubView.CHAT) },
                    modifier = Modifier.weight(1f)
                )
                SubViewPill(
                    label = "Platôs & Sobrecarga",
                    selected = subView == CoachSubView.ANALYTICS,
                    onClick = { onSelectSubView(CoachSubView.ANALYTICS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Live Telemetry Stream Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApexSurfaceContainerLow)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sensors,
                        contentDescription = null,
                        tint = ApexPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Lendo sessão ativa: ")
                            withStyle(SpanStyle(color = ApexOnSurface, fontWeight = FontWeight.Bold)) {
                                append(activeTelemetryLine)
                            }
                        },
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "140 BPM",
                    style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                    color = ApexPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ApexPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }

            if (subView == CoachSubView.CHAT) {
                // Mode Selector Segmented Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CoachMode.entries.forEach { mode ->
                        val active = mode == selectedMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) ApexPrimary else Color.Transparent)
                                .clickable { onSelectMode(mode) }
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label.uppercase(),
                                style = ApexTextStyles.LabelCaps.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = if (active) ApexOnPrimary else ApexOnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (subView == CoachSubView.CHAT) {
            // Quick Action Pills
            val quickPills = listOf(
                "💡" to ("Ombro estalou no supino?" to "Por que meu ombro estalou no supino?"),
                "⚖️" to ("+2kg ou +2 reps?" to "Devo aumentar 2kg ou fazer mais 2 reps com a mesma carga?"),
                "🔄" to ("Trocar Agachamento Livre" to "Preciso de substituição para Agachamento Livre devido a desconforto lombar."),
                "🥪" to ("Pré-treino pernas (45m)" to "O que consumir 45 minutos antes de um treino pesado de pernas?"),
                "🔋" to ("Momento do Deload" to "Quando devo aplicar uma semana de deload no meu ciclo?")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPills.forEach { (emoji, pair) ->
                    val (label, prompt) = pair
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerHigh)
                            .clickable(enabled = !isGenerating) { onSendMessage(prompt) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = emoji, fontSize = 13.sp)
                        Text(
                            text = label,
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurface
                        )
                    }
                }
            }

            // Chat Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    when {
                        msg.isUser -> UserChatBubble(msg)
                        msg.isWelcomeCard -> WelcomeAiBubble(msg)
                        msg.isVbtCard -> VbtDiagnosisAiBubble(
                            msg = msg,
                            isWeight80Applied = isWeight80Applied,
                            onApply80Kg = onApply80Kg
                        )
                        else -> StandardAiBubble(msg = msg, onFeedback = onShowToast)
                    }
                }
            }

            // Floating Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApexSurfaceContainerLowest.copy(alpha = 0.96f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerHigh)
                        .clickable {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR").toLanguageTag())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale sua dúvida ao Apex Gemini Coach")
                                }
                                speechLauncher.launch(intent)
                            } catch (_: Exception) {
                                onShowToast("Reconhecimento de voz indisponível neste dispositivo.")
                            }
                        }
                        .testTag("btn_voice_input"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = stringResource(R.string.cd_voice_input),
                        tint = ApexPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Pergunte ao Gemini Coach (ex: biomecânica, RPE)...",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Limpar texto",
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { inputText = "" }
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = ApexTextStyles.BodyMd.copy(color = ApexOnSurface),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isGenerating) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ApexSurfaceContainerHigh,
                        unfocusedContainerColor = ApexSurfaceContainerHigh,
                        focusedBorderColor = ApexPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_coach_chat")
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(ApexSecondaryContainer, ApexPrimaryContainer))
                        )
                        .clickable(enabled = !isGenerating && inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                        .testTag("btn_send_coach"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ApexSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.cd_send_message),
                            tint = ApexSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // ANALYTICS & PLATEAU VIEW (HTML #3 faithful reproduction!)
            CoachAnalyticsPanel(
                plateauScan = plateauScan,
                onRunScan = onRunPlateauScan,
                onAskPrompt = { query -> onSendMessage(query) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SubViewPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) ApexSurfaceContainerHighest else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
            color = if (selected) ApexPrimary else ApexOnSurfaceVariant
        )
    }
}

@Composable
private fun AiAvatarCircle() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(ApexSecondaryContainer, ApexPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = ApexSurface,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun WelcomeAiBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AiAvatarCircle()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Apex Gemini Coach", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.Bold), color = ApexPrimary)
                Text(msg.timestamp, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .background(ApexSurfaceContainerHigh)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Salve, atleta! Sou o seu Apex Gemini Coach. Estou conectado aos seus sensores e histórico de repetições em tempo real.",
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSurface
                )
                Text(
                    text = "Acompanho seu volume semanal, RPE acumulado e velocidade da barra. Qual a dúvida ou ajuste biomecânico para a sessão de hoje?",
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(15.dp))
                    Text("TELEMETRIA ATIVA: BANCO SUPINO #1", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                }
            }
        }
    }
}

@Composable
private fun UserChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(msg.timestamp, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                Text("Você", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.SemiBold), color = ApexOnSurface)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .background(ApexSecondaryContainer)
                    .padding(14.dp)
            ) {
                Text(
                    text = msg.text,
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSecondaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ApexSurfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = ApexOnSurface, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VbtDiagnosisAiBubble(
    msg: ChatMessage,
    isWeight80Applied: Boolean,
    onApply80Kg: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AiAvatarCircle()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Apex Gemini Coach", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.Bold), color = ApexPrimary)
                Text(msg.timestamp, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .background(ApexSurfaceContainerHigh)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(19.dp))
                    Text(
                        text = "Diagnóstico VBT (Velocity Based Training)",
                        style = ApexTextStyles.LabelInteractive,
                        color = ApexPrimary
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        append("Detectei uma queda de ")
                        withStyle(SpanStyle(color = ApexOnSurface, fontWeight = FontWeight.Bold)) {
                            append("~28% na velocidade concêntrica média")
                        }
                        append(" entre a rep 6 e 8 na série 3. Seu RPE real saltou de 7.5 para ")
                        withStyle(SpanStyle(color = ApexOnSurface, fontWeight = FontWeight.Bold)) {
                            append("9.2")
                        }
                        append(".")
                    },
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSurfaceVariant
                )

                // Telemetry Mini Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexSurfaceContainerLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PERDA DE VELOCIDADE", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        Text("-28% (Fadiga)", style = ApexTextStyles.MetricMd.copy(fontSize = 16.sp), color = ApexError)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RIR PROJETADO (SÉRIE 4)", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        Text("0 ou Falha", style = ApexTextStyles.MetricMd.copy(fontSize = 16.sp), color = ApexPrimary)
                    }
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Recomendação: ") }
                        append("Aplique uma redução autorregulada de ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("4% da carga (-4kg = 80kg totais)") }
                        append(". Isso preservará seu recrutamento de unidades motoras sem fritar o SNC antes do Supino Inclinado com Halteres.")
                    },
                    style = ApexTextStyles.BodyMd,
                    color = ApexOnSurface
                )

                // Interactive Action Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ApexSurfaceContainerLowest)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ApexPrimaryContainer.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Série 4: Ajustar para 80kg", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.Bold), color = ApexOnSurface)
                            Text("METAS: 8 REPS @ RPE 8.0", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWeight80Applied) "80KG APLICADO!" else "APLICAR 80KG",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = if (isWeight80Applied) ApexOnSecondaryContainer else ApexOnPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWeight80Applied) ApexSecondaryContainer else ApexPrimary)
                            .clickable(onClick = onApply80Kg)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("btn_apply_80kg")
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardAiBubble(
    msg: ChatMessage,
    onFeedback: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AiAvatarCircle()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Apex Gemini Coach", style = ApexTextStyles.BodySm.copy(fontWeight = FontWeight.Bold), color = ApexPrimary)
                Text(msg.timestamp, style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
                    .background(if (msg.isError) ApexErrorContainer.copy(alpha = 0.6f) else ApexSurfaceContainerHigh)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (msg.isStreaming && msg.text.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ApexPrimary
                        )
                        Text(
                            text = "GEMINI PENSANDO...",
                            style = ApexTextStyles.LabelCaps,
                            color = ApexOnSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = msg.text.replace("**", ""),
                        style = ApexTextStyles.BodyMd,
                        color = if (msg.isError) ApexOnErrorContainer else ApexOnSurface
                    )
                }

                if (!msg.isStreaming && !msg.isError && msg.modeTag != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ApexOutlineVariant.copy(alpha = 0.25f))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MODO: ${msg.modeTag}",
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                            color = ApexOnSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.ThumbUp,
                                contentDescription = "Útil",
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onFeedback("Feedback registrado no Apex Neural Engine") }
                            )
                            Icon(
                                imageVector = Icons.Rounded.ThumbDown,
                                contentDescription = "Não ajudou",
                                tint = ApexOnSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onFeedback("Ajustaremos as próximas recomendações") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachAnalyticsPanel(
    plateauScan: PlateauScanState,
    onRunScan: () -> Unit,
    onAskPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Status Telemetry Banner (HTML #3)
        item(key = "ai_status_banner") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    Column {
                        Text("Apex AI Coach", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                        Text("SOBRECARGA PROGRESSIVA PREDITIVA", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                    }
                    Text(
                        text = "V4.8 NEURAL",
                        style = ApexTextStyles.LabelCaps,
                        color = ApexPrimary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerHighest)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ApexPrimary))
                    Text(
                        text = "Dados biométricos e telemetria das últimas 3 semanas analisados",
                        style = ApexTextStyles.BodySm,
                        color = ApexPrimary
                    )
                }
            }
        }

        // Detector de Platô & Deload
        item(key = "plateau_detector") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        Icon(Icons.Rounded.Troubleshoot, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(20.dp))
                        Text("Detector de Platô & Deload", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                    Text("TEMPO REAL", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                }
                Text(
                    text = "Varredura heurística de estagnação por curvas de velocidade (VBT), repetições de reserva (RIR) e tonelagem semanal.",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(ApexPrimaryContainer, ApexSecondaryContainer)))
                        .clickable(enabled = !plateauScan.isScanning, onClick = onRunScan)
                        .testTag("btn_scan_plateau"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (plateauScan.isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ApexOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            plateauScan.isScanning -> "Processando Modelagem Biomecânica..."
                            plateauScan.completedOnce -> "Varredura Concluída • Platô Mapeado"
                            else -> "Verificar Platôs e Estagnação"
                        },
                        style = ApexTextStyles.LabelInteractive,
                        color = ApexOnPrimary
                    )
                }

                AnimatedVisibility(visible = plateauScan.isScanning) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(plateauScan.stageLabel, style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                            Text("${plateauScan.progressPercent}%", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(ApexSurfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(plateauScan.progressPercent / 100f)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(ApexPrimaryContainer)
                            )
                        }
                    }
                }

                // Stagnation Alert Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ApexSurfaceContainerHigh)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = ApexError, modifier = Modifier.size(18.dp))
                            Text("ALERTA DE PLATÔ BIOMECÂNICO", style = ApexTextStyles.LabelCaps, color = ApexError)
                        }
                        Text(
                            text = "CRÍTICO",
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                            color = ApexOnErrorContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ApexErrorContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Desenvolvimento Militar: ") }
                            append("Carga paralisada em ")
                            withStyle(SpanStyle(color = ApexPrimary, fontWeight = FontWeight.SemiBold)) { append("28 kg") }
                            append(" nos últimos 3 treinos consecutivos com decaimento de cadência na 3ª série.")
                        },
                        style = ApexTextStyles.BodySm,
                        color = ApexOnSurface
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ApexSurfaceContainer)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(15.dp))
                            Text("PROTOCOLO CORRETIVO RECOMENDADO", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                        }
                        Text(
                            text = plateauScan.geminiInsight?.replace("**", "")
                                ?: "Executar Deload de 15% (24 kg) por 1 microciclo com foco em pausa isométrica, ou substituir pelo Desenvolvimento Arnold para recrutamento do feixe anterior sob novo vetor.",
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                        plateauScan.errorMessage?.let { err ->
                            Text(text = err, style = ApexTextStyles.BodySm, color = ApexError)
                        }
                    }
                }
            }
        }

        // Sobrecarga Inteligente Feed
        item(key = "overload_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(20.dp))
                    Text("Sobrecarga Inteligente", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                }
                Text(
                    text = "PRÓXIMO TREINO",
                    style = ApexTextStyles.LabelCaps,
                    color = ApexPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ApexSurfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Feed Card 1: Supino Reto Barra
        item(key = "overload_1") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(ApexSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Supino Reto Barra", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                            Text("PEITORAL • FORÇA HIPERTRÓFICA", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+2.5 kg", style = ApexTextStyles.MetricMd, color = ApexPrimary)
                        Text("Sobrecarga", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(ApexSurfaceContainerHigh).padding(10.dp)
                    ) {
                        Text("BASELINE SEMANAL", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        Text("3x8 @ 82 kg", style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                        Text("RPE 7.5 (Ótimo)", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
                    }
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(ApexSurfaceContainerHigh).padding(10.dp)
                    ) {
                        Text("ALVO CALCULADO", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexPrimary)
                        Text("84.5 kg", style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                        Text("Faixa: 6-8 Repetições", style = ApexTextStyles.BodySm, color = ApexPrimary)
                    }
                }
                Text(
                    text = "A aceleração concêntrica manteve-se constante (+0.08 m/s). O limiar neural indica prontidão absoluta para o salto de 2.5 kg.",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
            }
        }

        // Feed Card 2: Levantamento Terra
        item(key = "overload_2") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(ApexSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Levantamento Terra", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                            Text("CADEIA POSTERIOR • VOLUME ÓTIMO", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("+1 Rep", style = ApexTextStyles.MetricMd, color = ApexPrimary)
                        Text("Intensidade", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ApexSurfaceContainerHigh).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TONELAGEM ACUMULADA", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        Text("+450 kg acumulados", style = ApexTextStyles.LabelInteractive, color = ApexOnSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CARGA PRESERVADA", style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
                        Text("140 kg fixos", style = ApexTextStyles.LabelInteractive, color = ApexPrimary)
                    }
                }
                Text(
                    text = "Preserve a carga de 140 kg para mitigação de fadiga lombar axial. Progrida adicionando exatamente 1 repetição na 3ª série antes de elevar o peso.",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
            }
        }

        // Feed Card 3: Agachamento Livre
        item(key = "overload_3") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(ApexSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.HourglassEmpty, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Agachamento Livre", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                            Text("QUADRÍCEPS • OTIMIZAÇÃO METABÓLICA", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("-30s", style = ApexTextStyles.MetricMd, color = ApexPrimary)
                        Text("Densidade", style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ApexSurfaceContainerHigh).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Descanso entre séries:", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = ApexOnSurfaceVariant, textDecoration = TextDecoration.LineThrough)) {
                                append("180 seg")
                            }
                            append(" → ")
                            withStyle(SpanStyle(color = ApexPrimary, fontWeight = FontWeight.Bold)) {
                                append("150 seg")
                            }
                        },
                        style = ApexTextStyles.BodySm
                    )
                }
                Text(
                    text = "RPE registrado foi 6.0 no microciclo anterior com frequência cardíaca estável. Comprimir o descanso elevará o estresse metabólico sem estresse articular extra.",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
            }
        }

        // Visual Biometrics Break
        item(key = "biometrics_break") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BiometricsPhotoCard(
                    imageUrl = IMG_BIOMECH_PRESS,
                    tag = "MECÂNICA DE EMPURRAR",
                    subtitle = "Vetor Deltoide Anterior",
                    onClick = { onAskPrompt("Como ajustar o volume semanal de ombros sem fadiga?") },
                    modifier = Modifier.weight(1f)
                )
                BiometricsPhotoCard(
                    imageUrl = IMG_BIOMECH_KNURL,
                    tag = "CARGA • TENSÃO MECÂNICA",
                    subtitle = "Sobrecarga Linear Ótima",
                    onClick = { onAskPrompt("Como quebrar o platô no supino reto?") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(14.dp)) }
    }
}

@Composable
private fun BiometricsPhotoCard(
    imageUrl: String,
    tag: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = subtitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ApexSurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = ApexPrimary.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, ApexSurfaceContainerLowest.copy(alpha = 0.92f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(tag, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexPrimary)
            Text(subtitle, style = ApexTextStyles.BodySm, color = ApexOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
