# Deployment Guide — ObsIA

ObsIA is a fully offline Android application. "Deployment" means building a signed/debug APK and distributing it so testers or end users can install it on a physical ARM64 Android device.

There is no server to deploy. All AI inference, RAG retrieval, and clinical rules run on-device.

---

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 (Temurin) | Set `JAVA_HOME` |
| Android SDK | API 35 | Set `ANDROID_HOME` |
| Android NDK | r26+ | Side-by-side install via SDK Manager |
| CMake | 3.22+ | Via SDK Manager or system install |
| Ninja | Any | Required by CMake Android toolchain |

---

## 1. Build the Native C++ Library (if changed)

The precompiled `.so` is checked in at `app/src/main/jniLibs/arm64-v8a/libobsia_jni.so`. You only need to rebuild it if you changed files inside `Modelo/modeloFinal/`.

```bash
cd Modelo/modeloFinal
mkdir -p build_android_arm64 && cd build_android_arm64

cmake -G "Ninja" \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI=arm64-v8a \
  -DANDROID_PLATFORM=android-26 \
  -DCMAKE_BUILD_TYPE=Release ..

cmake --build . --config Release --parallel 4

# Copy the output to the jniLibs folder
cp libobsia_jni.so ../../equipo-1-ings-202610-development/app/src/main/jniLibs/arm64-v8a/
```

---

## 2. Place the Model Asset

The quantized LLM is not tracked in git (>1 GB). Before building, copy the model file into assets:

```
app/src/main/assets/qwen2.5-3b-instruct-q4_k_m.gguf
```

Make sure it is listed in `.gitignore` to avoid accidentally committing it.

---

## 3. Build the Debug APK

```bash
cd equipo-1-ings-202610-development

# Windows (sets JAVA_HOME, ANDROID_HOME, NDK paths automatically)
../build_arro.bat

# Or manually
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Install on Device

```bash
# Via ADB (device connected via USB with USB Debugging enabled)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or transfer the APK file to the device and open it to install manually (allow "Install from unknown sources" in device settings).

---

## 5. Automated CI/CD Pipeline

The project uses GitHub Actions (`.github/workflows/ci.yml`) for automated build, test, and release.

| Branch | CI Behavior |
|---|---|
| `development` | Build + unit tests on every push and PR |
| `staging` | Build + unit tests + creates a **pre-release** APK on GitHub Releases |
| `main` | Build + unit tests + creates a **production release** APK on GitHub Releases (triggered by `feat:` or `fix:` commits) |

### How the automatic release works

1. Push to `main` with a commit message starting with `feat:` or `fix:`.
2. CI builds the APK and runs unit tests.
3. If tests pass, a new GitHub Release is created automatically with semantic versioning (`v1.2.3`) and the APK attached.
4. Users download the APK from the GitHub Releases page.

### Accessing released APKs

Navigate to the repository on GitHub → **Releases** (right sidebar) → download `app-debug.apk` from the latest release.

---

## 6. Device Requirements

| Requirement | Minimum |
|---|---|
| Android version | 8.0 (API 26) |
| Architecture | ARM64-v8a only |
| RAM | 6 GB recommended (4 GB minimum) |
| Storage | ~2 GB free (model asset) |

---

## Notes

- The app works completely offline after installation. No internet access is required at runtime.
- The Vosk speech recognition model is bundled inside the APK assets.
- KV-cache is stored in device RAM during the session and cleared on app close.
