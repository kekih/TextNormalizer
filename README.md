# Text Normalizer

Простое Android-приложение на **Kotlin + Jetpack Compose**.

## Что умеет

- Полноценный текстовый редактор (вставка / копирование / очистка)
- Кнопка **«Исправить»** — приводит текст к более чистому и структурированному виду
- Особенно хорошо работает с пошаговыми решениями уравнений, математическими текстами и черновиками
- Тёмная тема в стиле Aether (ink + amber)

## Как собрать

1. Открой проект в **Android Studio** (Hedgehog / Ladybug или новее)
2. Дождись синхронизации Gradle
3. Если попросит — сгенерируй иконки (File → New → Image Asset)
4. Нажми Run ▶ или `Build → Build Bundle(s) / APK(s) → Build APK(s)`

## Структура

```
app/
  src/main/java/com/kekih/textnormalizer/
    MainActivity.kt          ← вся логика + UI + форматтер
```

Форматтер сейчас **rule-based** (без интернета и API-ключей).  
Можно легко заменить функцию `formatToNormalForm()` на вызов своего бэкенда / Gemini / Grok API.

## Автор

Сделано для @kekih
