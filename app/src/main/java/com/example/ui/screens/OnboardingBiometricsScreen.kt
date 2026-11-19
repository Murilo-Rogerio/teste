package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSecondary
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainer
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexViewModel
import kotlin.math.roundToInt

private data class ActivityOption(val label: String, val factor: Double)
private data class GoalOption(val label: String, val deltaKcal: Int)

private val activityOptions = listOf(
    ActivityOption("Sedentário", 1.2),
    ActivityOption("Leve (1-2x)", 1.375),
    ActivityOption("Moderado (3-5x)", 1.55),
    ActivityOption("Intenso (6x)", 1.725),
    ActivityOption("Atleta", 1.9)
)

private val goalOptions = listOf(
    GoalOption("Cutting (-400)", -400),
    GoalOption("Manutenção", 0),
    GoalOption("Hipertrofia (+300)", 300),
    GoalOption("Força Pura (+150)", 150)
)

@Composable
fun OnboardingBiometricsScreen(
    initialProfile: UserProfile?,
    googleDisplayName: String,
    googleEmail: String,
    googlePhotoUrl: String,
    isSaving: Boolean,
    isEditMode: Boolean,
    onBack: (() -> Unit)? = null,
    onSignOut: () -> Unit,
    onSave: (
        username: String,
        displayName: String,
        photoUrl: String,
        sex: String,
        age: Int,
        weightKg: Double,
        heightCm: Double,
        waistCm: Double,
        chestCm: Double,
        armCm: Double,
        thighCm: Double,
        bodyFatPercent: Double,
        activityFactor: Double,
        goalDeltaKcal: Int,
        goalLabel: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestedUsername = remember(initialProfile, googleEmail, googleDisplayName) {
        initialProfile?.username?.takeIf { it.isNotBlank() }
            ?: googleEmail.substringBefore("@")
                .replace(Regex("[^a-zA-Z0-9_]"), "_")
                .lowercase()
                .take(24)
                .ifBlank { "atleta_apex" }
    }

    var username by remember(suggestedUsername) { mutableStateOf(suggestedUsername) }
    var displayName by remember(initialProfile, googleDisplayName) {
        mutableStateOf(initialProfile?.displayName?.ifBlank { googleDisplayName } ?: googleDisplayName.ifBlank { "Atleta" })
    }
    var sex by remember(initialProfile) { mutableStateOf(initialProfile?.sex ?: "M") }
    var ageText by remember(initialProfile) { mutableStateOf((initialProfile?.age ?: 25L).toString()) }
    var weightText by remember(initialProfile) {
        mutableStateOf(initialProfile?.weightKg?.let { formatDecimalInput(it) } ?: "")
    }
    var heightText by remember(initialProfile) {
        mutableStateOf(initialProfile?.heightCm?.let { formatDecimalInput(it) } ?: "")
    }
    var waistText by remember(initialProfile) {
        mutableStateOf(initialProfile?.waistCm?.takeIf { it > 0.0 }?.let { formatDecimalInput(it) } ?: "")
    }
    var chestText by remember(initialProfile) {
        mutableStateOf(initialProfile?.chestCm?.takeIf { it > 0.0 }?.let { formatDecimalInput(it) } ?: "")
    }
    var armText by remember(initialProfile) {
        mutableStateOf(initialProfile?.armCm?.takeIf { it > 0.0 }?.let { formatDecimalInput(it) } ?: "")
    }
    var thighText by remember(initialProfile) {
        mutableStateOf(initialProfile?.thighCm?.takeIf { it > 0.0 }?.let { formatDecimalInput(it) } ?: "")
    }
    var bfText by remember(initialProfile) {
        mutableStateOf(initialProfile?.bodyFatPercent?.takeIf { it > 0.0 }?.let { formatDecimalInput(it) } ?: "")
    }
    var activityFactor by remember(initialProfile) { mutableDoubleStateOf(initialProfile?.activityFactor ?: 1.55) }
    var goalDeltaKcal by remember(initialProfile) { mutableIntStateOf(initialProfile?.goalDeltaKcal?.toInt() ?: 300) }
    var goalLabel by remember(initialProfile) { mutableStateOf(initialProfile?.goalLabel?.ifBlank { "Hipertrofia (+300)" } ?: "Hipertrofia (+300)") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val parsedAge = ageText.toIntOrNull()?.coerceIn(12, 100) ?: 25
    val parsedWeight = weightText.replace(",", ".").toDoubleOrNull()?.coerceIn(30.0, 300.0)
    val parsedHeight = heightText.replace(",", ".").toDoubleOrNull()?.coerceIn(100.0, 250.0)

    val previewWeight = parsedWeight ?: 75.0
    val previewHeight = parsedHeight ?: 175.0
    val previewBmr = run {
        val base = (10.0 * previewWeight) + (6.25 * previewHeight) - (5.0 * parsedAge)
        if (sex == "F") (base - 161.0).roundToInt() else (base + 5.0).roundToInt()
    }
    val previewTdee = ((previewBmr * activityFactor) + goalDeltaKcal).roundToInt().coerceAtLeast(1200)
    val previewProtein = (previewWeight * if (goalDeltaKcal < 0) 2.2 else 2.0).roundToInt()
    val previewWater = (previewWeight * 35.0).roundToInt().coerceIn(1500, 6000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ApexSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditMode && onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("biometrics_back_btn")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Voltar",
                                tint = ApexOnSurface
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isEditMode) "PERFIL & MEDIDAS" else "CONFIGURAÇÃO INICIAL",
                            style = ApexTextStyles.LabelCaps,
                            color = ApexPrimary
                        )
                        Text(
                            text = if (isEditMode) "Atualizar Biometria" else "Crie seu Perfil de Atleta",
                            style = ApexTextStyles.HeadlineMd,
                            color = ApexOnSurface
                        )
                    }
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("onboarding_signout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = "Sair da conta",
                        tint = ApexError
                    )
                }
            }

            Text(
                text = if (isEditMode) {
                    "Atualize suas medidas sempre que quiser recalcular automaticamente seu TMB, metas de macros e hidratação."
                } else {
                    "Informe seu nome de usuário, peso, altura e medidas iniciais. Seu app começa 100% zerado e personalizado para você."
                },
                style = ApexTextStyles.BodySm,
                color = ApexOnSurfaceVariant
            )

            // Section 1: Identity
            SectionCard(title = "IDENTIDADE DA CONTA", icon = Icons.Rounded.Person) {
                ApexField(
                    value = username,
                    onValueChange = { username = it.replace(" ", "").take(30) },
                    label = "Nome de usuário (@username)",
                    placeholder = "ex: murilo_apex",
                    prefix = "@",
                    testTag = "input_username"
                )
                ApexField(
                    value = displayName,
                    onValueChange = { displayName = it.take(60) },
                    label = "Nome de exibição",
                    placeholder = "Como você quer ser chamado",
                    testTag = "input_display_name"
                )
            }

            // Section 2: Core Biometrics
            SectionCard(title = "DADOS BIOMÉTRICOS PRINCIPAIS", icon = Icons.Rounded.Straighten) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SexPill(
                        label = "Masculino",
                        selected = sex == "M",
                        onClick = { sex = "M" },
                        modifier = Modifier.weight(1f).testTag("sex_male_btn")
                    )
                    SexPill(
                        label = "Feminino",
                        selected = sex == "F",
                        onClick = { sex = "F" },
                        modifier = Modifier.weight(1f).testTag("sex_female_btn")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ApexField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = "Idade",
                        placeholder = "25",
                        suffix = "anos",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "input_age"
                    )
                    ApexField(
                        value = weightText,
                        onValueChange = { weightText = sanitizeDecimal(it) },
                        label = "Peso *",
                        placeholder = "78.5",
                        suffix = "kg",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_weight"
                    )
                    ApexField(
                        value = heightText,
                        onValueChange = { heightText = sanitizeDecimal(it) },
                        label = "Altura *",
                        placeholder = "178",
                        suffix = "cm",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_height"
                    )
                }
            }

            // Section 3: Optional Body Circumferences
            SectionCard(title = "MEDIDAS CORPORAIS (OPCIONAL)", icon = Icons.Rounded.Straighten) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ApexField(
                        value = waistText,
                        onValueChange = { waistText = sanitizeDecimal(it) },
                        label = "Cintura",
                        placeholder = "82",
                        suffix = "cm",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_waist"
                    )
                    ApexField(
                        value = chestText,
                        onValueChange = { chestText = sanitizeDecimal(it) },
                        label = "Peitoral",
                        placeholder = "100",
                        suffix = "cm",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_chest"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ApexField(
                        value = armText,
                        onValueChange = { armText = sanitizeDecimal(it) },
                        label = "Braço",
                        placeholder = "36",
                        suffix = "cm",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_arm"
                    )
                    ApexField(
                        value = thighText,
                        onValueChange = { thighText = sanitizeDecimal(it) },
                        label = "Coxa",
                        placeholder = "56",
                        suffix = "cm",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_thigh"
                    )
                    ApexField(
                        value = bfText,
                        onValueChange = { bfText = sanitizeDecimal(it) },
                        label = "% Gordura",
                        placeholder = "15",
                        suffix = "%",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "input_bf"
                    )
                }
            }

            // Section 4: Activity & Goal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainer)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("NÍVEL DE ATIVIDADE SEMANAL", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activityOptions.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { opt ->
                                val selected = kotlin.math.abs(activityFactor - opt.factor) < 0.01
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                                        .clickable { activityFactor = opt.factor }
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.label,
                                        style = ApexTextStyles.LabelCaps.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (selected) ApexOnPrimary else ApexOnSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("OBJETIVO PRINCIPAL", style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goalOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { opt ->
                                val selected = goalDeltaKcal == opt.deltaKcal && goalLabel == opt.label
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
                                        .clickable {
                                            goalDeltaKcal = opt.deltaKcal
                                            goalLabel = opt.label
                                        }
                                        .padding(vertical = 11.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.label,
                                        style = ApexTextStyles.LabelInteractive.copy(fontSize = 12.sp),
                                        color = if (selected) ApexOnPrimary else ApexOnSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Calculated Metabolic Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ApexSurfaceContainerHighest)
                    .border(1.dp, ApexPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewMetricCell("TMB", "${ApexViewModel.formatPtBrNumber(previewBmr)}", "kcal", ApexOnSurface, Modifier.weight(1f))
                PreviewMetricCell("META TDEE", "${ApexViewModel.formatPtBrNumber(previewTdee)}", "kcal", ApexPrimary, Modifier.weight(1f))
                PreviewMetricCell("PROTEÍNA", "${previewProtein}g", "por dia", ApexSecondary, Modifier.weight(1f))
                PreviewMetricCell("ÁGUA", "${String.format("%.1f", previewWater / 1000.0)}L", "meta", ApexPrimary, Modifier.weight(1f))
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = ApexTextStyles.BodySm,
                    color = ApexError,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Button(
                onClick = {
                    val cleanUser = username.trim().removePrefix("@")
                    val w = weightText.replace(",", ".").toDoubleOrNull()
                    val h = heightText.replace(",", ".").toDoubleOrNull()
                    val a = ageText.toIntOrNull()
                    when {
                        cleanUser.length < 3 -> validationError = "Informe um nome de usuário com pelo menos 3 caracteres."
                        a == null || a !in 12..100 -> validationError = "Informe uma idade válida entre 12 e 100 anos."
                        w == null || w !in 30.0..300.0 -> validationError = "Informe seu peso atual (entre 30 kg e 300 kg)."
                        h == null || h !in 100.0..250.0 -> validationError = "Informe sua altura em cm (entre 100 cm e 250 cm)."
                        else -> {
                            validationError = null
                            onSave(
                                cleanUser,
                                displayName.trim().ifBlank { cleanUser },
                                googlePhotoUrl,
                                sex,
                                a,
                                w,
                                h,
                                waistText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                chestText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                armText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                thighText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                bfText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                activityFactor,
                                goalDeltaKcal,
                                goalLabel
                            )
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ApexPrimaryContainer,
                    contentColor = ApexOnPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_biometrics_btn")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ApexOnPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isEditMode) "Salvar Medidas Atualizadas" else "Concluir Cadastro e Iniciar",
                            style = ApexTextStyles.LabelInteractive.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ApexSurfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = ApexPrimary, modifier = Modifier.size(16.dp))
            Text(title, style = ApexTextStyles.LabelCaps, color = ApexOnSurfaceVariant)
        }
        content()
    }
}

@Composable
private fun SexPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ApexPrimaryContainer else ApexSurfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ApexTextStyles.LabelInteractive,
            color = if (selected) ApexOnPrimary else ApexOnSurface
        )
    }
}

