package paboomi.practica1_compi1_1s_2026.ui.navigation

sealed class Screen(val route: String) {

    object  Editor: Screen("editor")
    object  Diagram: Screen("diagram")
}