# Text Normalizer

Простое Android-приложение на **Kotlin + Jetpack Compose**.

## Что умеет

- Полноценный текстовый редактор (вставка / копирование / очистка)
- Кнопка **«Исправить»** — приводит текст к более чистому и структурированному виду
- Особенно хорошо работает с пошаговыми решениями уравнений, математическими текстами и черновиками
- Тёмная тема в стиле Aether (ink + amber)

## CI / Сборка APK

Проект собирается **автоматически** через GitHub Actions на каждый push в `main`.

1. Зайди в вкладку **Actions** репозитория
2. Выбери последний успешный workflow **Build APK**
3. В низу страницы скачай artifact **TextNormalizer-debug**

Также можно запустить сборку вручную: Actions → Build APK → Run workflow.

## Как собрать локально

1. Открой проект в **Android Studio**
2. Дождись синхронизации Gradle
3. `Build → Build Bundle(s) / APK(s) → Build APK(s)`

## Структура

```
app/
  src/main/java/com/kekih/textnormalizer/
    MainActivity.kt          ← вся логика + UI + форматтер
.github/workflows/build.yml  ← CI
```

Форматтер сейчас **rule-based**. Можно легко заменить на вызов API.

## Автор

Сделано для @kekih
