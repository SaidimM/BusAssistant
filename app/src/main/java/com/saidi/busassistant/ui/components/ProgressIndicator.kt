package com.saidi.busassistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import com.saidi.busassistant.R
import com.saidi.busassistant.ui.theme.*
import com.saidi.busassistant.ui.viewmodel.ClosestBusInfo

/**
 * Bus Progress Bar Component.
 * Pure Canvas-based arrival line progress without map dependencies.
 */
@Composable
fun BusProgressBar(
    closestBus: ClosestBusInfo?,
    modifier: Modifier = Modifier
) {
    if (closestBus == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.no_real_time_data),
                style = MaterialTheme.typography.labelMedium,
                color = GrayText
            )
        }
        return
    }

    val busColor = when {
        closestBus.isArriving -> GreenSuccess
        closestBus.minutesAway <= 5 -> OnTimeGreen
        closestBus.minutesAway <= 10 -> OrangeWarning
        else -> BluePrimary
    }

    val progress = closestBus.progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val trackColor = GrayLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barHeight = 8.dp.toPx()
            val barY = canvasHeight / 2 - barHeight / 2
            val startX = 0f
            val endX = canvasWidth

            // 1. Draw background track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(startX, barY),
                size = Size(endX - startX, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2, barHeight / 2)
            )

            // 2. Draw active transit progress
            if (animatedProgress > 0) {
                drawRoundRect(
                    color = busColor.copy(alpha = 0.3f),
                    topLeft = Offset(startX, barY),
                    size = Size((endX - startX) * animatedProgress, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2, barHeight / 2)
                )
            }

            // 3. Draw station nodes (Origin, User Boarding Stop, Terminal)
            val dotRadius = 5.dp.toPx()
            val dotY = canvasHeight / 2

            // Origin
            drawCircle(
                color = GrayText.copy(alpha = 0.5f),
                radius = dotRadius,
                center = Offset(startX + dotRadius, dotY)
            )

            // User boarding stop
            val userStationX = startX + (endX - startX) * 0.7f
            drawCircle(
                color = if (closestBus.isArriving) busColor else BluePrimary,
                radius = dotRadius * 1.3f,
                center = Offset(userStationX, dotY)
            )

            // Terminal
            drawCircle(
                color = GrayText.copy(alpha = 0.5f),
                radius = dotRadius,
                center = Offset(endX - dotRadius, dotY)
            )

            // 4. Draw vehicle position indicator
            val busX = startX + (endX - startX) * animatedProgress
            val busY = canvasHeight / 2

            drawCircle(
                color = busColor,
                radius = 10.dp.toPx(),
                center = Offset(busX, busY)
            )
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(busX, busY)
            )
            drawCircle(
                color = busColor,
                radius = 4.dp.toPx(),
                center = Offset(busX, busY)
            )
        }

        // Time countdown label
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val timeText = when {
                closestBus.isArriving -> stringResource(R.string.arriving_soon)
                closestBus.minutesAway <= 1 -> stringResource(R.string.within_one_minute)
                else -> stringResource(R.string.minutes, closestBus.minutesAway)
            }
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge,
                color = busColor
            )
        }
    }
}

/**
 * Simplified dot-based station progress indicator.
 */
@Composable
fun DotProgressIndicator(
    totalStations: Int,
    currentStation: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val safeTotal = totalStations.coerceIn(3, 15)
        val safeCurrent = currentStation.coerceIn(0, safeTotal)

        repeat(safeTotal) { index ->
            val color = when {
                index < safeCurrent -> GreenSuccess
                index == safeCurrent -> BluePrimary
                else -> GrayLight
            }
            Box(
                modifier = Modifier
                    .size(if (index == safeCurrent) 10.dp else 8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
