package paboomi.practica1_compi1_1s_2026

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paboomi.practica1_compi1_1s_2026.backend.logic.ParserRunner
import paboomi.practica1_compi1_1s_2026.backend.logic.TokenData
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramResult
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramGenerator

class MainViewModel : ViewModel() {

    var code by mutableStateOf(
        "INICIO\n" +
        "  VAR a = 10\n" +
        "  VAR b = 20\n" +
        "  SI (a < b) ENTONCES\n" +
        "    MOSTRAR \"a es menor que b\"\n" +
        "  FINSI\n" +
        "  MIENTRAS (a < 15) HACER\n" +
        "    a = a + 1\n" +
        "    MOSTRAR a\n" +
        "  FINMIENTRAS\n" +
        "  MOSTRAR \"Fin del programa\"\n" +
        "FIN\n" +
        "%%%%\n" +
        "%DEFAULT=1\n" +
        "%COLOR_TEXTO_SI=12,45-5,1|1\n" +
        "%FIGURA_MIENTRAS=CIRCULO|1"
    )

    /* ── Resultado de la compilación ─────────────────────────── */

    var tokens by mutableStateOf<List<TokenData>>(emptyList())
        private set

    /** Todos los errores: léxicos + sintácticos unificados */
    var errors by mutableStateOf<List<String>>(emptyList())
        private set

    /** true si la última compilación produjo al menos un error */
    var hasErrors by mutableStateOf(false)
        private set

    var isCompiling by mutableStateOf(false)
        private set

    var hasCompiled by mutableStateOf(false)
        private set

    /** Diagrama de flujo generado a partir del código */
    var diagram by mutableStateOf<DiagramResult>(DiagramResult(emptyList(), emptyList()))
        private set

    /* ── Acción principal ────────────────────────────────────── */

    fun compile() {
        viewModelScope.launch {
            isCompiling = true

            // Limpiar caracteres invisibles antes de cualquier análisis
            val expresion = Regex("[\\u200B-\\u200D\\uFEFF\\p{C}&&[^\\n\\r\\t]]")
            val cleanCode = code.replace(expresion, "")

            // El análisis se corre en un hilo de fondo para no bloquear la UI
            val result = withContext(Dispatchers.Default) {
                ParserRunner.run(cleanCode)
            }

            tokens    = result.tokens
            errors    = result.allErrors   // léxicos + sintácticos juntos
            hasErrors = result.hasErrors
            hasCompiled = true
            isCompiling = false

            /* ── Generar diagrama ────────────────────────────────
               Si no hay errores de compilación, generar el diagrama
               de flujo a partir del código fuente.
               ──────────────────────────────────────────────────── */
            if (!hasErrors) {
                val generatedDiagram = withContext(Dispatchers.Default) {
                    DiagramGenerator.generate(cleanCode)  // usar código limpio
                }
                diagram = generatedDiagram
            } else {
                diagram = DiagramResult(emptyList(), emptyList())
            }
        }
    }
}
