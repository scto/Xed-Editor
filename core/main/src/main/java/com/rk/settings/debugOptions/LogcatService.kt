package com.rk.settings.debugOptions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.mutableStateListOf
import com.rk.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LogcatService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var logcatProcess: Process? = null

    companion object {
        val logcatLogs = mutableStateListOf<String>()

        private val _logFlow = MutableSharedFlow<String>(extraBufferCapacity = 512)
        val logFlow = _logFlow.asSharedFlow()

        fun start(context: Context) {
            val intent = Intent(context, LogcatService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LogcatService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun shouldCaptureLog(line: String): Boolean {
        if (line.isEmpty()) {
            return false
        }

        // Filter out known noisy system verbose logs
        val noisyKeywords = listOf(
            "ViewRootImpl", "Choreographer", "OpenGLRenderer", "InputMethodManager", 
            "RenderThread", "libEGL", "EGL_emulation", "CompatChangeReporter", "vndksupport",
            "ConfigStore", "Mono", "me.weishu.reflection", "chatty", "HostConnection", "gralloc","GestureDetector","WindowManager","ImeFocusController","Insets"
        )
        if (noisyKeywords.any { line.contains(it, ignoreCase = true) }) {
            return false
        }

        if (!line.contains('/') || !line.contains('(')) {
            return true
        }

        val slashIdx = line.indexOf('/')
        val parenIdx = line.indexOf('(')
        if (slashIdx != 1 || parenIdx <= slashIdx) {
            return true
        }

        val priority = line[0]
        val tag = line.substring(slashIdx + 1, parenIdx).trim()

        // 1. Capture warning + error + fatal + assert logs
        if (priority == 'W' || priority == 'E' || priority == 'F' || priority == 'A') {
            return true
        }

        // 2. Capture System.* calls
        if (tag.startsWith("System.out") || tag.startsWith("System.err")) {
            return true
        }

        // 3. Default to capturing
        return true
    }

    override fun onCreate() {
        super.onCreate()

        // Start collecting logcat
        serviceScope.launch {
            try {
                // Clear any existing logs
                launch(Dispatchers.Main) {
                    logcatLogs.clear()
                }

                // Run logcat continuously in brief format
                val process = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "brief"))
                logcatProcess = process
                
                val batch = mutableListOf<String>()
                var lastUpdateTime = System.currentTimeMillis()

                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if (shouldCaptureLog(line)) {
                            _logFlow.tryEmit(line)
                            synchronized(batch) {
                                batch.add(line)
                            }

                            val now = System.currentTimeMillis()
                            if (batch.size >= 50 || now - lastUpdateTime > 100) {
                                val itemsToAdd = synchronized(batch) {
                                    val copy = batch.toList()
                                    batch.clear()
                                    copy
                                }
                                lastUpdateTime = now
                                launch(Dispatchers.Main) {
                                    synchronized(logcatLogs) {
                                        logcatLogs.addAll(itemsToAdd)
                                        if (logcatLogs.size > 1000) {
                                            val toRemove = logcatLogs.size - 1000
                                            logcatLogs.removeRange(0, toRemove)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add any remaining items
                if (batch.isNotEmpty()) {
                    launch(Dispatchers.Main) {
                        synchronized(logcatLogs) {
                            logcatLogs.addAll(batch)
                            if (logcatLogs.size > 1000) {
                                val toRemove = logcatLogs.size - 1000
                                logcatLogs.removeRange(0, toRemove)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP") {
            Settings.enable_logcat = false
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        logcatProcess?.destroy()
        //logcatLogs.clear()
        super.onDestroy()
    }
}
