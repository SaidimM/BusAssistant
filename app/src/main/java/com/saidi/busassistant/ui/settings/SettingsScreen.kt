package com.saidi.busassistant.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saidi.busassistant.R
import com.saidi.busassistant.ui.theme.*
import com.saidi.busassistant.ui.viewmodel.HomeViewModel

/**
 * Settings Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onHabitInsightsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data & Privacy
            SettingsSection(title = stringResource(R.string.data_and_privacy)) {
                // Habit insights dashboard entry
                SettingsClickableItem(
                    icon = Icons.Default.Psychology,
                    title = stringResource(R.string.habit_insights_title),
                    subtitle = stringResource(R.string.habit_learning_subtitle),
                    iconTint = BluePrimary,
                    onClick = onHabitInsightsClick
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Location
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.use_location),
                    subtitle = stringResource(R.string.use_location_subtitle),
                    checked = true,
                    onCheckedChange = { /* TODO */ }
                )

                // Habit recording
                SettingsSwitchItem(
                    icon = Icons.Default.BarChart,
                    title = stringResource(R.string.record_usage_habits),
                    subtitle = stringResource(R.string.record_usage_habits_subtitle),
                    checked = true,
                    onCheckedChange = { /* TODO */ }
                )
            }

            // Data Management
            SettingsSection(title = stringResource(R.string.data_management)) {
                SettingsClickableItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.clear_learning_data),
                    subtitle = stringResource(R.string.clear_learning_data_subtitle),
                    iconTint = RedAlert,
                    onClick = { showClearConfirm = true }
                )
            }

            // About
            SettingsSection(title = stringResource(R.string.about)) {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.data_note),
                    subtitle = stringResource(R.string.data_note_subtitle)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.version),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1.1.0-radar",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = stringResource(R.string.footer_text),
                style = MaterialTheme.typography.labelSmall,
                color = GrayText.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Clear confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_data_title)) },
            text = {
                Text(stringResource(R.string.clear_data_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLearningData()
                        showClearConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.clear), color = RedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Settings section container.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = GrayText,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Settings toggle item.
 */
@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = BluePrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = GrayText
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BluePrimary,
                checkedTrackColor = BluePrimary.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * Clickable settings item.
 */
@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = BluePrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = GrayText
            )
        }
    }
}

/**
 * Read-only information settings item.
 */
@Composable
private fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = GrayText
            )
        }
    }
}
