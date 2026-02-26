package paboomi.practica1_compi1_1s_2026.ui.diagrams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramResult
import paboomi.practica1_compi1_1s_2026.backend.logic.ShapeType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Canvas mejorado que renderiza un diagrama de flujo usando composables
 * con líneas de conexión entre nodos.
 */
@Composable
fun DiagramCanvas(
    diagram: DiagramResult,
    modifier: Modifier = Modifier
) {
    if (diagram.isEmpty) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Sin diagrama",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        return
    }

    // Contenedor con líneas de fondo
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                // Dibujar las líneas de conexión DETRÁS de todo
                drawConnections(this, diagram)
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            /* ── Mostrar diagrama como lista de nodos ────────────────
               Cada nodo se renderiza como una tarjeta con su forma
               y etiqueta, en orden de ejecución (representando el flujo).
               ───────────────────────────────────────────────────────── */

            diagram.nodes.forEach { node ->
                DiagramNodeCard(node)
            }

            // Pie informativo
            Text(
                "Diagrama generado: ${diagram.nodeCount} nodos",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Dibuja las líneas de conexión entre nodos.
 */
private fun drawConnections(drawScope: DrawScope, diagram: DiagramResult) {
    with(drawScope) {
        // Para cada conexión, dibujamos una línea con flecha
        diagram.connections.forEach { connection ->
            val fromNode = diagram.nodes.find { it.id == connection.fromId }
            val toNode = diagram.nodes.find { it.id == connection.toId }

            if (fromNode != null && toNode != null) {
                drawArrowBetweenCards(fromNode, toNode, connection.label)
            }
        }
    }
}

/**
 * Dibuja una flecha entre dos nodos (representados como tarjetas).
 * Las líneas se dibujan entre los centros de las tarjetas.
 */
private fun DrawScope.drawArrowBetweenCards(
    fromNode: paboomi.practica1_compi1_1s_2026.backend.logic.DiagramNode,
    toNode: paboomi.practica1_compi1_1s_2026.backend.logic.DiagramNode,
    label: String
) {
    // Convertir Dp a Float para el drawing
    val fromX = fromNode.x.toPx() + fromNode.width.toPx() / 2
    val fromY = fromNode.y.toPx() + fromNode.height.toPx() / 2

    val toX = toNode.x.toPx() + toNode.width.toPx() / 2
    val toY = toNode.y.toPx() + toNode.height.toPx() / 2

    // Dibujar línea principal
    drawLine(
        color = Color.Gray,
        start = Offset(fromX, fromY),
        end = Offset(toX, toY),
        strokeWidth = 2f
    )

    // Dibujar punta de flecha (triángulo)
    val angle = atan2(toY - fromY, toX - fromX)
    val arrowLength = 12f
    val arrowAngle = Math.PI / 6  // 30 grados

    val arrowPoint1X = toX - arrowLength * cos(angle - arrowAngle)
    val arrowPoint1Y = toY - arrowLength * sin(angle - arrowAngle)

    val arrowPoint2X = toX - arrowLength * cos(angle + arrowAngle)
    val arrowPoint2Y = toY - arrowLength * sin(angle + arrowAngle)

    // Líneas de la punta de flecha
    drawLine(
        color = Color.Gray,
        start = Offset(toX, toY),
        end = Offset(arrowPoint1X.toFloat(), arrowPoint1Y.toFloat()),
        strokeWidth = 2f
    )
    drawLine(
        color = Color.Gray,
        start = Offset(toX, toY),
        end = Offset(arrowPoint2X.toFloat(), arrowPoint2Y.toFloat()),
        strokeWidth = 2f
    )

    // Dibujar etiqueta de la rama si existe
    if (label.isNotEmpty()) {
        val midX = (fromX + toX) / 2 + 30
        val midY = (fromY + toY) / 2
    }
}

/**
 * Renderiza un nodo individual del diagrama como una tarjeta.
 * Para nodos de decisión (DIAMOND), muestra indicadores de flujo "Sí" y "No".
 */
@Composable
private fun DiagramNodeCard(node: paboomi.practica1_compi1_1s_2026.backend.logic.DiagramNode) {
    val shapeSymbol = when (node.type) {
        ShapeType.OVAL -> "◯"
        ShapeType.RECTANGLE -> "▭"
        ShapeType.DIAMOND -> "◇"
        ShapeType.PARALLELOGRAM -> "▱"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = node.fillColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    shapeSymbol,
                    style = MaterialTheme.typography.displaySmall,
                    color = node.textColor,
                    fontSize = 28.sp
                )
                Text(
                    node.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = node.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                /* ── Indicadores de flujo para decisiones ────────────
                   Si el nodo es un DIAMOND (SI/MIENTRAS), mostrar
                   badges para las ramas "Sí" y "No" o "Regresa"
                   ──────────────────────────────────────────────── */
                if (node.type == ShapeType.DIAMOND) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                    ) {
                        FlowBadge("Sí", Color(0xFF4CAF50))  // Verde
                        FlowBadge("No", Color(0xFFF44336))  // Rojo
                    }
                }
            }
        }
    }
}

/**
 * Badge pequeño que indica el flujo (Sí, No, Regresa).
 */
@Composable
private fun FlowBadge(label: String, color: Color) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
