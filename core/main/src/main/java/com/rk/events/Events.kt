package com.rk.events

import com.rk.drawer.DrawerTab
import com.rk.editor.Editor
import com.rk.extension.api.XedExtensionPoint
import com.rk.file.FileObject
import com.rk.icons.pack.IconPack
import com.rk.lsp.LspConnectionStatus
import com.rk.lsp.LspLogEntry
import com.rk.lsp.LspServerInstance
import com.rk.settings.debugOptions.LogEntry
import com.rk.tabs.base.Tab
import com.rk.tabs.editor.EditorTab
import com.rk.theme.ThemeHolder
import com.rk.utils.logError
import java.util.Locale
import kotlin.reflect.KClass

interface Event

sealed interface DrawerEvent : Event {

    /** Event triggered when a drawer tab has been added. */
    data class TabAdded(val tab: DrawerTab) : DrawerEvent

    /** Event triggered when a drawer tab has been removed. */
    data class TabRemoved(val tab: DrawerTab) : DrawerEvent

    /** Event triggered when the active drawer tab changes. */
    data class TabSelected(val tab: DrawerTab?) : DrawerEvent

    /** Event triggered when the registered service tabs have been initialized. */
    data class ServicesInitialized(val tabs: List<DrawerTab>) : DrawerEvent

    /** Event triggered when the active service tab changes. */
    data class ServiceTabSelected(val tab: DrawerTab?) : DrawerEvent
}

sealed interface FileTreeEvent : Event {

    data class NodeExpanded(
        val projectRoot: FileObject,
        val path: FileObject,
    ) : FileTreeEvent

    data class NodeCollapsed(
        val projectRoot: FileObject,
        val path: FileObject,
    ) : FileTreeEvent

    data class Focused(
        val projectRoot: FileObject,
        val path: FileObject,
    ) : FileTreeEvent

    data class SelectionChanged(
        val projectRoot: FileObject,
        val selected: List<FileObject>,
    ) : FileTreeEvent

    /** Event triggered when a file tree drawer tab has been selected. */
    data class Opened(val projectRoot: FileObject) : FileTreeEvent

    /** Event triggered when a file tree drawer tab has been closed. */
    data class Closed(val projectRoot: FileObject) : FileTreeEvent

    /**
     * Event triggered when the file tree structure has been updated/synchronized with the file system. This can occur
     * after files have been moved or the refresh button has been pressed.
     *
     * **NOTE:** This event is not triggered on initial file tree load.
     */
    data class TreeSynchronized(val parent: FileObject) : FileTreeEvent
}

sealed interface FileEvent : Event {

    data class Created(val file: FileObject) : FileEvent

    data class Deleted(val path: String) : FileEvent

    data class Renamed(
        val file: FileObject,
        val oldPath: String,
    ) : FileEvent

    data class Moved(
        val file: FileObject,
        val oldPath: String,
    ) : FileEvent

    data class Copied(
        val file: FileObject,
        val sourcePath: String,
    ) : FileEvent
}

sealed interface TabEvent : Event {

    data class Opened(val tab: Tab) : TabEvent

    data class Closed(val tab: Tab) : TabEvent

    data class Reordered(
        val tab: Tab,
        val from: Int,
        val to: Int,
    ) : TabEvent

    data class Selected(val tab: Tab) : TabEvent
}

sealed interface EditorTabEvent : Event {

    data class Refreshed(val tab: EditorTab) : EditorTabEvent

    data class Saved(val tab: EditorTab) : EditorTabEvent

    data class Opened(val tab: EditorTab) : EditorTabEvent

    data class Closed(val tab: EditorTab) : EditorTabEvent

    data class Reordered(
        val tab: EditorTab,
        val from: Int,
        val to: Int,
    ) : TabEvent

    data class Selected(val tab: EditorTab) : TabEvent
}

sealed interface EditorEvent : Event {
    data class InstanceCreated(val editor: Editor) : EditorEvent

    data class InstanceDestroyed(val editor: Editor) : EditorEvent
}

sealed interface LSPEvent : Event {

    data class InstanceCreated(val instance: LspServerInstance) : LSPEvent

    data class StatusChanged(
        val instance: LspServerInstance,
        val newStatus: LspConnectionStatus,
        val oldStatus: LspConnectionStatus,
    ) : LSPEvent

    data class LogEntryWritten(val instance: LspServerInstance, val logEntry: LspLogEntry) : LSPEvent
}

sealed interface AppEvent : Event {

    data class ThemeChanged(val newTheme: ThemeHolder, val oldTheme: ThemeHolder?) : AppEvent

    data class IconPackChanged(val newIconPack: IconPack?, val oldIconPack: IconPack?) : AppEvent

    data class LanguageChanged(val newLanguage: Locale, val oldLanguage: Locale?) : AppEvent

    data class LogEntryWritten(val logEntry: LogEntry, val extensionId: String?) : AppEvent
}

interface Subscription {
    fun unsubscribe()
}

object Events {

    @PublishedApi internal val listeners = mutableMapOf<KClass<out Event>, MutableList<suspend (Event) -> Unit>>()

    @XedExtensionPoint
    inline fun <reified T : Event> subscribe(noinline listener: suspend (T) -> Unit): Subscription {
        val list = listeners.getOrPut(T::class) { mutableListOf() }

        val wrapper: suspend (Event) -> Unit = {
            listener(it as T)
        }

        list += wrapper

        return object : Subscription {
            override fun unsubscribe() {
                list -= wrapper
            }
        }
    }

    suspend fun publish(event: Event) {
        listeners
            .filterKeys { it.isInstance(event) }
            .values
            .flatten()
            .forEach { listener ->
                try {
                    listener(event)
                } catch (t: Throwable) {
                    logError(t, "Listener failed for ${event::class.simpleName}")
                }
            }
    }
}
