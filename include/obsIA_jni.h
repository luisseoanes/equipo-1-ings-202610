#pragma once
#include <jni.h>

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_obsIA_engine_NativeEngine_processQuery(
    JNIEnv* env, jobject thiz, jstring query);

JNIEXPORT jboolean JNICALL
Java_com_obsIA_engine_NativeEngine_isReady(
    JNIEnv* env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_obsIA_engine_NativeEngine_release(
    JNIEnv* env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_obsIA_engine_NativeEngine_getMemoryUsageMB(
    JNIEnv* env, jobject thiz);

} // extern "C"
