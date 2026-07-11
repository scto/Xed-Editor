package com.rk.runner

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.rk.DefaultScope
import com.rk.events.Events
import com.rk.extension.api.XedExtensionPoint
import com.rk.file.FileObject
import com.rk.icons.Icon
import com.rk.runner.runners.web.html.HtmlRunner
import com.rk.runner.runners.web.markdown.MarkdownRunner
import com.rk.utils.errorDialog
import kotlinx.coroutines.launch

object RunnerManager {

    private val _extensionRunners = mutableStateListOf<Runner>()

    val extensionRunners: List<Runner>
        get() = _extensionRunners.toList()

    val builtinRunners = listOf(HtmlRunner, MarkdownRunner)

    @XedExtensionPoint
    fun registerRunner(runner: Runner) {
        if (!_extensionRunners.contains(runner)) {
            _extensionRunners.add(runner)
        }
    }

    @XedExtensionPoint
    fun unregisterRunner(runner: Runner) {
        _extensionRunners.remove(runner)
    }

    fun isRunnable(fileObject: FileObject): Boolean {
        return getAvailableRunners(fileObject).isNotEmpty()
    }

    fun getAvailableRunners(fileObject: FileObject): List<Runner> {
        val result = mutableListOf<Runner>()

        val runners = builtinRunners + extensionRunners + ShellBasedRunners.runners
        runners.forEach {
            if (it.isEnabled() && it.matcher(fileObject)) {
                result.add(it)
            }
        }

        return result
    }

    fun run(activity: Activity, fileObject: FileObject, onMultipleRunners: (List<RunnableOption>) -> Unit) {
        val availableRunners = getAvailableRunners(fileObject)

        if (availableRunners.isEmpty()) {
            errorDialog(activity, msg = "No runners available")
            return
        }

        if (availableRunners.size == 1) {
            DefaultScope.launch {
                availableRunners.first().run(activity, fileObject)
                Events.publish(RunnerEvent.RunnerRun(availableRunners.first()))
            }
        } else {
            val options = availableRunners.map { runner ->
                object : RunnableOption {
                    override val label: String = runner.label

                    override fun getIcon(context: Context): Icon? = runner.getIcon(context)

                    override fun run(activity: Activity) {
                        DefaultScope.launch {
                            runner.run(activity, fileObject)
                            Events.publish(RunnerEvent.RunnerRun(runner))
                        }
                    }
                }
            }
            onMultipleRunners.invoke(options)
        }
    }
}
