package paboomi.practica1_compi1_1s_2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import paboomi.practica1_compi1_1s_2026.backend.logic.TokenData
import paboomi.practica1_compi1_1s_2026.backend.logic.DiagramResult
import paboomi.practica1_compi1_1s_2026.backend.logic.SymbolData
import paboomi.practica1_compi1_1s_2026.backend.logic.OperatorOccurrence
import paboomi.practica1_compi1_1s_2026.backend.logic.ControlStructure
import paboomi.practica1_compi1_1s_2026.ui.diagrams.DiagramCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramScreen(
    tokens: List<TokenData>,
    errors: List<String>,
    diagram: DiagramResult,
    hasErrors: Boolean,
    isCompiling: Boolean,
    onBack: () -> Unit
) {
    /* ── Regla del requisito ────────────────────────────────────────
       Si hay errores → forzar tab "Errores" y bloquear los demás.
       Si no hay errores → mostrar todos los tabs normalmente.
       ──────────────────────────────────────────────────────────── */
    var selectedTab by remember { mutableIntStateOf(0) }

    // Cuando llega el resultado con errores, saltar automáticamente a tab Errores
    LaunchedEffect(hasErrors) {
        if (hasErrors) selectedTab = 5
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagrama de Flujo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Editor"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (isCompiling) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Analizando código...")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                /* ── TabRow ────────────────────────────────────────────────
                   REQUISITO: Si existen errores, solo el tab "Errores" debe
                   ser accesible. Los tabs "Diagrama" y "Tokens" se deshabilitan
                   visualmente y sus clicks no tienen efecto.
                   ──────────────────────────────────────────────────────── */
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp
                ) {
                    Tab(selected = selectedTab == 0,
                        onClick = { if (!hasErrors) selectedTab = 0 },
                        enabled = !hasErrors,
                        text = { Text("Diagrama") })
                    Tab(selected = selectedTab == 1,
                        onClick = { if (!hasErrors) selectedTab = 1 },
                        enabled = !hasErrors,
                        text = { Text("Tokens (${tokens.size})") })
                    Tab(selected = selectedTab == 2,
                        onClick = { if (!hasErrors) selectedTab = 2 },
                        enabled = !hasErrors,
                        text = { Text("Símbolos (${diagram.symbols.size})") })
                    Tab(selected = selectedTab == 3,
                        onClick = { if (!hasErrors) selectedTab = 3 },
                        enabled = !hasErrors,
                        text = { Text("Operadores (${diagram.operators.size})") })
                    Tab(selected = selectedTab == 4,
                        onClick = { if (!hasErrors) selectedTab = 4 },
                        enabled = !hasErrors,
                        text = { Text("Control (${diagram.controlStructures.size})") })
                    Tab(selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("Errores (${errors.size})") })
                }

                when (selectedTab) {
                    0 -> DiagramContent(diagram)
                    1 -> TokensContent(tokens)
                    2 -> SymbolsContent(diagram.symbols)
                    3 -> OperatorsContent(diagram.operators)
                    4 -> ControlContent(diagram.controlStructures)
                    5 -> ErrorsContent(errors)
                }
            }
        }
    }
}

@Composable
private fun DiagramContent(diagram: DiagramResult) {
    /* ── Renderizar el diagrama de flujo ─────────────────────
       Si está vacío, mostrar placeholder; si tiene nodos,
       mostrar el canvas con la visualización del diagrama.
       ────────────────────────────────────────────────────── */
    if (diagram.isEmpty) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin diagrama",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        DiagramCanvas(
            diagram = diagram,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TokensContent(tokens: List<TokenData>) {
    if (tokens.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay tokens. Presiona Compilar primero.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado de la tabla
        TokenTableRow(
            col1 = "#",
            col2 = "Token",
            col3 = "Valor",
            col4 = "Línea",
            col5 = "Col",
            isHeader = true
        )
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)

        // Filas de tokens
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(tokens) { index, token ->
                val bgColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                TokenTableRow(
                    col1 = "${index + 1}",
                    col2 = token.type,
                    col3 = if (token.value == "-") "" else token.value,
                    col4 = "${token.line}",
                    col5 = "${token.column}",
                    isHeader = false,
                    modifier = Modifier.background(bgColor)
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun TokenTableRow(
    col1: String,
    col2: String,
    col3: String,
    col4: String,
    col5: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier
) {
    val textStyle = if (isHeader)
        MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    else
        MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )

    val bgModifier = if (isHeader)
        modifier.background(MaterialTheme.colorScheme.primaryContainer)
    else
        modifier

    Row(
        modifier = bgModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            style = textStyle,
            modifier = Modifier.weight(0.07f),
            textAlign = TextAlign.Center,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = col2,
            style = textStyle,
            modifier = Modifier
                .weight(0.42f)
                .padding(start = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.primary
        )
        Text(
            text = col3,
            style = textStyle,
            modifier = Modifier
                .weight(0.28f)
                .padding(start = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = col4,
            style = textStyle,
            modifier = Modifier.weight(0.12f),
            textAlign = TextAlign.Center,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = col5,
            style = textStyle,
            modifier = Modifier.weight(0.11f),
            textAlign = TextAlign.Center,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SymbolsContent(symbols: List<SymbolData>) {
    if (symbols.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se declararon variables.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#",        modifier = Modifier.weight(0.07f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Variable", modifier = Modifier.weight(0.35f).padding(start = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Valor inicial", modifier = Modifier.weight(0.43f).padding(start = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Línea", modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(symbols) { index, sym ->
                val bgColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}",
                        modifier = Modifier.weight(0.07f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(sym.name,
                        modifier = Modifier.weight(0.35f).padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(sym.value,
                        modifier = Modifier.weight(0.43f).padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${sym.line}",
                        modifier = Modifier.weight(0.15f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun OperatorsContent(operators: List<OperatorOccurrence>) {
    if (operators.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron operadores aritméticos.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            listOf("Operador" to 0.28f, "Línea" to 0.12f, "Columna" to 0.14f, "Ocurrencia" to 0.46f)
                .forEach { (header, weight) ->
                    Text(header, modifier = Modifier.weight(weight),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = if (header != "Ocurrencia") TextAlign.Center else TextAlign.Start)
                }
        }
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(operators) { index, op ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                Row(
                    modifier = Modifier.fillMaxWidth().background(bg)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(op.operator, modifier = Modifier.weight(0.28f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Text("${op.line}", modifier = Modifier.weight(0.12f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    Text("${op.column}", modifier = Modifier.weight(0.14f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    Text(op.occurrence, modifier = Modifier.weight(0.46f).padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider(thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
private fun ControlContent(structures: List<ControlStructure>) {
    if (structures.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron estructuras de control.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("Objeto",     modifier = Modifier.weight(0.22f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
            Text("Línea",     modifier = Modifier.weight(0.15f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
            Text("Condición", modifier = Modifier.weight(0.63f).padding(start = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(structures) { index, ctrl ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                Row(
                    modifier = Modifier.fillMaxWidth().background(bg)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ctrl.type, modifier = Modifier.weight(0.22f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    Text("${ctrl.line}", modifier = Modifier.weight(0.15f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    Text(ctrl.condition, modifier = Modifier.weight(0.63f).padding(start = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider(thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
private fun ErrorsContent(errors: List<String>) {
    if (errors.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sin errores léxicos",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "El código fue analizado correctamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(errors) { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}