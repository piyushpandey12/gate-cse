package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.ai.AiTutorScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.flashcards.FlashcardScreen
import com.example.ui.screens.flashcards.FormulaSheetScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.planner.AdminPanelScreen
import com.example.ui.screens.planner.ProfileScreen
import com.example.ui.screens.planner.SettingsScreen
import com.example.ui.screens.planner.StudyPlannerScreen
import com.example.ui.screens.practice.PracticeSessionScreen
import com.example.ui.screens.practice.PracticeSetupScreen
import com.example.ui.screens.pyq.PyqExplorerScreen
import com.example.ui.screens.resources.ResourceDirectoryScreen
import com.example.ui.screens.revision.MistakesScreen
import com.example.ui.screens.revision.RevisionScreen
import com.example.ui.screens.subjects.SubjectDetailScreen
import com.example.ui.screens.subjects.SubjectListScreen
import com.example.ui.screens.tests.ExamSimulatorScreen
import com.example.ui.screens.tests.TestsListScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GateTheme
import com.example.viewmodel.GateViewModel
import com.example.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GateViewModel by viewModels {
        val app = application as GateApplication
        ViewModelFactory(app.repository, app.aiTutorService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GateTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: GateViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()

    val currentScreenTitle = when {
        currentRoute.startsWith("dashboard") -> "GATE CSE 2027"
        currentRoute.startsWith("subjects") -> "Subject Directory"
        currentRoute.startsWith("subject_detail") -> "Subject Curriculum"
        currentRoute.startsWith("practice_setup") -> "Practice Engine"
        currentRoute.startsWith("practice_session") -> "Active Practice Drill"
        currentRoute.startsWith("pyqs") -> "PYQ Explorer (1987-2026)"
        currentRoute.startsWith("tests") -> "Exam Simulator"
        currentRoute.startsWith("exam_simulator") -> "CBT Exam Mode"
        currentRoute.startsWith("revision") -> "Spaced Repetition (SRS)"
        currentRoute.startsWith("mistakes") -> "Dedicated Error Notebook"
        currentRoute.startsWith("resources") -> "Curated Resource Directory"
        currentRoute.startsWith("flashcards") -> "Active Recall Flashcards"
        currentRoute.startsWith("formulas") -> "Formula Compendium"
        currentRoute.startsWith("notes") -> "Engineering Notes"
        currentRoute.startsWith("ai_tutor") -> "AI Tutor Assistant"
        currentRoute.startsWith("analytics") -> "Performance Analytics"
        currentRoute.startsWith("planner") -> "Daily Study Planner"
        currentRoute.startsWith("settings") -> "Application Settings"
        currentRoute.startsWith("admin") -> "Diagnostics & Admin"
        currentRoute.startsWith("profile") -> "Candidate Profile"
        currentRoute.startsWith("more") -> "Feature Hub"
        else -> "GATE CSE 2027"
    }

    // Hide Bottom Nav and Top Bar on fullscreen Exam Simulator to simulate real CBT environment
    val isExamRunning = currentRoute.startsWith("exam_simulator")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isExamRunning,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(295.dp)
            ) {
                DrawerContent(
                    currentRoute = currentRoute,
                    userRole = userProfile?.role,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isExamRunning) {
                    AppTopBar(
                        title = currentScreenTitle,
                        streakDays = userProfile?.currentStreak ?: 1,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAiClick = { navController.navigate(Screen.AiTutor.route) },
                        onMoreClick = { navController.navigate(Screen.More.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }
            },
            bottomBar = {
                if (!isExamRunning) {
                    AppBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Subjects.route) {
                    SubjectListScreen(
                        viewModel = viewModel,
                        onSelectSubject = { subId ->
                            navController.navigate(Screen.SubjectDetail.createRoute(subId))
                        },
                        onPracticeSubject = { subId ->
                            navController.navigate(Screen.PracticeSession.createRoute(subId, 10))
                        }
                    )
                }

                composable(
                    route = Screen.SubjectDetail.route,
                    arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
                ) { backStack ->
                    val subjectId = backStack.arguments?.getString("subjectId") ?: "OS"
                    SubjectDetailScreen(
                        subjectId = subjectId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onStartTopicPractice = { topId ->
                            navController.navigate(Screen.PracticeSession.createRoute(topId, 10))
                        },
                        onNavigateResources = {
                            navController.navigate(Screen.Resources.route)
                        }
                    )
                }

                composable(Screen.PracticeSetup.route) {
                    PracticeSetupScreen(
                        viewModel = viewModel,
                        onStartPractice = { topicId, count ->
                            navController.navigate(Screen.PracticeSession.createRoute(topicId, count))
                        }
                    )
                }

                composable(
                    route = Screen.PracticeSession.route,
                    arguments = listOf(
                        navArgument("topicId") { type = NavType.StringType },
                        navArgument("count") { type = NavType.IntType }
                    )
                ) { backStack ->
                    val topicId = backStack.arguments?.getString("topicId") ?: "ALL"
                    val count = backStack.arguments?.getInt("count") ?: 10
                    PracticeSessionScreen(
                        topicId = topicId,
                        questionCount = count,
                        viewModel = viewModel,
                        onFinishSession = { navController.navigate(Screen.Dashboard.route) },
                        onAskAiQuestion = { qId ->
                            navController.navigate(Screen.AiTutor.route)
                        }
                    )
                }

                composable(Screen.PyqExplorer.route) {
                    PyqExplorerScreen(
                        viewModel = viewModel,
                        onAskAiQuestion = { qId ->
                            navController.navigate(Screen.AiTutor.route)
                        }
                    )
                }

                composable(Screen.TestsList.route) {
                    TestsListScreen(
                        viewModel = viewModel,
                        onLaunchExam = { testType ->
                            navController.navigate(Screen.ExamSimulator.createRoute(testType))
                        }
                    )
                }

                composable(
                    route = Screen.ExamSimulator.route,
                    arguments = listOf(navArgument("testType") { type = NavType.StringType })
                ) { backStack ->
                    val testType = backStack.arguments?.getString("testType") ?: "FULL_MOCK"
                    ExamSimulatorScreen(
                        testType = testType,
                        viewModel = viewModel,
                        onTestSubmitted = {
                            navController.navigate(Screen.Dashboard.route)
                        }
                    )
                }

                composable(Screen.Revision.route) {
                    RevisionScreen(
                        viewModel = viewModel,
                        onPracticeQuestion = { qId ->
                            navController.navigate(Screen.PracticeSession.createRoute(qId, 5))
                        }
                    )
                }

                composable(Screen.Mistakes.route) {
                    MistakesScreen(
                        viewModel = viewModel,
                        onNavigateToPractice = { topicId, count ->
                            navController.navigate(Screen.PracticeSession.createRoute(topicId, count))
                        },
                        onAskAi = { qId ->
                            navController.navigate(Screen.AiTutor.route)
                        }
                    )
                }

                composable(Screen.Resources.route) {
                    ResourceDirectoryScreen(viewModel = viewModel)
                }

                composable(Screen.Flashcards.route) {
                    FlashcardScreen(viewModel = viewModel)
                }

                composable(Screen.Formulas.route) {
                    FormulaSheetScreen(viewModel = viewModel)
                }

                composable(Screen.Notes.route) {
                    NotesScreen(viewModel = viewModel)
                }

                composable(Screen.AiTutor.route) {
                    AiTutorScreen(viewModel = viewModel)
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }

                composable(Screen.Planner.route) {
                    StudyPlannerScreen(viewModel = viewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Admin.route) {
                    AdminPanelScreen(viewModel = viewModel)
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel)
                }

                composable(Screen.More.route) {
                    MoreScreen(
                        userRole = userProfile?.role,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
            }
        }
    }
}


@Composable
fun DrawerContent(
    currentRoute: String,
    userRole: String? = null,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "27",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GATE CSE 2027",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Offline-First CBT Prep",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 1. MAIN
        item {
            DrawerSectionHeader("MAIN NAVIGATION")
        }

        val primaryNav = listOf(
            Triple(Screen.Dashboard.route, "Home", Icons.Default.Home),
            Triple(Screen.PracticeSetup.route, "Practice Engine", Icons.Default.PlayCircleOutline),
            Triple(Screen.PyqExplorer.route, "PYQ Archive", Icons.Default.HistoryEdu),
            Triple(Screen.Revision.route, "Spaced Revision", Icons.Default.SyncProblem),
            Triple(Screen.More.route, "More Features Hub", Icons.Default.GridView)
        )

        items(primaryNav) { (route, label, icon) ->
            DrawerItemRow(
                label = label,
                icon = icon,
                isSelected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }

        // 2. LEARNING & RETENTION
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DrawerSectionHeader("LEARNING & RETENTION")
        }

        val learningNav = listOf(
            Triple(Screen.Revision.route, "Spaced Repetition (SRS)", Icons.Default.Repeat),
            Triple(Screen.Mistakes.route, "Error Notebook", Icons.Default.ErrorOutline),
            Triple(Screen.Flashcards.route, "SRS Flashcards", Icons.Default.Style),
            Triple(Screen.Formulas.route, "Formula Compendium", Icons.Default.Functions),
            Triple(Screen.Notes.route, "Engineering Notes", Icons.Default.EditNote),
            Triple(Screen.Resources.route, "Curated Resources", Icons.AutoMirrored.Filled.LibraryBooks)
        )

        items(learningNav) { (route, label, icon) ->
            DrawerItemRow(
                label = label,
                icon = icon,
                isSelected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }

        // 3. INTELLIGENCE & METRICS
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DrawerSectionHeader("INTELLIGENCE & METRICS")
        }

        val intelligenceNav = listOf(
            Triple(Screen.AiTutor.route, "AI Tutor Assistant", Icons.Default.SmartToy),
            Triple(Screen.Analytics.route, "Performance Analytics", Icons.Default.BarChart),
            Triple(Screen.Planner.route, "Study Planner", Icons.AutoMirrored.Filled.EventNote)
        )

        items(intelligenceNav) { (route, label, icon) ->
            DrawerItemRow(
                label = label,
                icon = icon,
                isSelected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }

        // 4. ACCOUNT & SYSTEM
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DrawerSectionHeader("ACCOUNT & SYSTEM")
        }

        val accountNav = mutableListOf(
            Triple(Screen.Settings.route, "Settings", Icons.Default.Settings),
            Triple(Screen.Profile.route, "Candidate Profile", Icons.Default.Person)
        )
        if (userRole?.uppercase() == "ADMIN") {
            accountNav.add(Triple(Screen.Admin.route, "Diagnostics & Admin", Icons.Default.AdminPanelSettings))
        }

        items(accountNav) { (route, label, icon) ->
            DrawerItemRow(
                label = label,
                icon = icon,
                isSelected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            fontSize = 10.sp
        ),
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun DrawerItemRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("drawer_item_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            )
        }
    }
}
