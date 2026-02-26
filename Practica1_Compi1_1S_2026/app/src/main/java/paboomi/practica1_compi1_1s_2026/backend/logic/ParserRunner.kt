package paboomi.practica1_compi1_1s_2026.backend.logic

import java_cup.runtime.DefaultSymbolFactory
import paboomi.practica1_compi1_1s_2026.backend.analyzer.Lexer
import paboomi.practica1_compi1_1s_2026.backend.analyzer.Parser
import paboomi.practica1_compi1_1s_2026.backend.analyzer.sym
import java.io.StringReader

/**
 * Resultado completo de la compilación:
 * - tokens   : lista de tokens reconocidos por el Lexer
 * - lexErrors: errores léxicos (caracteres no reconocidos)
 * - synErrors: errores sintácticos detectados por el Parser
 *
 * Si alguna de las dos listas de errores no está vacía, el
 * código tiene errores y el diagrama NO debe mostrarse.
 */
data class CompileResult(
    val tokens: List<TokenData>,
    val lexErrors: List<String>,
    val synErrors: List<String>
) {
    /** true si hay al menos un error léxico o sintáctico */
    val hasErrors: Boolean get() = lexErrors.isNotEmpty() || synErrors.isNotEmpty()

    /** Lista unificada de todos los errores (léxicos primero, luego sintácticos) */
    val allErrors: List<String> get() = lexErrors + synErrors
}

/**
 * Experto en ejecutar el análisis léxico + sintáctico.
 * Patrón Experto: sabe cómo combinar Lexer y Parser, y cómo
 * recolectar los errores de ambas fases en un solo resultado.
 */
object ParserRunner {

    fun run(code: String): CompileResult {

        /* ── Fase 1: Lexer ──────────────────────────────────────
           Recorremos todos los tokens ANTES de pasarlos al parser
           para construir la tabla visible en la UI.
           ──────────────────────────────────────────────────────── */
        val tokens = mutableListOf<TokenData>()
        val lexErrors = mutableListOf<String>()

        val lexerForTokens = Lexer(StringReader(code))
        try {
            var symbol = lexerForTokens.next_token()
            while (symbol.sym != sym.EOF) {
                val name = if (symbol.sym in sym.terminalNames.indices)
                    sym.terminalNames[symbol.sym]
                else
                    "UNKNOWN(${symbol.sym})"

                // Los terminales con valor variable (ID, CADENA, ENTERO, DECIMAL, HEX)
                // traen su texto en symbol.value; el resto se muestra vacío.
                val value = symbol.value?.toString() ?: ""

                tokens.add(TokenData(name, value, symbol.left, symbol.right))
                symbol = lexerForTokens.next_token()
            }
        } catch (e: Exception) {
            lexErrors.add("Error interno del lexer: ${e.message}")
        }

        // Agrega errores léxicos registrados por el propio Lexer
        lexErrors.addAll(lexerForTokens.lexicalErrors ?: emptyList())

        /* ── Fase 2: Parser ─────────────────────────────────────
           Usamos un segundo StringReader (el Lexer es stateful,
           no se puede rebobinar) para el análisis sintáctico.
           El parser se declara fuera del try para poder consultar
           su lista de errores incluso si lanza una excepción.
           ──────────────────────────────────────────────────────── */
        val synErrors = mutableListOf<String>()

        // Parser declarado fuera del try para ser accesible en catch.
        // Se usa DefaultSymbolFactory porque el Lexer JFlex (%cup) genera Symbol
        // planos — ComplexSymbolFactory causaría ClassCastException al intentar
        // castear Symbol → ComplexSymbol al construir nodos del árbol.
        val lexerForParser = Lexer(StringReader(code))
        val parser = Parser(lexerForParser, DefaultSymbolFactory())

        try {
            // parse() recorre el árbol sintáctico; los errores sintácticos
            // se acumulan en parser.syntaxErrors (ver Parser.java modificado).
            parser.parse()
        } catch (e: Exception) {
            // unrecovered_syntax_error lanza excepción DESPUÉS de añadir
            // el mensaje a la lista; aquí sólo detenemos la propagación.
            // Si es un error inesperado de otra causa, también lo reportamos.
            if (e.message != "Error sintáctico irrecuperable") {
                synErrors.add("Error inesperado del parser: ${e.message}")
            }
        }
        // Siempre recogemos los errores que el parser pudo acumular
        synErrors.addAll(parser.syntaxErrors)

        return CompileResult(
            tokens = tokens,
            lexErrors = lexErrors,
            synErrors = synErrors
        )
    }
}
