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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.configureAppCheck
import com.example.ui.components.ApexBottomNavigationBar
import com.example.ui.components.ApexToastBanner
import com.example.ui.components.ApexTopBar
import com.example.ui.screens.IaCoachScreen
import com.example.ui.screens.NutricaoScreen
import com.example.ui.screens.ProgressoScreen
import com.example.ui.screens.TreinoAtivoScreen
import com.example.ui.screens.TreinosScreen
import com.example.ui.theme.ApexPerformanceTheme
import com.example.ui.theme.ApexSurface
import com.example.viewmodel.ApexTab
import com.example.viewmodel.ApexViewModel

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
                ApexPerformanceApp()
            }
        }
    }
}

@Composable
fun ApexPerformanceApp(
    viewModel: ApexViewModel = viewModel()
) {
    val context = LocalContext.current

    // Monitor real Android network connectivity for the header OFFLINE indicator
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        fun checkOnline(): Boolean {
            val net = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        viewModel.setOfflineStatus(!checkOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModel.setOfflineStatus(false)
            }
            override fun onLost(network: Network) {
                viewModel.setOfflineStatus(!checkOnline())
            }
        }
        runCatching { cm?.registerDefaultNetworkCallback(callback) }
        onDispose {
            runCatching { cm?.unregisterNetworkCallback(callback) }
        }
    }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val activeSets by viewModel.activeSets.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()

    val showPwaBanner by viewModel.showPwaBanner.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedSplitFilter.collectAsStateWithLifecycle()

    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val totalVolumeKg by viewModel.baseSessionVolumeKg.collectAsStateWithLifecycle()
    val restRemainingSeconds by viewModel.restRemainingSeconds.collectAsStateWithLifecycle()
    val restTotalPresetSeconds by viewModel.restTotalPresetSeconds.collectAsStateWithLifecycle()
    val timerSoundEnabled by viewModel.timerSoundEnabled.collectAsStateWithLifecycle()
    val showPrBanner by viewModel.showPrBanner.collectAsStateWithLifecycle()

    val coachSubView by viewModel.coachSubView.collectAsStateWithLifecycle()
    val coachMode by viewModel.coachMode.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
    val isWeight80Applied by viewModel.isWeight80Applied.collectAsStateWithLifecycle()
    val plateauScan by viewModel.plateauScan.collectAsStateWithLifecycle()
    val mealScanState by viewModel.mealScanState.collectAsStateWithLifecycle()

    // Return to primary Treinos tab on system Back when on secondary tabs
    BackHandler(enabled = selectedTab != ApexTab.TREINOS) {
        viewModel.selectTab(ApexTab.TREINOS)
    }

    val activeSet = activeSets.firstOrNull { it.isActive } ?: activeSets.lastOrNull()
    val activeTelemetryLine = "Supino Reto ${activeSet?.weightKg ?: 84}kg × ${activeSet?.reps ?: 8} reps | RPE ${String.format("%.1f", activeSet?.rpe ?: 8.5)}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexSurface)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = ApexSurface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                ApexTopBar(
                    subtitle = selectedTab.headerSubtitle,
                    level = stats.level,
                    streakDays = stats.headerStreakDays,
                    isOffline = isOffline,
                    onInstallClick = { viewModel.triggerInstallSimulation() },
                    onProfileClick = { viewModel.selectTab(ApexTab.PROGRESSO) }
                )
            },
            bottomBar = {
                ApexBottomNavigationBar(
                    selectedTab = selectedTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                ) {
                    when (selectedTab) {
                        ApexTab.TREINOS -> TreinosScreen(
                            routines = routines,
                            stats = stats,
                            showPwaBanner = showPwaBanner,
                            installState = installState,
                            selectedFilter = selectedFilter,
                            exerciseLibrary = viewModel.exerciseLibrary,
                            onDismissPwaBanner = viewModel::dismissPwaBanner,
                            onInstallClick = viewModel::triggerInstallSimulation,
                            onSelectFilter = viewModel::selectSplitFilter,
                            onImportClick = {
                                viewModel.showToast("Conector Apex pronto para planilhas e apps externos")
                            },
                            onSaveNewRoutine = viewModel::saveNewRoutine,
                            onStartWorkout = viewModel::startWorkoutSession,
                            onExerciseAdded = { name -> viewModel.showToast("$name adicionado!") }
                        )

                        ApexTab.ATIVO -> TreinoAtivoScreen(
                            stats = stats,
                            sets = activeSets,
                            elapsedSeconds = elapsedSeconds,
                            totalVolumeKg = totalVolumeKg,
                            restRemainingSeconds = restRemainingSeconds,
                            restTotalPresetSeconds = restTotalPresetSeconds,
                            soundEnabled = timerSoundEnabled,
                            showPrBanner = showPrBanner,
                            onSelectRestPreset = viewModel::selectRestPreset,
                            onAdd30Seconds = viewModel::add30SecondsRest,
                            onSkipRest = viewModel::skipRestTimer,
                            onToggleSound = viewModel::toggleTimerSound,
                            onDismissPrBanner = viewModel::dismissPrBanner,
                            onAdjustWeight = viewModel::adjustSetWeight,
                            onAdjustReps = viewModel::adjustSetReps,
                            onCycleRpe = viewModel::cycleSetRpe,
                            onToggleSetCompleted = viewModel::toggleSetCompleted,
                            onAddSet = viewModel::addActiveSetRow,
                            onUpdateNotes = viewModel::updateExerciseNotes,
                            onFinishWorkout = viewModel::finishActiveWorkout
                        )

                        ApexTab.IA_COACH -> IaCoachScreen(
                            subView = coachSubView,
                            selectedMode = coachMode,
                            messages = chatMessages,
                            isGenerating = isAiGenerating,
                            isWeight80Applied = isWeight80Applied,
                            plateauScan = plateauScan,
                            activeTelemetryLine = activeTelemetryLine,
                            onSelectSubView = viewModel::selectCoachSubView,
                            onSelectMode = viewModel::selectCoachMode,
                            onApply80Kg = viewModel::apply80KgRecommendation,
                            onSendMessage = viewModel::sendCoachMessage,
                            onRunPlateauScan = viewModel::runPlateauScan,
                            onShowToast = viewModel::showToast
                        )

                        ApexTab.NUTRICAO -> NutricaoScreen(
                            meals = meals,
                            stats = stats,
                            mealScanState = mealScanState,
                            onToggleMeal = viewModel::toggleMealCompleted,
                            onAddCustomMeal = viewModel::addCustomMeal,
                            onScanMealWithAi = viewModel::scanMealWithGemini,
                            onResetMealScan = viewModel::resetMealScanState,
                            onAddWater = viewModel::addWater
                        )

                        ApexTab.PROGRESSO -> ProgressoScreen(
                            stats = stats,
                            logs = workoutLogs,
                            onBuildExportPayload = viewModel::buildExportPayload,
                            onShowToast = viewModel::showToast
                        )
                    }
                }
            }
        }

        // Global Floating Toast Pill
        ApexToastBanner(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 98.dp)
        )
    }
}
