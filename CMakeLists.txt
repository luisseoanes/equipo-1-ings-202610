cmake_minimum_required(VERSION 3.22)
project(obsIA_native_engine CXX)
set(CMAKE_CXX_STANDARD 17)

# ── ABI-specific compiler flags ───────────────────────────────────────────────
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a+dotprod -O3")
elseif(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv7-a -O2")
endif()

# ── Include paths ─────────────────────────────────────────────────────────────
include_directories(include)
include_directories(third_party/nlohmann)

# ── Shared library: libnative_engine.so ──────────────────────────────────────
add_library(native_engine SHARED
    src/triage/triage_engine.cpp
    src/jni/obsIA_jni.cpp
)

# android  — provides <android/log.h> and Android-specific APIs
# log      — liblog.so (LOGI / LOGE macros)
target_link_libraries(native_engine android log)
