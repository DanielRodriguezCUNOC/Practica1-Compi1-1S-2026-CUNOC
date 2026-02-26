package paboomi.practica1_compi1_1s_2026.backend.analyzer

import java.io.StringReader

data class LexerResult(
    val tokens: List<TokenData>,
    val errors: List<String>
)

object LexerRunner {

    fun run(code: String): LexerResult {
        val reader = StringReader(code)
        val lexer = Lexer(reader)
        val tokens = mutableListOf<TokenData>()
        val errors = mutableListOf<String>()

        try {
            var symbol = lexer.next_token()
            while (symbol.sym != sym.EOF) {
                val name = if (symbol.sym in sym.terminalNames.indices)
                    sym.terminalNames[symbol.sym]
                else
                    "UNKNOWN(${symbol.sym})"

                // Los tokens con valor llevan el texto original; los demás muestran "-"
                val value = symbol.value?.toString() ?: "-"

                tokens.add(
                    TokenData(
                        type = name,
                        value = value,
                        line = symbol.left,
                        column = symbol.right
                    )
                )
                symbol = lexer.next_token()
            }
        } catch (e: Exception) {
            errors.add("Error interno del lexer: ${e.message}")
        }

        // Agregar errores léxicos registrados dentro del lexer
        errors.addAll(lexer.lexicalErrors ?: emptyList())

        return LexerResult(tokens = tokens, errors = errors)
    }
}
