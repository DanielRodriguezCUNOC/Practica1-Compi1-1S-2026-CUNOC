package paboomi.practica1_compi1_1s_2026.backend.logic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Formas disponibles en los diagramas de flujo académicos.
 */
enum class ShapeType {
    OVAL,               // Inicio/Fin
    RECTANGLE,          // Instrucciones normales
    DIAMOND,            // Decisiones (SI, MIENTRAS)
    PARALLELOGRAM       // Entrada/Salida (LEER, MOSTRAR)
}

/**
 * Nodo individual del diagrama de flujo.
 *
 * - id: identificador único para conectar nodos
 * - type: forma del nodo
 * - label: texto a mostrar (ej. "a = b + 1", "¿a > 5?")
 * - x, y: posición en el canvas (dp)
 * - width, height: dimensiones del nodo
 * - textColor: color del texto
 * - fillColor: color de fondo del nodo
 */
data class DiagramNode(
    val id: Int,
    val type: ShapeType,
    val label: String,
    val x: Dp = 0.dp,
    val y: Dp = 0.dp,
    val width: Dp = 100.dp,
    val height: Dp = 60.dp,
    val textColor: Color = Color.Black,
    val fillColor: Color = Color.White
)

/**
 * Conexión entre dos nodos del diagrama.
 *
 * - fromId: ID del nodo de origen
 * - toId: ID del nodo destino
 * - label: etiqueta en la flecha (ej. "Sí", "No")
 */
data class DiagramConnection(
    val fromId: Int,
    val toId: Int,
    val label: String = ""
)

/**
 * Configuración de estilos para el diagrama completo.
 * Parsea valores de la sección de configuración.
 */
data class DiagramConfig(
    // SI (Decisiones)
    val siTextColor: Color = Color.Black,
    val siBackColor: Color = Color(0xFFE3F2FD),  // Azul claro
    val siFigure: ShapeType = ShapeType.DIAMOND,
    val siFontSize: Int = 12,

    // MIENTRAS (Bucles)
    val mientrasTextColor: Color = Color.Black,
    val mientrasBackColor: Color = Color(0xFFF3E5F5),  // Púrpura claro
    val mientrasFigure: ShapeType = ShapeType.DIAMOND,
    val mienstrasFontSize: Int = 12,

    // BLOQUE (instrucciones generales)
    val bloqueTextColor: Color = Color.Black,
    val bloqueBackColor: Color = Color(0xFFF5F5F5),  // Gris claro
    val bloqueFigure: ShapeType = ShapeType.RECTANGLE,
    val bloqueFontSize: Int = 12
)

/**
 * Entrada de la tabla de símbolos: representa una variable declarada.
 */
data class SymbolData(
    val name: String,
    val value: String,   // valor inicial, o "-" si no tiene
    val line: Int
)

/**
 * Resultado del generador de diagramas.
 * Contiene los nodos y conexiones necesarias para renderizar.
 */
data class DiagramResult(
    val nodes: List<DiagramNode>,
    val connections: List<DiagramConnection>,
    val symbols: List<SymbolData> = emptyList()
) {
    val isEmpty: Boolean get() = nodes.isEmpty()
    val nodeCount: Int get() = nodes.size
}
