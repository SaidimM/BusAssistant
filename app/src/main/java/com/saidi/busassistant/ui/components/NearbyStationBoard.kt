package com.saidi.busassistant.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saidi.busassistant.R
import com.saidi.busassistant.data.model.NearbyLineArrival
import com.saidi.busassistant.data.model.NearbyStationUiState
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GreenSuccess
import com.saidi.busassistant.ui.theme.OrangeWarning

/**
 * Nearby Bus Stop Real-Time Radar Card (Zero-Interaction Nearby Board)
 * Launches immediately showing user's closest physical station and passing departures
 */
@Composable
fun NearbyStationBoard(
    state: NearbyStationUiState,
    onToggleDirection: () -> Unit,
    onSaveFavoriteLine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val station = state.activeStation ?: return

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ========== Header: Station Name & Proximity ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = station.stationName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenSuccess.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.distance_walk_format,
                                        state.distanceMeters,
                                        state.walkingMinutes
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = station.directionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Flip direction button
                if (station.oppositeStationId != null) {
                    FilledTonalIconButton(
                        onClick = onToggleDirection,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = stringResource(R.string.flip_direction),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ========== Habit Match Banner (If Triggered) ==========
            state.detectedHabitSummary?.let { habitText ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BluePrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = habitText,
                        style = MaterialTheme.typography.labelSmall,
                        color = BluePrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========== Fastest Approaching Hero Banner ==========
            val fastest = state.fastestArrival
            if (fastest != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (fastest.isArriving)
                        GreenSuccess.copy(alpha = 0.14f)
                    else
                        BluePrimary.copy(alpha = 0.10f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (fastest.isArriving) GreenSuccess else BluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Line ${fastest.lineNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fastest.isArriving)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        BluePrimary
                                )
                                Text(
                                    text = if (fastest.isArriving)
                                        stringResource(R.string.arriving_soon)
                                    else
                                        stringResource(R.string.fastest_bus_prompt, fastest.arrivalMinutes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (fastest.arrivalMinutes == 0)
                                    stringResource(R.string.within_one_minute)
                                else
                                    stringResource(R.string.about_minutes, fastest.arrivalMinutes),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = if (fastest.isArriving) GreenSuccess else BluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(10.dp))

            // ========== Passing Lines List ==========
            Text(
                text = stringResource(R.string.passing_lines_count, state.arrivals.size),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.arrivals.forEach { arrival ->
                    NearbyArrivalRow(
                        arrival = arrival,
                        onSaveFavorite = { onSaveFavoriteLine(arrival.lineNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyArrivalRow(
    arrival: NearbyLineArrival,
    onSaveFavorite: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (arrival.isFastest)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (arrival.isFastest) BluePrimary else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Line ${arrival.lineNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (arrival.isFastest) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.stops_away, arrival.stationsAway),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mins = arrival.arrivalMinutes
                val (badgeBg, textColor) = when {
                    mins <= 2 -> Pair(GreenSuccess.copy(alpha = 0.15f), GreenSuccess)
                    mins <= 6 -> Pair(OrangeWarning.copy(alpha = 0.15f), OrangeWarning)
                    else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = if (mins == 0)
                            stringResource(R.string.arriving_soon)
                        else
                            stringResource(R.string.about_minutes, mins),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onSaveFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.pin_to_favorites),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
