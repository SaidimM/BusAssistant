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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GreenSuccess
import com.saidi.busassistant.ui.theme.OrangeWarning
import com.saidi.busassistant.ui.viewmodel.CommuteCorridorUiState
import com.saidi.busassistant.ui.viewmodel.CorridorCandidateLine

/**
 * 智能通勤走廊看板卡片
 * 聚合同一通勤区段（如：康家沟 ➔ 四惠东）的所有候选公交线路，极速同屏对比
 */
@Composable
fun CommuteCorridorCard(
    corridorState: CommuteCorridorUiState,
    onToggleDirection: () -> Unit,
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
            // ========== 顶部标题栏 ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (corridorState.inferredDirection == "WORK")
                                BluePrimary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = if (corridorState.inferredDirection == "WORK") "🌅 上班通勤" else "🌙 回家通勤",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (corridorState.inferredDirection == "WORK")
                                    BluePrimary
                                else
                                    MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (corridorState.isAutoInferred) {
                            Text(
                                text = "智能推测",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${corridor.originStation} ➔ ${corridor.destinationStation}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // 切换方向按钮
                FilledTonalIconButton(
                    onClick = onToggleDirection,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "切换通勤方向",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========== 最快到站推荐 Banner ==========
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
                                    text = "首选: ${fastestLine}路",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (earliestMins <= 2)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        BluePrimary
                                )
                                Text(
                                    text = if (earliestMins <= 2) "即将进站，快出门！" else "离你最近，优先乘坐",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 倒计时突出显示
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (earliestMins == 0) "已到站" else "${earliestMins}分钟",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = if (earliestMins <= 2) GreenSuccess else BluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // ========== 步行及工位预计到达时间 ==========
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

            // ========== 候选线路实时对比列表 ==========
            Text(
                text = "候选路线 (${corridorState.candidateLines.size}条可选)",
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
            // 左侧：线路名称 + 站距
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
                    text = if (candidate.stopsAway < 90) "还有 ${candidate.stopsAway} 站" else "暂无位置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 右侧：预计时间
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val mins = candidate.arrivalMinutes
                val (badgeColor, textColor) = when {
                    mins <= 2 -> Pair(GreenSuccess.copy(alpha = 0.15f), GreenSuccess)
                    mins <= 7 -> Pair(OrangeWarning.copy(alpha = 0.15f), OrangeWarning)
                    mins < 90 -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline)
                    else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = when {
                            mins == 0 -> "即将到站"
                            mins < 90 -> "~${mins} 分钟"
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
