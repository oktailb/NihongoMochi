package org.nihongo.mochi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit = {},
    onSaveAndExit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Met le jeu en pause quand le dialogue apparaît, et reprend quand il disparaît
    DisposableEffect(Unit) {
        onPause()
        onDispose {
            onResume()
        }
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.exit_dialog_title)) },
        text = { 
            Column {
                Text(text = stringResource(Res.string.exit_dialog_message))
                if (onSaveAndExit == null) {
                    Text(
                        text = stringResource(Res.string.exit_dialog_lose_progress),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bouton principal : Reprendre
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.exit_dialog_resume))
                }

                // Option de sauvegarde
                if (onSaveAndExit != null) {
                    OutlinedButton(
                        onClick = onSaveAndExit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.exit_dialog_pause_exit))
                    }
                }

                // Action destructrice
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.exit_dialog_quit_lose_progress))
                }
            }
        },
        dismissButton = null // On gère tout dans confirmButton pour éviter les superpositions
    )
}
