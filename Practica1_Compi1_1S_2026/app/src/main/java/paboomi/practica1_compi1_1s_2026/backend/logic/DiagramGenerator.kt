package paboomi.practica1_compi1_1s_2026.backend.logic

import androidx.compose.ui.unit.dp

/**
 * Patrón Experto: DiagramGenerator es el responsable de convertir
 * pseudocódigo en nodos y conexiones de diagrama de flujo.
 *
 * Características:
 * - Parsea código línea por línea
 * - Maneja ramificaciones (SI/MIENTRAS) con caminos Sí/No
 * - Para bucles MIENTRAS: genera línea de regreso a la decisión
 */
object DiagramGenerator {

    /**
     * Genera un diagrama de flujo a partir del código pseudocódigo.
     * Retorna los nodos y conexiones necesarios para renderizar.
     */
    fun generate(code: String): DiagramResult {
        // Separar sección algoritmo y sección de configuración
        val sepIdx = code.indexOf("%%%%")
        val algoCode   = if (sepIdx != -1) code.substring(0, sepIdx) else code
        val configCode = if (sepIdx != -1) code.substring(sepIdx + 4) else ""
        val config = parseConfig(configCode)

        val nodes = mutableListOf<DiagramNode>()
        val connections = mutableListOf<DiagramConnection>()
        val symbols = mutableListOf<SymbolData>()
        val controlStructures = mutableListOf<ControlStructure>()

        // Extraer tabla de símbolos y estructuras de control con números de línea reales
        algoCode.split("\n").forEachIndexed { idx, rawLine ->
            val line = rawLine.trim()
            val lineNum = idx + 1
            when {
                line.startsWith("VAR", ignoreCase = true) -> {
                    val rest = line.removePrefix("VAR").removePrefix("var").trim()
                    if (rest.contains("=")) {
                        val eqIdx = rest.indexOf("=")
                        val name  = rest.substring(0, eqIdx).trim()
                        val value = rest.substring(eqIdx + 1).trim()
                        if (symbols.none { it.name == name })
                            symbols.add(SymbolData(name, value, lineNum))
                    } else if (rest.isNotEmpty()) {
                        if (symbols.none { it.name == rest })
                            symbols.add(SymbolData(rest, "-", lineNum))
                    }
                }
                // Detección de SI
                line.startsWith("SI", ignoreCase = true) &&
                !line.startsWith("FINSI", ignoreCase = true) -> {
                    controlStructures.add(ControlStructure("SI", lineNum, extractCondition(line)))
                }
                // Detección de MIENTRAS
                line.startsWith("MIENTRAS", ignoreCase = true) &&
                !line.startsWith("FINMIENTRAS", ignoreCase = true) -> {
                    controlStructures.add(ControlStructure("MIENTRAS", lineNum, extractCondition(line)))
                }
                // Asignación simple: "x = expr"
                line.contains("=") &&
                !line.startsWith("SI", ignoreCase = true) &&
                !line.startsWith("MIENTRAS", ignoreCase = true) &&
                !line.startsWith("VAR", ignoreCase = true) &&
                !line.startsWith("#") -> {
                    val eqIdx = line.indexOf("=")
                    val name  = line.substring(0, eqIdx).trim()
                    if (name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) &&
                        symbols.none { it.name == name }) {
                        val value = line.substring(eqIdx + 1).trim()
                        symbols.add(SymbolData(name, value, lineNum))
                    }
                }
            }
        }

        // Limpiamos y normalizamos el código del algoritmo
        val lines = algoCode.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        var nodeId = 0
        var yPos = 0.dp

        // Stack para manejar anidamiento: (nodeId de la decisión, tipo: "SI" o "MIENTRAS")
        data class ControlNode(val nodeId: Int, val type: String)
        val controlStack = mutableListOf<ControlNode>()
        val lastNodeStack = mutableListOf<Int>()  // Último nodo ejecutado en cada nivel

