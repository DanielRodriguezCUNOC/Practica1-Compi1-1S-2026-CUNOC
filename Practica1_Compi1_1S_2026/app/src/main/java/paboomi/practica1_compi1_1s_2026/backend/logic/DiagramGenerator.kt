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
    fun generate(code: String, config: DiagramConfig = DiagramConfig()): DiagramResult {
        val nodes = mutableListOf<DiagramNode>()
        val connections = mutableListOf<DiagramConnection>()

        // Limpiamos y normalizamos el código
        val lines = code.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("%") }

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
                        type = ShapeType.DIAMOND,
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
                        type = ShapeType.DIAMOND,
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
                        type = ShapeType.RECTANGLE,
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

        return DiagramResult(nodes, connections)
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
