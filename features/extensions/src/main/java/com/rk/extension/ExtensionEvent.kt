package com.rk.extension

import com.rk.events.Event

sealed interface ExtensionEvent : Event {

    data class Installed(val extension: Extension) : ExtensionEvent

    data class Loaded(val extension: LocalExtension) : ExtensionEvent

    data class Crashed(val extension: Extension) : ExtensionEvent

    data class Uninstalled(val extension: Extension, val isUpdate: Boolean) : ExtensionEvent
}
