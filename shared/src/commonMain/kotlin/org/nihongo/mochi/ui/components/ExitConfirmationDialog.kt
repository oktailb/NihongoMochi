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
        title = { Text(text = "Partie en pause") },
        text = { 
            Column {
                Text(text = "Voulez-vous vraiment quitter ?")
                if (onSaveAndExit == null) {
                    Text(
                        text = "La progression de cette partie sera perdue.",
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
                    Text("Reprendre la partie")
                }

                // Option de sauvegarde
                if (onSaveAndExit != null) {
                    OutlinedButton(
                        onClick = onSaveAndExit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mettre en pause et quitter")
                    }
                }

                // Action destructrice
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Quitter et perdre la progression")
                }
            }
        },
        dismissButton = null // On gère tout dans confirmButton pour éviter les superpositions
    )
}
