package paboomi.practica1_compi1_1s_2026.ui.diagrams

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramNode
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramResult
import paboomi.practica1_compi1_1s_2026.backend.logic.ShapeType

// ─── DiagramCanvas principal ────────────────────────────────────────────────

/**
 * Renderiza el diagrama de flujo como una columna vertical de figuras
 * geométricas reales (óvalo, rombo, paralelogramo, rectángulo) con
 * flechas conectoras entre cada nodo.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        diagram.nodes.forEachIndexed { index, node ->
            // Flecha conectora antes de cada nodo (excepto el primero)
            if (index > 0) {
                val incoming = diagram.connections.find { it.toId == node.id }
                FlowConnector(label = incoming?.label ?: "")
            }
            // Figura geométrica con texto
            FlowShapeNode(node)
        }

        Text(
            text = "· ${diagram.nodeCount} nodos ·",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 28.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
        )
    }
}

// ─── Figura geométrica ───────────────────────────────────────────────────────

/**
 * Dibuja la figura correspondiente al tipo de nodo (OVAL, DIAMOND,
 * PARALLELOGRAM, RECTANGLE) y coloca el texto centrado dentro.
 */
@Composable
private fun FlowShapeNode(node: DiagramNode) {

    // Dimensiones por tipo
    val nodeWidth: Dp  = 220.dp
    val nodeHeight: Dp = when (node.type) {
        ShapeType.DIAMOND -> 120.dp
        else              -> 68.dp
    }

    // Color de borde: versión más oscura del relleno
    val borderColor = Color(
        red   = (node.fillColor.red   * 0.65f).coerceIn(0f, 1f),
        green = (node.fillColor.green * 0.65f).coerceIn(0f, 1f),
        blue  = (node.fillColor.blue  * 0.65f).coerceIn(0f, 1f),
        alpha = 1f
    )

    Box(
        modifier = Modifier
            .width(nodeWidth)
            .height(nodeHeight)
            .drawBehind {
                when (node.type) {

                    ShapeType.OVAL -> {
                        drawOval(color = node.fillColor)
                        drawOval(color = borderColor, style = Stroke(width = 2.dp.toPx()))
                    }

                    ShapeType.RECTANGLE -> {
                        drawRect(color = node.fillColor)
                        drawRect(color = borderColor, style = Stroke(width = 2.dp.toPx()))
                    }

                    ShapeType.DIAMOND -> {
                        val path = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width,       size.height / 2f)
                            lineTo(size.width / 2f,  size.height)
                            lineTo(0f,               size.height / 2f)
                            close()
                        }
                        drawPath(path = path, color = node.fillColor)
                        drawPath(path = path, color = borderColor,
                            style = Stroke(width = 2.dp.toPx()))
                    }

                    ShapeType.PARALLELOGRAM -> {
                        val skew = size.height * 0.28f
                        val path = Path().apply {
                            moveTo(skew, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width - skew, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path = path, color = node.fillColor)
                        drawPath(path = path, color = borderColor,
                            style = Stroke(width = 2.dp.toPx()))
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Padding horizontal mayor en el rombo para que el texto no salga
        val hPad: Dp = if (node.type == ShapeType.DIAMOND) 32.dp else 14.dp

        Text(
            text      = node.label,
            color     = node.textColor,
            fontSize  = 13.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = hPad, vertical = 4.dp)
        )
    }
}

// ─── Conector con flecha ─────────────────────────────────────────────────────

/**
 * Flecha vertical con punta que conecta dos nodos consecutivos.
 * Muestra la etiqueta ("Sí", "No", "Regresa") a la derecha de la línea.
 */
@Composable
private fun FlowConnector(label: String) {

    val labelColor = when (label) {
        "Sí"      -> Color(0xFF2E7D32)   // verde oscuro
        "No"      -> Color(0xFFC62828)   // rojo oscuro
        "Regresa" -> Color(0xFF1565C0)   // azul oscuro
        else      -> Color.Gray
    }

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        // Línea y punta de flecha
        Canvas(modifier = Modifier.matchParentSize()) {
            val cx      = size.width / 2f
            val tipY    = size.height - 6.dp.toPx()
            val bodyEnd = tipY - 8.dp.toPx()

            // Línea vertical
            drawLine(
                color       = Color(0xFF9E9E9E),
                start       = Offset(cx, 0f),
                end         = Offset(cx, bodyEnd),
                strokeWidth = 2.dp.toPx()
            )
            // Punta izquierda
            drawLine(
                color       = Color(0xFF9E9E9E),
                start       = Offset(cx, tipY),
                end         = Offset(cx - 7.dp.toPx(), bodyEnd),
                strokeWidth = 2.dp.toPx()
            )
            // Punta derecha
            drawLine(
                color       = Color(0xFF9E9E9E),
                start       = Offset(cx, tipY),
                end         = Offset(cx + 7.dp.toPx(), bodyEnd),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Etiqueta a la derecha de la línea
        if (label.isNotEmpty()) {
            Text(
                text     = label,
                color    = labelColor,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 124.dp)    // justo a la derecha del centro (110dp)
            )
        }
    }
}
