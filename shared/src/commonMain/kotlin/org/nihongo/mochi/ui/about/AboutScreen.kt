package org.nihongo.mochi.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

// Expect/Actual or Platform specific utility for date and version info
// Since this is UI code in shared, we cannot access BuildConfig or Java Date directly if we want to be pure KMP.
// For now, we will use placeholders or expect/actual interfaces.
// But to fix compilation quickly, I'll remove the Android dependencies (BuildConfig, java.util.Date)
// and replace them with KMP equivalents (Kotlinx-datetime) or simple strings for now.

@Composable
fun AboutScreen(
    versionName: String, // Pass version from platform
    currentDate: String, // Pass date from platform
    onIssueTrackerClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onPatreonClick: () -> Unit,
    onTipeeeClick: () -> Unit,
    onKanjiDataClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.menu_about),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Image logo (if migrated)
                /*
                Image(
                    painter = painterResource(R.mipmap.nihongomochi),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(96.dp)
                )
                */
            }

            Text(
                text = stringResource(Res.string.about_version_info),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // textColorSecondary equivalent
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Informations Section
            AboutSectionCard(
                title = stringResource(Res.string.about_category_informations),
                icon = Icons.Default.Info
            ) {
                // Table equivalent
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = stringResource(Res.string.about_version_label),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = versionName,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(Res.string.about_date_label),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentDate,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FullWidthButton(
                    text = stringResource(Res.string.about_issue_tracker),
                    icon = Icons.Default.Send,
                    onClick = onIssueTrackerClick
                )
                
                FullWidthButton(
                    text = stringResource(Res.string.about_rate_app),
                    icon = Icons.Default.Star, // star_off
                    onClick = onRateAppClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Credits Section
            AboutSectionCard(
                title = stringResource(Res.string.about_category_credits),
                // Using info icon as placeholder for ic_menu_info_details if not vector
                icon = Icons.Default.Info 
            ) {
                // Design / Dev
                Text(
                    text = stringResource(Res.string.about_design_dev),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "LECOQ Vincent",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FullWidthButton(
                    text = stringResource(Res.string.about_patreon),
                    onClick = onPatreonClick
                )
                
                FullWidthButton(
                    text = stringResource(Res.string.about_tipeee),
                    onClick = onTipeeeClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Pedagogical Content
                Text(
                    text = stringResource(Res.string.about_pedagogical),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(Res.string.about_coming_soon),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resources Section
            AboutSectionCard(
                title = stringResource(Res.string.about_category_resources),
                // Using default icon as placeholder for ic_menu_gallery
                icon = Icons.Default.Info 
            ) {
                 FullWidthButton(
                    text = stringResource(Res.string.about_kanji_data_credit),
                    onClick = onKanjiDataClick
                )
            }
            
            // Extra padding at bottom for navigation bar if needed, though Scaffold usually handles it.
            // XML had paddingBottom="16dp" on ScrollView + padding inside.
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AboutSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        // Use a semi-transparent surface color to blend with background like other screens
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            content()
        }
    }
}

@Composable
fun FullWidthButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(4.dp) // Material 2 default or keep Material 3 rounded
    ) {
        // Material 3 Button content is RowScope
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text)
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon, 
                    contentDescription = null,
                    modifier = Modifier.size(18.dp) // Default icon size in button
                )
            }
        }
    }
}
