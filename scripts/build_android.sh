#!/bin/bash
# build_android.sh — Compiles libnative_engine.so for ARM64 and ARMv7
#
# Prerequisites:
#   - Android NDK installed (default: ~/Android/Sdk/ndk/26.1.10909125)
#   - CMake 3.22+ on PATH
#
# Usage:
#   ./scripts/build_android.sh
#
# Override NDK path:
#   ANDROID_NDK_ROOT=/path/to/ndk ./scripts/build_android.sh

set -e

NDK="${ANDROID_NDK_ROOT:-$HOME/Android/Sdk/ndk/26.1.10909125}"

if [[ ! -d "$NDK" ]]; then
    echo "❌ NDK not found at: $NDK"
    echo "   Set ANDROID_NDK_ROOT to the correct path and retry."
    exit 1
fi

echo "Using NDK: $NDK"
echo

for ABI in arm64-v8a armeabi-v7a; do
    echo "──────────────────────────────────────────"
    echo "Building ABI: $ABI"
    echo "──────────────────────────────────────────"

    cmake \
        -S . -B "build/android/$ABI" \
        -DCMAKE_TOOLCHAIN_FILE="$NDK/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="$ABI" \
        -DANDROID_PLATFORM="android-29" \
        -DCMAKE_BUILD_TYPE=Release

    cmake --build "build/android/$ABI" --parallel

    echo "✅ $ABI → build/android/$ABI/libnative_engine.so"
    echo
done

echo "Build complete."
