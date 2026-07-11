package com.rk.git

import com.rk.events.Event
import com.rk.file.FileObject

sealed interface GitEvent : Event {
    // TODO: Implement repo init, branch deletion, stash management, rebase, merge, conflicts
    // data class RepositoryInitialized(val path: String) : GitEvent

    data class RepositoryCloned(
        val remoteUrl: String,
        val branch: String,
        val destination: FileObject,
    ) : GitEvent

    data class BranchCreated(
        val root: FileObject,
        val name: String,
        val fromBranch: String?,
    ) : GitEvent

    // data class BranchDeleted(val name: String) : GitEvent

    data class BranchCheckedOut(val root: FileObject, val name: String) : GitEvent

    data class CommitCreated(
        val root: FileObject,
        val message: String,
    ) : GitEvent

    data class CommitAmended(
        val root: FileObject,
        val message: String,
    ) : GitEvent

    data class FetchCompleted(val root: FileObject, val remote: String, val branch: String) : GitEvent

    data class PullCompleted(
        val root: FileObject,
        val remote: String,
        val branch: String,
    ) : GitEvent

    data class PushCompleted(
        val root: FileObject,
        val remote: String,
        val branch: String,
        val force: Boolean = false,
    ) : GitEvent

    data class WorkingTreeUpdated(val root: FileObject, val changes: List<GitChange>) : GitEvent
}
