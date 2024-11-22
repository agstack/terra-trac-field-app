package org.technoserve.farmcollector.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R

/**
 *  This function is used to display the message when the user captures the invalid polygon
 */

@Composable
fun InvalidPolygonDialog(
    showDialog: MutableState<Boolean>,
    onDismiss: () -> Unit
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = stringResource(id = R.string.invalid_polygon_title)) },
            text = { Text(text = stringResource(id = R.string.invalid_polygon_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
}
