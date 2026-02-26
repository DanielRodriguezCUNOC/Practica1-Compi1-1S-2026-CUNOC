package paboomi.practica1_compi1_1s_2026.backend.logic

import androidx.compose.ui.unit.dp

/**
 * Patrón Experto: DiagramGenerator es el responsable de convertir
 * pseudocódigo en nodos y conexiones de diagrama de flujo.
 *
 * Simplificación académica: parsea el código línea por línea sin
 * construir un AST completo, reconociendo patrones de palabras clave.
 */
object DiagramGenerator {

    /**
     * Genera un diagrama de flujo a partir del código pseudocódigo.
     * Retorna los nodos y conexiones necesarios para renderizar.
     */
    fun generate(code: String, config: DiagramConfig = DiagramConfig()): DiagramResult {
        val nodes = mutableListOf<DiagramNode>()
        val connections = mutableListOf<DiagramConnection>()

        // Limpiamos y normalizamos el código
        val lines = code.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("%") }

        // Rastreamos niveles de anidamiento para posicionar los nodos
        var nodeId = 0
        var yPos = 0.dp
        val nodeStack = mutableListOf<Int>()  // Para conectar nodos

        // === NODO DE INICIO ===
        val startNode = DiagramNode(
            id = nodeId,
            type = ShapeType.OVAL,
            label = "INICIO",
            x = 50.dp,
            y = yPos,
            width = 80.dp,
            height = 50.dp,
            fillColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)  // Verde
        )
        nodes.add(startNode)
        nodeStack.add(nodeId)
        nodeId++
        yPos += 80.dp

        // === PROCESAMIENTO DE LÍNEAS ===
        for (line in lines) {
            // Saltar INICIO y FIN que ya están procesados
            if (line == "INICIO" || line == "FIN" || line.isEmpty() || line == "%%%%") {
                continue
            }

            val (newNodes, newConnections, nextNodeId, nextYPos) = processLine(
                line, nodeId, yPos, nodeStack, config
            )

            nodes.addAll(newNodes)
            connections.addAll(newConnections)
            nodeId = nextNodeId
            yPos = nextYPos
        }

        // === NODO DE FIN ===
        val lastNodeId = nodeStack.lastOrNull() ?: (nodeId - 1)
        val endNode = DiagramNode(
            id = nodeId,
            type = ShapeType.OVAL,
            label = "FIN",
            x = 50.dp,
            y = yPos,
            width = 80.dp,
            height = 50.dp,
            fillColor = androidx.compose.ui.graphics.Color(0xFFFFEBEE)  // Rojo claro
        )
        nodes.add(endNode)
        connections.add(DiagramConnection(lastNodeId, nodeId))

        return DiagramResult(nodes, connections)
    }

    /**
     * Procesa una línea del código y retorna los nodos generados,
     * conexiones, y la siguiente posición para el próximo nodo.
     */
    private fun processLine(
        line: String,
        startNodeId: Int,
        startYPos: androidx.compose.ui.unit.Dp,
        nodeStack: MutableList<Int>,
        config: DiagramConfig
    ): Tuple4 {
        val nodes = mutableListOf<DiagramNode>()
        val connections = mutableListOf<DiagramConnection>()
        var nodeId = startNodeId
        var yPos = startYPos
        val lastNodeId = nodeStack.lastOrNull() ?: (startNodeId - 1)

        when {
            // ──── SI (Decisión) ────
            line.startsWith("SI", ignoreCase = true) -> {
                val condition = extractCondition(line)
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
                connections.add(DiagramConnection(lastNodeId, nodeId, "Sí"))
                nodeStack.add(nodeId)
                nodeId++
                yPos += 120.dp
            }

            // ──── MIENTRAS (Bucle) ────
            line.startsWith("MIENTRAS", ignoreCase = true) -> {
                val condition = extractCondition(line)
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
                connections.add(DiagramConnection(lastNodeId, nodeId))
                nodeStack.add(nodeId)
                nodeId++
                yPos += 120.dp
            }

            // ──── MOSTRAR (Salida) ────
            line.startsWith("MOSTRAR", ignoreCase = true) -> {
                val message = extractMessage(line)
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
                connections.add(DiagramConnection(lastNodeId, nodeId))
                nodeStack.clear()
                nodeStack.add(nodeId)
                nodeId++
                yPos += 100.dp
            }

            // ──── LEER (Entrada) ────
            line.startsWith("LEER", ignoreCase = true) -> {
                val varName = extractVariable(line)
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
                connections.add(DiagramConnection(lastNodeId, nodeId))
                nodeStack.clear()
                nodeStack.add(nodeId)
                nodeId++
                yPos += 100.dp
            }

            // ──── VAR, ASIGNACIÓN (Instrucción general) ────
            line.startsWith("VAR", ignoreCase = true) ||
            line.contains("=") -> {
                val label = if (line.length > 30) {
                    line.take(27) + "..."
                } else {
                    line
                }
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
                connections.add(DiagramConnection(lastNodeId, nodeId))
                nodeStack.clear()
                nodeStack.add(nodeId)
                nodeId++
                yPos += 90.dp
            }

            // ──── FINSI, FINMIENTRAS (Fin de bloques) ────
            line.startsWith("FINSI", ignoreCase = true) ||
            line.startsWith("FINMIENTRAS", ignoreCase = true) -> {
                if (nodeStack.isNotEmpty()) {
                    nodeStack.removeAt(nodeStack.size - 1)
                }
            }

            else -> {
                // Línea no reconocida o comentario
            }
        }

        return Tuple4(nodes, connections, nodeId, yPos)
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
}

/** Clase auxiliar para retornar múltiples valores */
internal data class Tuple4(
    val nodes: List<DiagramNode>,
    val connections: List<DiagramConnection>,
    val nodeId: Int,
    val yPos: androidx.compose.ui.unit.Dp
)
