package com.rk.settings.extension

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rk.extension.Extension
import com.rk.extension.ExtensionId
import com.rk.extension.UpdatableExtension
import com.rk.extension.extensionManager
import com.rk.resources.drawables
import com.rk.resources.strings
import kotlinx.coroutines.CoroutineScope

@Composable
fun DependenciesDialog(
    extensionIds: List<ExtensionId>,
    softDependencies: Boolean,
    scope: CoroutineScope,
    context: Context,
    activity: AppCompatActivity?,
    onDismiss: () -> Unit,
    onCompletion: () -> Unit,
) {
    val allExtensionsInstalled = extensionIds.all { extensionManager.isInstalled(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (softDependencies) strings.recommendations else strings.missing_dependencies))
        },
        text = {
            Column {
                Text(
                    text =
                        stringResource(
                            if (softDependencies) strings.recommendations_description
                            else strings.dependencies_description
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(12.dp))

                extensionIds.forEach { extensionId ->
                    DependencyItem(
                        extensionId = extensionId,
                        onInstallClick = { dependency ->
                            ensureExtensionDependencies(dependency, scope, context, activity) {
                                runExtensionInstallAction(
                                    extension = dependency,
                                    updateInstallState = {},
                                    context = context,
                                    activity = activity,
                                )
                            }
                        },
                        onUpdateClick = { dependency ->
                            if (dependency !is UpdatableExtension) return@DependencyItem

                            ensureExtensionDependencies(dependency, scope, context, activity) {
                                runExtensionUpdateAction(
                                    extension = dependency,
                                    updateInstallState = {},
                                    context = context,
                                    activity = activity,
                                )
                            }
                        },
                        onUninstallClick = { dependency ->
                            runExtensionUninstallAction(
                                extension = dependency,
                                updateInstallState = {},
                                scope = scope,
                                activity = activity,
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            if (softDependencies) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(strings.close))
                }
            } else {
                TextButton(onClick = onCompletion, enabled = allExtensionsInstalled) {
                    Text(stringResource(strings.continue_action))
                }
            }
        },
        dismissButton =
            if (!softDependencies) {
                {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(strings.cancel))
                    }
                }
            } else null,
    )
}

@Composable
private fun DependencyItem(
    extensionId: ExtensionId,
    onInstallClick: (Extension) -> Unit,
    onUpdateClick: (Extension) -> Unit,
    onUninstallClick: (Extension) -> Unit,
) {
    val extension = extensionManager.getExtension(extensionId)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(extension?.iconUrl)
                    .fallback(drawables.extension)
                    .placeholder(drawables.extension)
                    .error(drawables.extension)
                    .crossfade(true)
                    .build(),
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            contentDescription = null,
        )

        Spacer(Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = extension?.name ?: extensionId,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            extension?.version?.let {
                Text(
                    text = "v$it",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        extension?.let {
            SmallExtensionActionButton(
                installState = InstallState.Idle,
                scope = rememberCoroutineScope(),
                onInstallClick = {
                    onInstallClick(it)
                },
                onUninstallClick = {
                    onUninstallClick(it)
                },
                onUpdateClick = {
                    onUpdateClick(it)
                },
            )
        }
    }
}
