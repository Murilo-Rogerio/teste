package com.example

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.ApexRepository
import com.example.data.authStateFlow
import com.example.data.configureAppCheck
import com.example.data.signOutUser
import com.example.ui.components.ApexBottomNavigationBar
import com.example.ui.components.ApexToastBanner
import com.example.ui.components.ApexTopBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.IaCoachScreen
import com.example.ui.screens.NutricaoScreen
import com.example.ui.screens.OnboardingBiometricsScreen
import com.example.ui.screens.ProgressoScreen
import com.example.ui.screens.TreinoAtivoScreen
import com.example.ui.screens.TreinosScreen
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnPrimary
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexPerformanceTheme
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerLow
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexTab
import com.example.viewmodel.ApexViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1 (MANDATORY): Configure Firebase App Check with launch intent before UI or ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            ApexPerformanceTheme {
                AppNavigationRoot()
            }
        }
    }
}

@Composable
fun AppNavigationRoot() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        // Key composition & ViewModel by UID so account switch/sign-out tears down Firestore listeners cleanly
        key(user.uid) {
            AuthenticatedApexShell(firebaseUser = user)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedApexShell(
    firebaseUser: FirebaseUser
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val uid = firebaseUser.uid

    val viewModel: ApexViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ApexViewModel(ApexRepository(db), uid)
            }
        }
    )

    // Network connectivity observer
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        fun checkOnline(): Boolean {
            val net = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        viewModel.setOfflineStatus(!checkOnline())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { viewModel.setOfflineStatus(false) }
            override fun onLost(network: Network) { viewModel.setOfflineStatus(!checkOnline()) }
        }
        runCatching { cm?.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { cm?.unregisterNetworkCallback(callback) } }
    }

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val routinesState by viewModel.routinesState.collectAsStateWithLifecycle()
    val activeSetsState by viewModel.activeSetsState.collectAsStateWithLifecycle()
    val workoutLogsState by viewModel.workoutLogsState.collectAsStateWithLifecycle()
    val mealsState by viewModel.mealsState.collectAsStateWithLifecycle()
    val weightLogsState by viewModel.weightLogsState.collectAsStateWithLifecycle()

    val isSavingProfile by viewModel.isSavingProfile.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    var showEditBiometrics by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }

    val handleSignOut: () -> Unit = {
        showAccountSheet = false
        showEditBiometrics = false
        signOutUser(credentialManager = credentialManager, scope = scope)
    }

    when (val pState = profileState) {
        UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(ApexSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = ApexPrimary)
                    Text("Carregando sua conta Apex...", style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant)
                }
            }
            return
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(ApexSurface).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = ApexError, modifier = Modifier.size(40.dp))
                    Text("Erro ao sincronizar perfil", style = ApexTextStyles.HeadlineSm, color = ApexOnSurface)
                    Text(pState.message, style = ApexTextStyles.BodySm, color = ApexOnSurfaceVariant, textAlign = TextAlign.Center)
                    Button(
                        onClick = handleSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = ApexPrimaryContainer, contentColor = ApexOnPrimary)
                    ) { Text("Sair e tentar novamente") }
                }
            }
            return
        }

        is UiState.Success -> {
            val profile = pState.data

            // Mandatory onboarding for new accounts OR explicit edit biometrics mode
            if (profile == null || !profile.onboardingCompleted || showEditBiometrics) {
                BackHandler(enabled = showEditBiometrics && profile != null && profile.onboardingCompleted) {
                    showEditBiometrics = false
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    OnboardingBiometricsScreen(
                        initialProfile = profile,
                        googleDisplayName = firebaseUser.displayName.orEmpty(),
                        googleEmail = firebaseUser.email.orEmpty(),
                        googlePhotoUrl = firebaseUser.photoUrl?.toString().orEmpty(),
                        isSaving = isSavingProfile,
                        isEditMode = showEditBiometrics && profile != null && profile.onboardingCompleted,
                        onBack = if (showEditBiometrics) { { showEditBiometrics = false } } else null,
                        onSignOut = handleSignOut,
                        onSave = { username, displayName, photoUrl, sex, age, weight, height, waist, chest, arm, thigh, bf, activity, goalDelta, goalLabel ->
                            viewModel.saveUserBiometrics(
                                username = username,
                                displayName = displayName,
                                photoUrl = photoUrl,
                                sex = sex,
                                age = age,
                                weightKg = weight,
                                heightCm = height,
                                waistCm = waist,
                                chestCm = chest,
                                armCm = arm,
                                thighCm = thigh,
                                bodyFatPercent = bf,
                                activityFactor = activity,
                                goalDeltaKcal = goalDelta,
                                goalLabel = goalLabel,
                                onSaved = { showEditBiometrics = false }
                            )
                        }
                    )
                    ApexToastBanner(
                        message = toastMessage,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp)
                    )
                }
                return
            }

            // Authenticated & onboarded main 5-tab application
            val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
            val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
            val selectedFilter by viewModel.selectedSplitFilter.collectAsStateWithLifecycle()
            val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
            val restRemainingSeconds by viewModel.restRemainingSeconds.collectAsStateWithLifecycle()
            val restTotalPresetSeconds by viewModel.restTotalPresetSeconds.collectAsStateWithLifecycle()
            val timerSoundEnabled by viewModel.timerSoundEnabled.collectAsStateWithLifecycle()
            val coachSubView by viewModel.coachSubView.collectAsStateWithLifecycle()
            val coachMode by viewModel.coachMode.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
            val plateauScan by viewModel.plateauScan.collectAsStateWithLifecycle()
            val mealScanState by viewModel.mealScanState.collectAsStateWithLifecycle()

            val routines = (routinesState as? UiState.Success)?.data.orEmpty()
            val activeSets = (activeSetsState as? UiState.Success)?.data.orEmpty()
            val workoutLogs = (workoutLogsState as? UiState.Success)?.data.orEmpty()
            val meals = (mealsState as? UiState.Success)?.data.orEmpty()
            val weightLogs = (weightLogsState as? UiState.Success)?.data.orEmpty()

            BackHandler(enabled = selectedTab != ApexTab.TREINOS) {
                viewModel.selectTab(ApexTab.TREINOS)
            }

            Box(
                modifier = Modifier.fillMaxSize().background(ApexSurface)
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    containerColor = ApexSurface,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        ApexTopBar(
                            subtitle = selectedTab.headerSubtitle,
                            username = profile.username,
                            photoUrl = profile.photoUrl,
                            level = profile.level.toInt(),
                            streakDays = profile.streakDays.toInt(),
                            isOffline = isOffline,
                            onBiometricsClick = { showEditBiometrics = true },
                            onProfileClick = { showAccountSheet = true }
                        )
                    },
                    bottomBar = {
                        ApexBottomNavigationBar(
                            selectedTab = selectedTab,
                            hasActiveSession = activeSets.isNotEmpty(),
                            onSelectTab = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
                            when (selectedTab) {
                                ApexTab.TREINOS -> TreinosScreen(
                                    profile = profile,
                                    routines = routines,
                                    selectedFilter = selectedFilter,
                                    exerciseLibrary = viewModel.exerciseLibrary,
                                    onSelectFilter = viewModel::selectSplitFilter,
                                    onOpenBiometrics = { showEditBiometrics = true },
                                    onSaveNewRoutine = viewModel::saveNewRoutine,
                                    onToggleFavorite = viewModel::toggleRoutineFavorite,
                                    onDeleteRoutine = viewModel::deleteRoutine,
                                    onStartWorkout = viewModel::startWorkoutFromRoutine,
                                    onStartFreeExercise = viewModel::startFreeWorkoutWithExercise
                                )

                                ApexTab.ATIVO -> TreinoAtivoScreen(
                                    profile = profile,
                                    routines = routines,
                                    sets = activeSets,
                                    elapsedSeconds = elapsedSeconds,
                                    restRemainingSeconds = restRemainingSeconds,
                                    restTotalPresetSeconds = restTotalPresetSeconds,
                                    soundEnabled = timerSoundEnabled,
                                    onSelectRestPreset = viewModel::selectRestPreset,
                                    onAdd30Seconds = viewModel::add30SecondsRest,
                                    onSkipRest = viewModel::skipRestTimer,
                                    onToggleSound = viewModel::toggleTimerSound,
                                    onAdjustWeight = viewModel::adjustSetWeight,
                                    onAdjustReps = viewModel::adjustSetReps,
                                    onCycleRpe = viewModel::cycleSetRpe,
                                    onToggleSetCompleted = viewModel::toggleSetCompleted,
                                    onAddSet = viewModel::addActiveSetRow,
                                    onDeleteSet = viewModel::deleteActiveSetRow,
                                    onUpdateNotes = viewModel::updateActiveExerciseAndNotes,
                                    onFinishWorkout = viewModel::finishActiveWorkout,
                                    onStartRoutine = viewModel::startWorkoutFromRoutine,
                                    onStartFreeExercise = viewModel::startFreeWorkoutWithExercise,
                                    onNavigateToRoutines = { viewModel.selectTab(ApexTab.TREINOS) }
                                )

                                ApexTab.IA_COACH -> IaCoachScreen(
                                    profile = profile,
                                    workoutLogs = workoutLogs,
                                    subView = coachSubView,
                                    selectedMode = coachMode,
                                    messages = chatMessages,
                                    isGenerating = isAiGenerating,
                                    plateauScan = plateauScan,
                                    onSelectSubView = viewModel::selectCoachSubView,
                                    onSelectMode = viewModel::selectCoachMode,
                                    onSendMessage = viewModel::sendCoachMessage,
                                    onRunPlateauScan = viewModel::runPlateauScan,
                                    onShowToast = viewModel::showToast
                                )

                                ApexTab.NUTRICAO -> NutricaoScreen(
                                    profile = profile,
                                    meals = meals,
                                    mealScanState = mealScanState,
                                    onToggleMeal = viewModel::toggleMealCompleted,
                                    onAddCustomMeal = viewModel::addCustomMeal,
                                    onDeleteMeal = viewModel::deleteMeal,
                                    onScanMealWithAi = viewModel::scanMealWithGemini,
                                    onResetMealScan = viewModel::resetMealScanState,
                                    onAddWater = viewModel::addWater,
                                    onOpenBiometrics = { showEditBiometrics = true }
                                )

                                ApexTab.PROGRESSO -> ProgressoScreen(
                                    profile = profile,
                                    logs = workoutLogs,
                                    weightLogs = weightLogs,
                                    onRecordBodyMeasurement = viewModel::recordBodyMeasurement,
                                    onBuildExportPayload = viewModel::buildExportPayload,
                                    onShowToast = viewModel::showToast
                                )
                            }
                        }
                    }
                }

                ApexToastBanner(
                    message = toastMessage,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 98.dp)
                )
            }

            if (showAccountSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAccountSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = ApexSurfaceContainerLow,
                    contentColor = ApexOnSurface
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("CONTA DO ATLETA", style = ApexTextStyles.LabelCaps, color = ApexPrimary)
                        Text(
                            text = "${profile.displayName} (@${profile.username})",
                            style = ApexTextStyles.HeadlineMd,
                            color = ApexOnSurface
                        )
                        Text(
                            text = firebaseUser.email.orEmpty(),
                            style = ApexTextStyles.BodySm,
                            color = ApexOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                showAccountSheet = false
                                showEditBiometrics = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexSurfaceContainerHigh,
                                contentColor = ApexOnSurface
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("account_edit_biometrics_btn")
                        ) {
                            Icon(Icons.Rounded.Straighten, contentDescription = null, tint = ApexPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Medidas, Peso e Objetivo", style = ApexTextStyles.LabelInteractive)
                        }
                        Button(
                            onClick = handleSignOut,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ApexError.copy(alpha = 0.16f),
                                contentColor = ApexError
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("account_sign_out_btn")
                        ) {
                            Icon(Icons.Rounded.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sair da Conta Google", style = ApexTextStyles.LabelInteractive.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}
