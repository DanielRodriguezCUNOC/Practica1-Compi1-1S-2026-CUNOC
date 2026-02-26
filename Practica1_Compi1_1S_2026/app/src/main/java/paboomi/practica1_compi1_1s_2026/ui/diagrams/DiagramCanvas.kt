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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramResult
import paboomi.practica1_compi1_1s_2026.backend.logic.ShapeType

/**
 * Canvas simplificado que renderiza un diagrama de flujo usando composables.
 * Enfoque académico: visualización textual clara, sin complejidades gráficas.
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

/**
 * Renderiza un nodo individual del diagrama como una tarjeta.
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
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = node.fillColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
