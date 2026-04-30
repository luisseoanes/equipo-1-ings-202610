# ObsIA: Android Client Module

This folder contains the core Android application for **ObsIA**, built using Kotlin and Jetpack Compose. It handles the user interface, local database persistence, voice-to-text processing, and interaction with the native AI engine.

## 1. What does this folder do?
This is the main client-side application. It provides:
- **Chat Interface:** A modern, reactive UI for interacting with the clinical AI.
- **Local Persistence:** Management of patient data and chat history using Room/SQLite.
- **Voice Recognition:** Integration with Vosk for offline speech-to-text.
- **Native AI Integration:** JNI layer to communicate with the C++ inference engine.

---

## 2. How to install this part of the project?
1. Ensure you are in the project root.
2. The installation of this module is handled by Gradle when syncing the project in Android Studio.
3. If you need to rebuild the native libraries, navigate to the source of the native code (outside this folder) and follow its specific build instructions, then copy the `.so` files to `src/main/jniLibs/`.

---

## 3. How to run this part of the project?
- **From Android Studio:** Select the `app` configuration and click **Run**.
- **From CLI:**
  ```bash
  ./gradlew :app:installDebug
  ```

### Required Environment Variables
- **None:** The app is designed for offline use and does not currently require external API keys.

---

## 4. What standards must be considered in this part of the project?
- **Framework:** Jetpack Compose (MVVM Architecture).
- **Naming:**
    - Classes: `PascalCase`
    - Functions/Variables: `camelCase`
    - Layouts/Resources: `snake_case`
- **Dependency Injection:** Koin.
- **Documentation:** KDoc for all internal logic.
- **Linting:** Standard Android Lint rules.

---

## 5. What version of JS, Python, or Java does it use?
- **Language:** Kotlin 2.1.0 / Java 11
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)
- **Compose Compiler:** Integrated with Kotlin 2.1.0

---

## 6. What do I need for the database?
- **Engine:** Room Persistence Library (SQLite).
- **Setup:** Defined in `com.upb.obsia.data.local.AppDatabase`.
- **Migrations:** Located in the `migrations` package within the data layer.

---

## 📂 Internal Folder Structure
- `src/main/java/`: Kotlin source code.
- `src/main/res/`: UI resources (strings, themes, layouts).
- `src/main/assets/`: Pre-indexed RAG data and LLM models.
- `src/main/jniLibs/`: Precompiled native libraries for ARM64.

<!-- TODO: document the process for adding new RAG documents to the assets folder -->
