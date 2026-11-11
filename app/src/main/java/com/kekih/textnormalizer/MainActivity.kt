package com.kekih.textnormalizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextNormalizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TextEditorScreen()
                }
            }
        }
    }
}

@Composable
fun TextNormalizerTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFE7B04A),
        onPrimary = Color(0xFF090A0C),
        secondary = Color(0xFF9A9488),
        background = Color(0xFF090A0C),
        surface = Color(0xFF12141A),
        onBackground = Color(0xFFF3EFE6),
        onSurface = Color(0xFFF3EFE6),
        surfaceVariant = Color(0xFF1C1F28)
    )
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen() {
    var text by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Normalizer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = {
                        clipboardManager.getText()?.let { text = it.text }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Вставить")
                    }
                    IconButton(onClick = {
                        if (text.isNotBlank()) clipboardManager.setText(AnnotatedString(text))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Копировать")
                    }
                    IconButton(onClick = { text = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { text = formatToNormalForm(text) },
                icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) },
                text = { Text("Исправить") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Вставь текст решения / черновик и нажми «Исправить»",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (text.isEmpty()) {
                            Text(
                                "Вставь сюда текст...",
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        inner()
                    }
                )
            }
        }
    }
}

fun formatToNormalForm(input: String): String {
    if (input.isBlank()) return input

    // Если текст уже выглядит нормально (есть нормальные переносы и мало мусора) — не трогаем сильно
    val alreadyClean = !input.contains("$") &&
            !input.contains("left(") &&
            !input.contains("right)") &&
            !input.contains("{9}") &&
            !input.contains("\\") &&
            input.lines().size > 5

    if (alreadyClean && input.contains("▸")) {
        // Уже форматировали — просто подчищаем лишние пустые строки
        return input.replace(Regex("\n{3,}"), "\n\n").trim() + "\n"
    }

    var t = input
        .replace("\r\n", "\n")
        .replace("\r", "\n")

    // === 1. Чистка LaTeX ===
    t = t.replace("$$", "\n")
    t = t.replace("$", "")

    t = t.replace(Regex("\\\\?left\\("), "(")
    t = t.replace(Regex("\\\\?right\\)"), ")")
    t = t.replace("left(", "(")
    t = t.replace("right)", ")")
    t = t.replace("left", "")
    t = t.replace("right", "")

    t = t.replace(Regex("\\\\?pm"), "±")
    t = t.replace(Regex("\\\\?cdot"), "·")
    t = t.replace(Regex("\\\\?times"), "×")
    t = t.replace("pm", "±")
    t = t.replace("cdot", "·")

    t = t.replace(Regex("\\\\?sqrt"), "√")
    t = t.replace(Regex("\\\\?dfrac"), "")
    t = t.replace(Regex("\\\\?frac"), "")
    t = t.replace("sqrt", "√")

    t = t.replace("^2", "²")
    t = t.replace("^3", "³")

    t = t.replace("\\", "")

    // Дроби {a}{b} → a/b
    repeat(5) {
        t = t.replace(Regex("\\{([^{}]+)\\}\\{([^{}]+)\\}")) { m ->
            "${m.groupValues[1]}/${m.groupValues[2]}"
        }
    }
    t = t.replace(Regex("\\{([^{}]+)\\}")) { it.groupValues[1] }

    // === 2. Убираем markdown-разметку ===
    t = t.replace(Regex("^---+$", RegexOption.MULTILINE), "")
    t = t.replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
    t = t.replace("**", "")
    t = t.replace("__", "")

    // === 3. Нормализация пробелов (но НЕ трогаем переносы строк!) ===
    t = t.replace(Regex("[ \\t]+", RegexOption.MULTILINE), " ")  // только пробелы и табы
    t = t.replace(Regex(" *\n *"), "\n")                         // убираем пробелы вокруг \n

    // === 4. Разбиваем на строки и структурируем ===
    val rawLines = t.split("\n").map { it.trim() }

    val sb = StringBuilder()
    var answerPrinted = false

    for (line in rawLines) {
        if (line.isEmpty()) {
            // сохраняем один пустой рядок максимум
            if (sb.isNotEmpty() && !sb.endsWith("\n\n")) {
                sb.appendLine()
            }
            continue
        }

        val lower = line.lowercase()

        when {
            // Шаги
            lower.contains("шаг") && Regex("[1-9]").containsMatchIn(lower) -> {
                if (sb.isNotEmpty()) sb.appendLine()
                sb.appendLine("▸ $line")
                sb.appendLine()
            }

            // Заголовок ОТВЕТ — только один раз
            (lower.contains("ответ") || lower.contains("boxed")) && !answerPrinted -> {
                if (sb.isNotEmpty()) sb.appendLine()
                sb.appendLine("════════════════════════")
                sb.appendLine("ОТВЕТ")
                sb.appendLine("════════════════════════")
                sb.appendLine()
                answerPrinted = true

                val cleaned = line
                    .replace(Regex("(?i)ответ"), "")
                    .replace(Regex("(?i)boxed"), "")
                    .replace("✅", "")
                    .replace("✓", "")
                    .trim()
                if (cleaned.length > 2) {
                    sb.appendLine(cleaned)
                }
            }

            // Строки с галочками
            line.contains("✅") || line.contains("✓") -> {
                sb.appendLine(line.replace("✅", "✓"))
            }

            // Обычная строка
            else -> {
                sb.appendLine(line)
            }
        }
    }

    return sb.toString()
        .replace(Regex("\n{3,}"), "\n\n")
        .trim() + "\n"
}
