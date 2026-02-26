package paboomi.practica1_compi1_1s_2026.backend.analyzer

data class TokenData(
    val type: String,
    val value: String,
    val line: Int,
    val column: Int
)
