package com.saidi.busassistant.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saidi.busassistant.R
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.ui.theme.*
import com.saidi.busassistant.ui.viewmodel.RealTimeDataDisplay

/**
 * Bus line departure card.
 * Displays real-time departure countdown, boarding stop, and progress bar for a single route.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusLineCard(
    line: BusLineEntity,
    realTimeData: RealTimeDataDisplay?,
    onDelete: () -> Unit,
    onLabelClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Route metadata & action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Line Number & Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BluePrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = line.lineName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${line.startStation} → ${line.endStation}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GrayText
                        )
                    }
                }

                // Right: Label & Delete actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (line.userLabel != null) {
                        UserLabelChip(
                            label = line.userLabel,
                            onClick = onLabelClick
                        )
                    } else {
                        IconButton(
                            onClick = onLabelClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(R.string.add_label),
                                tint = GrayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = GrayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Boarding stop info
            Text(
                text = stringResource(R.string.boarding_station, line.userBoardingStation),
                style = MaterialTheme.typography.labelMedium,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time canvas progress bar
            BusProgressBar(
                closestBus = realTimeData?.closestBus,
                modifier = Modifier.fillMaxWidth()
            )

            // Footer: Distance and countdown details
            realTimeData?.closestBus?.let { bus ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val detailText = buildString {
                        append(stringResource(R.string.stops_away, bus.stationsAway))
                        if (bus.minutesAway > 0) {
                            append(" · ")
                            append(stringResource(R.string.about_minutes, bus.minutesAway))
                        }
                    }
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText
                    )

                    if (bus.isArriving) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GreenSuccess.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = stringResource(R.string.arriving_soon),
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_line_title)) },
            text = { Text(stringResource(R.string.delete_line_message, line.lineName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = RedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * User label chip component.
 */
@Composable
fun UserLabelChip(
    label: String,
    onClick: () -> Unit
) {
    val (icon, color, labelText) = when (label) {
        "work" -> Triple(Icons.Default.Work, BluePrimary, R.string.label_work)
        "home" -> Triple(Icons.Default.Home, TealAccent, R.string.label_home)
        "school" -> Triple(Icons.Default.School, OrangeWarning, R.string.label_school)
        "shopping" -> Triple(Icons.Default.ShoppingCart, Color(0xFF8B5CF6), R.string.label_shopping)
        "hospital" -> Triple(Icons.Default.LocalHospital, RedAlert, R.string.label_hospital)
        else -> Triple(Icons.Default.Star, GrayText, R.string.label_custom)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(labelText),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * User label selection dialog.
 */
@Composable
fun LabelSelectionDialog(
    currentLabel: String?,
    onDismiss: () -> Unit,
    onLabelSelected: (String?) -> Unit
) {
    val labels = listOf(
        Triple("work", Icons.Default.Work, R.string.label_work),
        Triple("home", Icons.Default.Home, R.string.label_home),
        Triple("school", Icons.Default.School, R.string.label_school),
        Triple("shopping", Icons.Default.ShoppingCart, R.string.label_shopping),
        Triple("hospital", Icons.Default.LocalHospital, R.string.label_hospital)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEach { (key, icon, labelRes) ->
                    val isSelected = currentLabel == key
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLabelSelected(key) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            BluePrimary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(labelRes),
                                tint = if (isSelected) BluePrimary else GrayText
                            )
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (currentLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { onLabelSelected(null) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.clear_label), color = RedAlert)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
