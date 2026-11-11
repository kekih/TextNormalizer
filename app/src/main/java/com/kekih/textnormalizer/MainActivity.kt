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
    val snackbarHostState = remember { SnackbarHostState() }

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
                        clipboardManager.getText()?.let {
                            text = it.text
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Вставить")
                    }
                    IconButton(onClick = {
                        if (text.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Копировать")
                    }
                    IconButton(onClick = { text = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    text = formatToNormalForm(text)
                },
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
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                "Вставь сюда текст...",
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

/**
 * Приводит грязный текст решения (с кучей $ $$ ### ---) к чистому читаемому виду.
 */
fun formatToNormalForm(input: String): String {
    if (input.isBlank()) return input

    var text = input
        .replace("\r\n", "\n")
        .replace("\r", "\n")

    // 1. Убираем все LaTeX-доллары и удвоенные
    text = text.replace("$$", "")
    text = text.replace("$", "")

    // 2. Убираем markdown-разделители и лишние #
    text = text.replace(Regex("^---+$", RegexOption.MULTILINE), "")
    text = text.replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")

    // 3. Нормализуем пробелы вокруг = и знаков
    text = text.replace(Regex("\\s*=\\s*"), " = ")
    text = text.replace(Regex("\\s*\\+\\s*"), " + ")
    text = text.replace(Regex("\\s*-\\s*"), " - ")

    // 4. Убираем множественные пустые строки
    text = text.replace(Regex("\n{3,}"), "\n\n")

    // 5. Чистим каждую строку
    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

    val sb = StringBuilder()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        when {
            // Заголовки шагов
            line.matches(Regex("(?i).*шаг\\s*\\d+.*")) ||
            line.matches(Regex("(?i)^шаг\\s*\\d+.*")) -> {
                sb.appendLine()
                sb.appendLine("▸ $line")
                sb.appendLine()
            }

            // "Ответ"
            line.contains("Ответ", ignoreCase = true) ||
            line.contains("boxed", ignoreCase = true) ||
            line.contains("✅") -> {
                sb.appendLine()
                sb.appendLine("════════════════════════════════")
                sb.appendLine("ОТВЕТ")
                sb.appendLine("════════════════════════════════")
                sb.appendLine()
                // следующий текст после "Ответ" тоже добавляем
                if (!line.equals("Ответ", ignoreCase = true) && !line.startsWith("✅")) {
                    sb.appendLine(cleanEquation(line))
                }
            }

            // Уравнения / формулы (содержат = или ^)
            line.contains("=") || line.contains("^") || line.contains("²") || line.contains("²") -> {
                sb.appendLine("  " + cleanEquation(line))
            }

            // Обычный текст
            else -> {
                // Если это просто номер пункта
                if (line.matches(Regex("\\d+\\.\\s*.*"))) {
                    sb.appendLine()
                    sb.appendLine(line)
                } else {
                    sb.appendLine(line)
                }
            }
        }
        i++
    }

    var result = sb.toString()
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    // Финальная полировка
    result = result
        .replace("y^2", "y²")
        .replace("x^2", "x²")
        .replace("^2", "²")
        .replace("dfrac", "")
        .replace("frac", "")
        .replace("\\", "")

    return result + "\n"
}

private fun cleanEquation(line: String): String {
    return line
        .replace(Regex("\\s*=\\s*"), " = ")
        .replace(Regex("\\s*\\+\\s*"), " + ")
        .replace(Regex("\\s*-\\s*"), " - ")
        .replace("**", "")
        .replace("__", "")
        .trim()
}