        // === NODO DE INICIO ===
        val startNode = DiagramNode(
            id = nodeId,
            type = ShapeType.OVAL,
            label = "INICIO",
            x = 50.dp,
            y = yPos,
            width = 80.dp,
            height = 50.dp,
            fillColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
        )
        nodes.add(startNode)
        lastNodeStack.add(nodeId)
        nodeId++
        yPos += 80.dp

        // === PROCESAMIENTO DE LÍNEAS ===
        var inSiBlock = false
        var siNodeId = -1

        for (line in lines) {
            // Saltar INICIO y FIN
            if (line == "INICIO" || line == "FIN" || line.isEmpty() || line == "%%%%") {
                continue
            }

            when {
                line.startsWith("SI", ignoreCase = true) -> {
                    val condition = extractCondition(line)
                    val lastId = lastNodeStack.lastOrNull() ?: 0

                    val siNode = DiagramNode(
                        id = nodeId,
                        type = config.siFigure,
                        label = "¿$condition?",
                        x = 50.dp,
                        y = yPos,
                        width = 120.dp,
                        height = 80.dp,
                        textColor = config.siTextColor,
                        fillColor = config.siBackColor
                    )
                    nodes.add(siNode)
                    // Conexión normal desde el nodo anterior
                    connections.add(DiagramConnection(lastId, nodeId))

                    controlStack.add(ControlNode(nodeId, "SI"))
                    siNodeId = nodeId
                    nodeId++
                    yPos += 120.dp
                    inSiBlock = true
                }

                line.startsWith("MIENTRAS", ignoreCase = true) -> {
                    val condition = extractCondition(line)
                    val lastId = lastNodeStack.lastOrNull() ?: 0

                    val whileNode = DiagramNode(
                        id = nodeId,
                        type = config.mientrasFigure,
                        label = "¿$condition?",
                        x = 50.dp,
                        y = yPos,
                        width = 120.dp,
                        height = 80.dp,
                        textColor = config.mientrasTextColor,
                        fillColor = config.mientrasBackColor
                    )
                    nodes.add(whileNode)
                    connections.add(DiagramConnection(lastId, nodeId))

                    controlStack.add(ControlNode(nodeId, "MIENTRAS"))
                    siNodeId = nodeId
                    nodeId++
                    yPos += 120.dp
                }

                line.startsWith("MOSTRAR", ignoreCase = true) -> {
                    val message = extractMessage(line)
                    val lastId = lastNodeStack.lastOrNull() ?: 0

                    val displayNode = DiagramNode(
                        id = nodeId,
                        type = ShapeType.PARALLELOGRAM,
                        label = "Mostrar\n$message",
                        x = 50.dp,
                        y = yPos,
                        width = 100.dp,
                        height = 70.dp,
                        textColor = config.bloqueTextColor,
                        fillColor = config.bloqueBackColor
                    )
                    nodes.add(displayNode)

                    // Si estamos dentro de un SI, conectar con "Sí"
                    if (inSiBlock && siNodeId != -1) {
                        connections.add(DiagramConnection(siNodeId, nodeId, "Sí"))
                        inSiBlock = false
                    } else {
                        connections.add(DiagramConnection(lastId, nodeId))
                    }

                    lastNodeStack.clear()
                    lastNodeStack.add(nodeId)
                    nodeId++
                    yPos += 100.dp
                }

                line.startsWith("LEER", ignoreCase = true) -> {
                    val varName = extractVariable(line)
                    val lastId = lastNodeStack.lastOrNull() ?: 0

                    val readNode = DiagramNode(
                        id = nodeId,
                        type = ShapeType.PARALLELOGRAM,
                        label = "Leer\n$varName",
                        x = 50.dp,
                        y = yPos,
                        width = 100.dp,
                        height = 70.dp,
                        textColor = config.bloqueTextColor,
                        fillColor = config.bloqueBackColor
                    )
                    nodes.add(readNode)
                    connections.add(DiagramConnection(lastId, nodeId))

                    lastNodeStack.clear()
                    lastNodeStack.add(nodeId)
                    nodeId++
                    yPos += 100.dp
                }

                line.startsWith("VAR", ignoreCase = true) ||
                line.contains("=") -> {
                    val label = if (line.length > 30) {
                        line.take(27) + "..."
                    } else {
                        line
                    }
                    val lastId = lastNodeStack.lastOrNull() ?: 0

                    val instructionNode = DiagramNode(
                        id = nodeId,
                        type = config.bloqueFigure,
                        label = label,
                        x = 50.dp,
                        y = yPos,
                        width = 120.dp,
                        height = 60.dp,
                        textColor = config.bloqueTextColor,
                        fillColor = config.bloqueBackColor
                    )
                    nodes.add(instructionNode)

                    if (inSiBlock && siNodeId != -1) {
                        connections.add(DiagramConnection(siNodeId, nodeId, "Sí"))
                        inSiBlock = false
                    } else {
                        connections.add(DiagramConnection(lastId, nodeId))
                    }

                    lastNodeStack.clear()
                    lastNodeStack.add(nodeId)
                    nodeId++
                    yPos += 90.dp
                }

                line.startsWith("FINSI", ignoreCase = true) -> {
                    // Cierre de SI: generar rama "No"
                    if (controlStack.isNotEmpty() && controlStack.last().type == "SI") {
                        val siControl = controlStack.removeAt(controlStack.size - 1)
                        val nextId = nodeId  // Próximo nodo que se ejecutará

                        // Conexión "No" del SI al siguiente nodo
                        // (se actualiza cuando se cree el siguiente nodo)
                        connections.add(DiagramConnection(siControl.nodeId, nextId, "No"))

                        lastNodeStack.clear()
                        lastNodeStack.add(nextId - 1)  // Apunta al último procesado
                    }
                    siNodeId = -1
                    inSiBlock = false
                }

                line.startsWith("FINMIENTRAS", ignoreCase = true) -> {
                    // Cierre de MIENTRAS: generar rama de regreso
                    if (controlStack.isNotEmpty() && controlStack.last().type == "MIENTRAS") {
                        val whileControl = controlStack.removeAt(controlStack.size - 1)

                        // Conexión "No" que sale del bucle
                        connections.add(DiagramConnection(whileControl.nodeId, nodeId, "No"))

                        // Conexión de regreso al inicio del bucle (desde el último nodo del cuerpo)
                        val lastBodyNode = lastNodeStack.lastOrNull() ?: nodeId
                        connections.add(DiagramConnection(lastBodyNode, whileControl.nodeId, "Regresa"))

                        lastNodeStack.clear()
                        lastNodeStack.add(nodeId)
                    }
                    siNodeId = -1
                    inSiBlock = false
                }

                else -> {
                    // Línea no reconocida
                }
            }
        }

