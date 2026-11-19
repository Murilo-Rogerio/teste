package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.UserProfile
import com.example.data.WorkoutLog
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondary
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.CoachMode
import com.example.viewmodel.CoachSubView
import com.example.viewmodel.PlateauScanState

@Composable
fun IaCoachScreen(
    profile: UserProfile,
    workoutLogs: List<WorkoutLog>,
    subView: CoachSubView,
    selectedMode: CoachMode,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    plateauScan: PlateauScanState,
    onSelectSubView: (CoachSubView) -> Unit,
    onSelectMode: (CoachMode) -> Unit,
    onSendMessage: (String) -> Unit,
    onRunPlateauScan: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Sub-view segmented pill (Chat Gemini | Platôs & Sobrecarga)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ApexSurfaceContainer)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubViewTab(
                label = "Chat Gemini",
                selected = subView == CoachSubView.CHAT,
                onClick = { onSelectSubView(CoachSubView.CHAT) },
                modifier = Modifier.weight(1f).testTag("coach_tab_chat")
            )
            SubViewTab(
                label = "Platôs & Sobrecarga",
                selected = subView == CoachSubView.ANALYTICS,
                onClick = { onSelectSubView(CoachSubView.ANALYTICS) },
                modifier = Modifier.weight(1f).testTag("coach_tab_analytics")
            )
        }

        if (subView == CoachSubView.CHAT) {
            CoachChatView(
                profile = profile,
                selectedMode = selectedMode,
                messages = messages,
                isGenerating = isGenerating,
                onSelectMode = onSelectMode,
                onSendMessage = onSendMessage,
                onShowToast = onShowToast,
                modifier = Modifier.weight(1f)
            )
        } else {
            CoachAnalyticsView(
                profile = profile,
                workoutLogs = workoutLogs,
                plateauScan = plateauScan,
                onRunPlateauScan = onRunPlateauScan,
                onAskCoach = onSendMessage,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SubViewTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ApexTextStyles.LabelInteractive.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = if (selected) ApexOnPrimary else ApexOnSurfaceVariant
        )
    }
}

