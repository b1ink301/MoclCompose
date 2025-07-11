package kr.b1ink.coil

import java.util.concurrent.ConcurrentHashMap

const val HEADER_X_PROGRESS_ID = "X-Progress-ID"

internal object ProgressManager {
    private val listeners = ConcurrentHashMap<String, (progress: Float) -> Unit>()

    fun register(id: String, listener: (progress: Float) -> Unit) {
        listeners[id] = listener
    }

    fun unregister(id: String) {
        listeners.remove(id)
    }

    internal fun onProgress(id: String, progress: Float) {
        listeners[id]?.invoke(progress)
    }
}