        // === NODO DE FIN ===
        val lastNodeId = lastNodeStack.lastOrNull() ?: (nodeId - 1)
        val endNode = DiagramNode(
            id = nodeId,
            type = ShapeType.OVAL,
            label = "FIN",
            x = 50.dp,
            y = yPos,
            width = 80.dp,
            height = 50.dp,
            fillColor = androidx.compose.ui.graphics.Color(0xFFFFEBEE)
        )
        nodes.add(endNode)
        connections.add(DiagramConnection(lastNodeId, nodeId))

        val operators = extractOperators(algoCode)
        return DiagramResult(nodes, connections, symbols, operators, controlStructures)
    }

    /** Extrae la condición de una línea SI o MIENTRAS */
    private fun extractCondition(line: String): String {
        val startIdx = line.indexOf("(")
        val endIdx = line.lastIndexOf(")")
        return if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
            line.substring(startIdx + 1, endIdx)
        } else {
            "condición"
        }
    }

    /** Extrae el mensaje de una línea MOSTRAR */
    private fun extractMessage(line: String): String {
        val parts = line.split("MOSTRAR", ignoreCase = true)
        return if (parts.size > 1) {
            val msg = parts[1].trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .take(15)
            msg
        } else {
            "..."
        }
    }

    /** Extrae el nombre de variable de LEER o asignación */
    private fun extractVariable(line: String): String {
        val parts = line.split("LEER", ignoreCase = true)
        return if (parts.size > 1) {
            parts[1].trim().take(15)
        } else {
            "var"
        }
    }

    // ── Extracción de operadores aritméticos ───────────────────────────────

    private fun extractOperators(algoCode: String): List<OperatorOccurrence> {
        val result = mutableListOf<OperatorOccurrence>()
        algoCode.split("\n").forEachIndexed { idx, rawLine ->
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            // Saltar comentarios y líneas vacías
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            var inString = false
            var i = 0
            while (i < rawLine.length) {
                val c = rawLine[i]
                when {
                    c == '"'  -> { inString = !inString; i++; continue }
                    inString  -> { i++; continue }
                    c == '#'  -> break  // resto de línea es comentario
                }
                val col = i + 1
                // Para el contexto: línea completa truncada a 30 chars
                val ctx = trimmed.take(30)
                // Revisar secuencias de 2 chars para no confundir == != >= <=
                val next = if (i + 1 < rawLine.length) rawLine[i + 1] else ' '
                when {
                    c == '+' ->
                        result.add(OperatorOccurrence("Suma",          "+", lineNum, col, ctx))
                    c == '-' && next != '>' ->
                        result.add(OperatorOccurrence("Resta",         "-", lineNum, col, ctx))
                    c == '*' ->
                        result.add(OperatorOccurrence("Multiplicación", "*", lineNum, col, ctx))
                    c == '/' ->
                        result.add(OperatorOccurrence("División",      "/", lineNum, col, ctx))
                }
                i++
            }
        }
        return result
    }

    // ── Parseo de sección de configuración ──────────────────────────────────

    /**
     * Parsea el texto de la sección de configuración (después de %%%%)
     * y devuelve un DiagramConfig con los valores encontrados.
     * Las directivas no reconocidas o mal formadas se ignoran silenciosamente.
     */
    private fun parseConfig(configCode: String): DiagramConfig {
        var siTextColor       = androidx.compose.ui.graphics.Color.Black
        var siBackColor       = androidx.compose.ui.graphics.Color(0xFFE3F2FD)
        var siFigure          = ShapeType.DIAMOND
        var siFontSize        = 12

        var mientrasTextColor = androidx.compose.ui.graphics.Color.Black
        var mientrasBackColor = androidx.compose.ui.graphics.Color(0xFFF3E5F5)
        var mientrasFigure    = ShapeType.DIAMOND
        var mienstrasFontSize = 12

        var bloqueTextColor   = androidx.compose.ui.graphics.Color.Black
        var bloqueBackColor   = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
        var bloqueFigure      = ShapeType.RECTANGLE
        var bloqueFontSize    = 12

        configCode.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.contains("=") }
            .forEach { line ->
                val clean    = if (line.startsWith("%")) line.substring(1) else line
                val eqIdx    = clean.indexOf("=")
                if (eqIdx == -1) return@forEach

                val directive = clean.substring(0, eqIdx).trim().uppercase()
                // Quitar sufijo de prioridad (|N)
                val value     = clean.substring(eqIdx + 1).trim().substringBefore("|").trim()

                when (directive) {
                    "FIGURA_SI"              -> siFigure          = parseFigure(value)
                    "FIGURA_MIENTRAS"        -> mientrasFigure    = parseFigure(value)
                    "FIGURA_BLOQUE"          -> bloqueFigure      = parseFigure(value)

                    "COLOR_TEXTO_SI"         -> siTextColor       = parseColor(value)
                    "COLOR_SI"               -> siBackColor       = parseColor(value)
                    "COLOR_TEXTO_MIENTRAS"   -> mientrasTextColor = parseColor(value)
                    "COLOR_MIENTRAS"         -> mientrasBackColor = parseColor(value)
                    "COLOR_TEXTO_BLOQUE"     -> bloqueTextColor   = parseColor(value)
                    "COLOR_BLOQUE"           -> bloqueBackColor   = parseColor(value)

                    "LETRA_SIZE_SI"          -> siFontSize        = value.toIntOrNull() ?: 12
                    "LETRA_SIZE_MIENTRAS"    -> mienstrasFontSize = value.toIntOrNull() ?: 12
                    "LETRA_SIZE_BLOQUE"      -> bloqueFontSize    = value.toIntOrNull() ?: 12
                }
            }

        return DiagramConfig(
            siTextColor       = siTextColor,
            siBackColor       = siBackColor,
            siFigure          = siFigure,
            siFontSize        = siFontSize,
            mientrasTextColor = mientrasTextColor,
            mientrasBackColor = mientrasBackColor,
            mientrasFigure    = mientrasFigure,
            mienstrasFontSize = mienstrasFontSize,
            bloqueTextColor   = bloqueTextColor,
            bloqueBackColor   = bloqueBackColor,
            bloqueFigure      = bloqueFigure,
            bloqueFontSize    = bloqueFontSize
        )
    }

    /** Convierte nombre de figura del lenguaje de config a ShapeType */
    private fun parseFigure(value: String): ShapeType = when (value.uppercase().trim()) {
        "ELIPSE", "CIRCULO"                   -> ShapeType.OVAL
        "RECTANGULO", "RECTANGULO_REDONDEADO" -> ShapeType.RECTANGLE
        "ROMBO"                               -> ShapeType.DIAMOND
        "PARALELOGRAMO"                       -> ShapeType.PARALLELOGRAM
        else                                  -> ShapeType.DIAMOND
    }

    /**
     * Parsea un color en formato:
     *  - HEX: H seguido de 6 dígitos hexadecimales (ej. HFF00AA)
     *  - RGB: tres enteros separados por coma (ej. 255,128,0)
     *         Los valores pueden incluir aritmética simple (+/-)
     */
    private fun parseColor(value: String): androidx.compose.ui.graphics.Color {
        val v = value.trim()
        if (v.matches(Regex("H[0-9A-Fa-f]{6}"))) {
            val r = v.substring(1, 3).toInt(16)
            val g = v.substring(3, 5).toInt(16)
            val b = v.substring(5, 7).toInt(16)
            return androidx.compose.ui.graphics.Color(r / 255f, g / 255f, b / 255f)
        }
        val parts = v.split(",")
        if (parts.size == 3) {
            val r = evalSimple(parts[0].trim()).coerceIn(0, 255)
            val g = evalSimple(parts[1].trim()).coerceIn(0, 255)
            val b = evalSimple(parts[2].trim()).coerceIn(0, 255)
            return androidx.compose.ui.graphics.Color(r / 255f, g / 255f, b / 255f)
        }
        return androidx.compose.ui.graphics.Color.Black
    }

    /**
     * Evalúa expresiones aritméticas simples (suma y resta de enteros).
     * Ej: "45-5" → 40, "10+2" → 12, "128" → 128
     */
    private fun evalSimple(expr: String): Int {
        val e = expr.trim()
        val plusIdx = e.lastIndexOf('+')
        if (plusIdx > 0)
            return evalSimple(e.substring(0, plusIdx)) + evalSimple(e.substring(plusIdx + 1))
        val minusIdx = e.lastIndexOf('-')
        if (minusIdx > 0)
            return evalSimple(e.substring(0, minusIdx)) - evalSimple(e.substring(minusIdx + 1))
        return e.toIntOrNull() ?: 0
    }
}
