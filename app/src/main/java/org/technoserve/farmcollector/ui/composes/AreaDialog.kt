package org.technoserve.farmcollector.ui.composes

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R

/**
 * This popup dialog allows you to choose from the entered size and calculated area from the captured polygon.
 */

const val CALCULATED_AREA_OPTION = "CALCULATED_AREA"
const val ENTERED_AREA_OPTION = "ENTERED_AREA"

@SuppressLint("DefaultLocale")
@Composable
fun AreaDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    calculatedArea: Double,
    enteredArea: Double,
) {
    val context = LocalContext.current
    // Calculate the threshold as 30% of the entered area
    val threshold = enteredArea * 0.30

    if (showDialog) {
        val calculatedAreaString =
            stringResource(
                id = R.string.calculated_area,
                String.format("%.9f", calculatedArea),
            )
        val enteredAreaString =
            stringResource(
                id = R.string.entered_size,
                String.format("%.2f", enteredArea),
            )
        val options =
            listOf(
                CALCULATED_AREA_OPTION to calculatedAreaString,
                ENTERED_AREA_OPTION to enteredAreaString,
            )
        val difference = Math.abs(calculatedArea - enteredArea)
        val showWarning = difference > threshold
        val warningMessage = if (showWarning) {
            stringResource(id = R.string.warning_difference, difference)
        } else {
            null
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = stringResource(id = R.string.choose_area))
            },
            text = {
                Column {
                    Text(text = stringResource(id = R.string.please_choose_area))
                    if (showWarning) {
                        warningMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error // Highlight warning
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        onConfirm(CALCULATED_AREA_OPTION)
                        Toast.makeText(context, R.string.polygon_saved, Toast.LENGTH_LONG).show()
                    }) {
                        Text(text = options[0].second)
                    }
                    TextButton(onClick = { onConfirm(ENTERED_AREA_OPTION) }) {
                        Text(text = options[1].second)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
}
