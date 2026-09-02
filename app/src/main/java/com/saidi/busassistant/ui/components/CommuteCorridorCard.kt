package com.saidi.busassistant.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Bolt
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
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GreenSuccess
import com.saidi.busassistant.ui.theme.OrangeWarning
import com.saidi.busassistant.ui.viewmodel.CommuteCorridorUiState
import com.saidi.busassistant.ui.viewmodel.CorridorCandidateLine

/**
 * Commute Corridor Hero Card
 * Aggregates all passing candidate lines along the same origin-destination route
 */
@Composable
fun CommuteCorridorCard(
    corridorState: CommuteCorridorUiState,
    onSwitchCorridor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corridor = corridorState.corridor ?: return

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ========== Header ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BluePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = corridor.corridorTag.ifBlank { stringResource(R.string.commute_corridor_title) },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (corridorState.isAutoInferred) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = stringResource(R.string.stat_top_line),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${corridor.originStation} ➔ ${corridor.destinationStation}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Switch corridor button
                if (corridorState.availableCorridors.size > 1) {
                    FilledTonalIconButton(
                        onClick = onSwitchCorridor,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = stringResource(R.string.switch_corridor),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========== Fastest Departure Recommendation ==========
            val fastestLine = corridorState.recommendedLineNumber
            val earliestMins = corridorState.earliestArrivalMinutes

            if (fastestLine != null && earliestMins != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (earliestMins <= 2)
                        GreenSuccess.copy(alpha = 0.15f)
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
                                    .background(if (earliestMins <= 2) GreenSuccess else BluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Line $fastestLine",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (earliestMins <= 2)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        BluePrimary
                                )
                                Text(
                                    text = if (earliestMins <= 2)
                                        stringResource(R.string.arriving_soon)
                                    else
                                        stringResource(R.string.fastest_bus_prompt, earliestMins),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (earliestMins == 0)
                                    stringResource(R.string.within_one_minute)
                                else
                                    stringResource(R.string.about_minutes, earliestMins),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = if (earliestMins <= 2) GreenSuccess else BluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // ========== Walking time & arrival estimate ==========
            corridorState.estimatedOfficeArrivalText?.let { arrivalText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = arrivalText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // ========== Candidate Lines List ==========
            Text(
                text = stringResource(R.string.passing_lines_count, corridorState.candidateLines.size),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                corridorState.candidateLines.forEach { candidate ->
                    CandidateLineRow(candidate = candidate)
                }
            }
        }
    }
}

@Composable
private fun CandidateLineRow(candidate: CorridorCandidateLine) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (candidate.isEarliest)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
                    color = if (candidate.isEarliest) BluePrimary else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = candidate.line.lineName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (candidate.isEarliest) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = if (candidate.stopsAway < 90)
                        stringResource(R.string.stops_away, candidate.stopsAway)
                    else
                        stringResource(R.string.no_real_time_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val mins = candidate.arrivalMinutes
                val (badgeColor, textColor) = when {
                    mins <= 2 -> Pair(GreenSuccess.copy(alpha = 0.15f), GreenSuccess)
                    mins <= 7 -> Pair(OrangeWarning.copy(alpha = 0.15f), OrangeWarning)
                    else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = when {
                            mins == 0 -> stringResource(R.string.arriving_soon)
                            mins < 90 -> stringResource(R.string.about_minutes, mins)
                            else -> "--"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