@Composable
private fun CoachChatView(
    profile: UserProfile,
    selectedMode: CoachMode,
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    onSelectMode: (CoachMode) -> Unit,
    onSendMessage: (String) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    DisposableEffect(speechRecognizer) {
        onDispose { speechRecognizer?.destroy() }
    }

    fun startVoiceRecognition() {
        val recognizer = speechRecognizer ?: run {
            onShowToast("Reconhecimento de voz indisponível neste dispositivo.")
            return
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                isListening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!spoken.isNullOrBlank()) inputText = spoken
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        }
        recognizer.startListening(intent)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceRecognition()
        else onShowToast("Permissão de microfone negada.")
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Real athlete context strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ApexSurfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
            Text(
                text = "@${profile.username} • ${String.format("%.1f", profile.weightKg)}kg • ${profile.heightCm.toInt()}cm • ${profile.goalLabel}",
                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                color = ApexOnSurface
            )
        }

        // Mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoachMode.entries.forEach { mode ->
                val active = mode == selectedMode
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (active) ApexPrimaryContainer else ApexSurfaceContainer)
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = mode.label,
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = if (active) ApexOnPrimary else ApexOnSurfaceVariant
                    )
                }
            }
        }

        // Chat feed
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg = msg)
            }
        }

        // Quick suggestions tailored to real profile
        val suggestions = listOf(
            "Monte uma divisão semanal para meu objetivo (${profile.goalLabel})",
            "Qual deve ser minha ingestão de proteína e água hoje?",
            "Como começar a progredir cargas sem overtraining?"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ApexSurfaceContainerHigh)
                        .border(1.dp, ApexPrimary.copy(alpha = 0.25f), CircleShape)
                        .clickable(enabled = !isGenerating) { onSendMessage(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = ApexTextStyles.BodySm.copy(fontSize = 11.sp),
                        color = ApexPrimary
                    )
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (isListening) {
                        speechRecognizer?.stopListening()
                        isListening = false
                    } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startVoiceRecognition()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListening) ApexError.copy(alpha = 0.2f) else ApexSurfaceContainerHigh)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.StopCircle else Icons.Rounded.Mic,
                    contentDescription = "Entrada por voz",
                    tint = if (isListening) ApexError else ApexPrimary
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Pergunte ao Apex Gemini Coach...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ApexPrimary,
                    unfocusedBorderColor = ApexOutlineVariant,
                    focusedTextColor = ApexOnSurface,
                    unfocusedTextColor = ApexOnSurface,
                    focusedContainerColor = ApexSurfaceContainer,
                    unfocusedContainerColor = ApexSurfaceContainer
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).testTag("coach_chat_input")
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isGenerating) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = !isGenerating && inputText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank() && !isGenerating) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                    .testTag("coach_send_btn")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ApexPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Enviar mensagem",
                        tint = if (inputText.isNotBlank()) ApexOnPrimary else ApexOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    when {
                        msg.isUser -> ApexPrimaryContainer.copy(alpha = 0.2f)
                        msg.isError -> ApexError.copy(alpha = 0.14f)
                        else -> ApexSurfaceContainer
                    }
                )
                .border(
                    1.dp,
                    when {
                        msg.isUser -> ApexPrimary.copy(alpha = 0.35f)
                        msg.isError -> ApexError.copy(alpha = 0.4f)
                        else -> ApexOutlineVariant.copy(alpha = 0.4f)
                    },
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!msg.isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = msg.modeTag ?: "APEX GEMINI COACH",
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexPrimary
                    )
                }
            }

            Text(
                text = if (msg.text.isEmpty() && msg.isStreaming) "Analisando seus dados biométricos..." else msg.text,
                style = ApexTextStyles.BodyMd,
                color = if (msg.isError) ApexError else ApexOnSurface
            )

            Text(
                text = if (msg.isStreaming) "Gerando resposta..." else msg.timestamp,
                style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                color = ApexOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun CoachAnalyticsView(
    profile: UserProfile,
    workoutLogs: List<WorkoutLog>,
    plateauScan: PlateauScanState,
    onRunPlateauScan: () -> Unit,
    onAskCoach: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTonnage = remember(workoutLogs) { workoutLogs.sumOf { it.tonnageKg } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Real Biometric Snapshot Card
        item(key = "bio_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("TELEMETRIA REAL DO ATLETA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnalyticsCell("PESO ATUAL", "${String.format("%.1f", profile.weightKg)} kg")
                    AnalyticsCell("TREINOS SALVOS", workoutLogs.size.toString())
                    AnalyticsCell("TONELAGEM TOTAL", "${ApexViewModel.formatPtBrLong(totalTonnage)} kg")
                }
                if (profile.waistCm > 0 || profile.chestCm > 0 || profile.armCm > 0 || profile.bodyFatPercent > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ApexSurfaceContainerLowest)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if (profile.waistCm > 0) AnalyticsCell("CINTURA", "${String.format("%.1f", profile.waistCm)} cm")
                        if (profile.chestCm > 0) AnalyticsCell("PEITORAL", "${String.format("%.1f", profile.chestCm)} cm")
                        if (profile.armCm > 0) AnalyticsCell("BRAÇO", "${String.format("%.1f", profile.armCm)} cm")
                        if (profile.bodyFatPercent > 0) AnalyticsCell("GORDURA", "${String.format("%.1f", profile.bodyFatPercent)}%")
                    }
                }
            }
        }

        // Neural Scanner Card (Real Gemini analysis)
        item(key = "scanner_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainer)
                    .border(1.dp, ApexPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Radar, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(22.dp))
                    Column {
                        Text("SCANNER NEURAL DE SOBRECARGA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                        Text("Análise via Gemini AI Logic", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    }
                }

                Text(
                    text = plateauScan.stageLabel,
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )

                if (plateauScan.isScanning || plateauScan.progressPercent > 0) {
                    LinearProgressIndicator(
                        progress = { (plateauScan.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                        color = ApexPrimaryContainer,
                        trackColor = ApexSurfaceContainerLowest,
                        strokeCap = StrokeCap.Round
                    )
                }

                if (plateauScan.geminiInsight != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ApexPrimaryContainer.copy(alpha = 0.14f))
                            .border(1.dp, ApexPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(plateauScan.geminiInsight, style = ApexTextStyles.BodyMd, color = ApexOnSurface)
                    }
                }

                if (plateauScan.errorMessage != null) {
                    Text(plateauScan.errorMessage, style = ApexTextStyles.BodySm, color = ApexError)
                }

                Button(
                    onClick = onRunPlateauScan,
                    enabled = !plateauScan.isScanning,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApexPrimaryContainer,
                        contentColor = ApexOnPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_run_plateau_scan")
                ) {
                    Icon(Icons.Rounded.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (plateauScan.isScanning) "Analisando..." else "Executar Varredura com IA",
                        style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }

        // Quick action to generate personalized starter plan via AI
        item(key = "starter_plan_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PRECISA DE AJUDA PARA COMEÇAR?", style = ApexTextStyles.LabelCaps, color = ApexSecondary)
                Text(
                    text = "Peça ao Apex Gemini Coach uma sugestão de fichas personalizadas baseada no seu peso (${String.format("%.1f", profile.weightKg)}kg), altura (${profile.heightCm.toInt()}cm) e objetivo (${profile.goalLabel}).",
                    style = ApexTextStyles.BodySm,
                    color = ApexOnSurfaceVariant
                )
                Button(
                    onClick = {
                        onAskCoach(
                            "Crie uma recomendação de divisão de treinos semanal sob medida para meu perfil (${profile.weightKg}kg, ${profile.heightCm}cm, objetivo: ${profile.goalLabel})."
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ApexSurfaceContainerHigh, contentColor = ApexPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gerar Sugestão de Treino no Chat", style = ApexTextStyles.LabelInteractive)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
        Text(value, style = ApexTextStyles.LabelInteractive.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold), color = ApexOnSurface)
    }
}
