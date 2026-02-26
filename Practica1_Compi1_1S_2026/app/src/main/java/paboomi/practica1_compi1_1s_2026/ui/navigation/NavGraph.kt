package paboomi.practica1_compi1_1s_2026.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import paboomi.practica1_compi1_1s_2026.MainViewModel
import paboomi.practica1_compi1_1s_2026.ui.screens.DiagramScreen
import paboomi.practica1_compi1_1s_2026.ui.screens.EditorScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Editor.route) {
        composable(Screen.Editor.route) {
            EditorScreen(
                code = viewModel.code,
                onCodeChange = { viewModel.code = it },
                onNavigateToDiagram = {
                    viewModel.compile()
                    navController.navigate(Screen.Diagram.route)
                }
            )
        }
        composable(Screen.Diagram.route) {
            DiagramScreen(
                tokens = viewModel.tokens,
                errors = viewModel.errors,
                diagram = viewModel.diagram,
                hasErrors = viewModel.hasErrors,
                isCompiling = viewModel.isCompiling,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