@Composable
private fun ApexField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    prefix: String? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp)) },
        placeholder = { Text(placeholder, color = ApexOnSurfaceVariant.copy(alpha = 0.5f)) },
        prefix = prefix?.let { { Text(it, color = ApexPrimary, fontWeight = FontWeight.Bold) } },
        suffix = suffix?.let { { Text(it, style = ApexTextStyles.BodySm.copy(fontSize = 11.sp), color = ApexOnSurfaceVariant) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ApexPrimary,
            unfocusedBorderColor = ApexOutlineVariant,
            focusedTextColor = ApexOnSurface,
            unfocusedTextColor = ApexOnSurface,
            focusedLabelColor = ApexPrimary,
            unfocusedLabelColor = ApexOnSurfaceVariant,
            focusedContainerColor = ApexSurfaceContainerLowest,
            unfocusedContainerColor = ApexSurfaceContainerLowest
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
private fun PreviewMetricCell(
    label: String,
    value: String,
    unit: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp), color = ApexOnSurfaceVariant)
        Text(
            text = value,
            style = ApexTextStyles.HeadlineSm.copy(fontWeight = FontWeight.ExtraBold),
            color = valueColor
        )
        Text(unit, style = ApexTextStyles.BodySm.copy(fontSize = 10.sp), color = ApexOnSurfaceVariant)
    }
}

private fun sanitizeDecimal(raw: String): String {
    val cleaned = raw.replace(",", ".").filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    return if (firstDot == -1) cleaned.take(5)
    else cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "").take(1)
}

private fun formatDecimalInput(v: Double): String {
    return if (v % 1.0 == 0.0) v.toInt().toString() else String.format("%.1f", v)
}
