package com.rk.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.resources.getString
import com.rk.resources.strings

/** A Preference that shows a list of options in a dialog when clicked. */
@Composable
fun <T> PreferenceList(
    label: String,
    description: String?,
    items: List<Pair<T, String>>, // T to String Resource ID
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    PreferenceTemplate(
        modifier =
            modifier.clickable(
                enabled = enabled,
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = { showDialog = true },
            ),
        contentModifier = Modifier.fillMaxHeight().padding(vertical = 16.dp).padding(start = 16.dp),
        title = { Text(text = label) },
        description = { description?.let { Text(text = it) } },
        enabled = enabled,
        applyPaddings = false,
    )

    if (showDialog) {
        var tempSelectedItem by remember { mutableStateOf(selectedItem) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = label) },
            text = {
                Column {
                    items.forEach { (item, string) ->
                        PreferenceTemplate(
                            modifier =
                                Modifier.clip(MaterialTheme.shapes.large).clickable {
                                    tempSelectedItem = item
                                },
                            title = { Text(text = string) },
                            startWidget = {
                                RadioButton(selected = tempSelectedItem == item, onClick = null)
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onItemSelected(tempSelectedItem)
                    }
                ) {
                    Text(text = strings.apply.getString())
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = strings.cancel.getString())
                }
            },
        )
    }
}
