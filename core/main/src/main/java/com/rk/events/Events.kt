package com.rk.events

import com.rk.drawer.DrawerTab
import com.rk.extension.api.XedExtensionPoint
import com.rk.file.FileObject
import com.rk.tabs.base.Tab
import com.rk.tabs.editor.EditorTab
import com.rk.theme.ThemeConfig
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
        val projectRoot: String,
        val path: String,
    ) : FileTreeEvent

    data class NodeCollapsed(
        val projectRoot: String,
        val path: String,
    ) : FileTreeEvent

    /**
     * Event triggered when a specific path or the entire tree has been refreshed. The [TreeSynchronized] event will
     * also be triggered after this event.
     *
     * @see TreeSynchronized
     */
    data class Refreshed(val projectRoot: String, val path: String) : FileTreeEvent

    data class Focused(
        val projectRoot: String,
        val path: String,
    ) : FileTreeEvent

    data class SelectionChanged(
        val projectRoot: String,
        val selected: List<String>,
    ) : FileTreeEvent

    /** Event triggered when a file tree drawer tab has been selected. */
    data class Opened(val projectRoot: FileObject) : FileTreeEvent // TODO: HERE

    /**
     * Event triggered when the file tree structure has been updated/synchronized with the file system.
     *
     * **NOTE:** This event is not triggered on initial file tree load.
     */
    data class TreeSynchronized(val parent: FileObject) : FileTreeEvent // TODO: HERE
}

sealed interface FileEvent : Event {

    data class Created(val path: String) : FileEvent

    data class Deleted(val path: String) : FileEvent

    data class Renamed(
        val oldPath: String,
        val newPath: String,
    ) : FileEvent

    data class Moved(
        val oldPath: String,
        val newPath: String,
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

    data class Saved(val tab: EditorTab) : EditorTabEvent // TODO: HERE

    data class Opened(val tab: EditorTab) : EditorTabEvent

    data class Closed(val tab: EditorTab) : EditorTabEvent

    data class Reordered(
        val tab: EditorTab,
        val from: Int,
        val to: Int,
    ) : TabEvent

    data class Selected(val tab: EditorTab) : TabEvent
}

sealed interface ThemeEvent : Event {

    data class Changed(val newTheme: ThemeConfig, val oldTheme: ThemeConfig) : ThemeEvent
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
        listeners.filterKeys { it.isInstance(event) }.values.flatten().forEach { it(event) }
    }
}
