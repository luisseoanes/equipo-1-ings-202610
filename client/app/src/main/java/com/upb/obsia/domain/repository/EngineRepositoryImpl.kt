package com.upb.obsia.data.repository

import android.content.Context
import com.obsIA.engine.NativeEngine
import com.upb.obsia.domain.model.EngineResponse
import com.upb.obsia.domain.repository.EngineRepository
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EngineRepositoryImpl(private val context: Context) : EngineRepository {

    private val engine = NativeEngine()
    private val initMutex = Mutex()
    private var initialized = false

    override suspend fun initialize(): Boolean {
        if (initialized) return true

        return initMutex.withLock {
            if (initialized) return@withLock true

            try {
                val modelFile = copyAssetIfNeeded("qwen-medicina-q2k.gguf")
                val chunksFile = copyAssetIfNeeded("chunks.json")

                val result =
                        withContext(Dispatchers.IO) {
                            engine.init(
                                    modelPath = modelFile.absolutePath,
                                    ragPath = chunksFile.absolutePath,
                                    nThreads = 4
                            )
                        }

                initialized = (result == 0)
                initialized
            } catch (e: Exception) {
                initialized = false
                false
            }
        }
    }

    override suspend fun query(text: String): EngineResponse {
        return try {
            val rawJson = withContext(Dispatchers.IO) { engine.processQuery(text) }
            parseResponse(rawJson)
        } catch (e: Exception) {
            EngineResponse.Failure(e.message ?: "Error desconocido en el motor")
        }
    }

    override fun release() {
        if (initialized) {
            engine.release()
            initialized = false
        }
    }

    override fun isReady(): Boolean = initialized

    private suspend fun copyAssetIfNeeded(assetName: String): File =
            withContext(Dispatchers.IO) {
                val dest = File(context.filesDir, assetName)
                if (!dest.exists()) {
                    context.assets.open(assetName).use { input ->
                        FileOutputStream(dest).use { output -> input.copyTo(output) }
                    }
                }
                dest
            }

    private fun parseResponse(rawJson: String): EngineResponse {
        return try {
            val json = JSONObject(rawJson)
            when (json.optString("status")) {
                "ok" ->
                        EngineResponse.Success(
                                responseText = json.optString("response_text"),
                                processingMs = json.optLong("processing_ms")
                        )
                else ->
                        EngineResponse.Failure(
                                json.optString("error_message", "Error al procesar la consulta")
                        )
            }
        } catch (e: Exception) {
            EngineResponse.Failure("Respuesta del motor con formato inválido")
        }
    }
}
