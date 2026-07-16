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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.ui.theme.*
import com.saidi.busassistant.ui.viewmodel.ClosestBusInfo
import com.saidi.busassistant.ui.viewmodel.RealTimeDataDisplay

/**
 * 公交线路卡片
 * 展示单条线路的实时状态
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
            // 顶部：线路信息 + 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 线路号和图标
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

                // 右侧：标签 + 删除
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 用户标签
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
                                contentDescription = "添加标签",
                                tint = GrayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 删除按钮
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = GrayText.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 站点信息
            Text(
                text = "上车站: ${line.userBoardingStation}",
                style = MaterialTheme.typography.labelMedium,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 实时进度条
            BusProgressBar(
                closestBus = realTimeData?.closestBus,
                modifier = Modifier.fillMaxWidth()
            )

            // 底部：详细信息
            realTimeData?.closestBus?.let { bus ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val detailText = buildString {
                        append("还有 ${bus.stationsAway} 站")
                        if (bus.minutesAway > 0) {
                            append(" · 约 ${bus.minutesAway} 分钟")
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
                                text = "即将到站",
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

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除线路") },
            text = { Text("确定要删除 ${line.lineName} 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("删除", color = RedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 用户标签 Chip
 */
@Composable
fun UserLabelChip(
    label: String,
    onClick: () -> Unit
) {
    val (icon, color) = when (label) {
        "上班" -> Icons.Default.Work to BluePrimary
        "回家" -> Icons.Default.Home to TealAccent
        "上学" -> Icons.Default.School to OrangeWarning
        "购物" -> Icons.Default.ShoppingCart to Color(0xFF8B5CF6)
        "就医" -> Icons.Default.LocalHospital to RedAlert
        else -> Icons.Default.Star to GrayText
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
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * 标签选择对话框
 */
@Composable
fun LabelSelectionDialog(
    currentLabel: String?,
    onDismiss: () -> Unit,
    onLabelSelected: (String?) -> Unit
) {
    val labels = listOf(
        "上班" to Icons.Default.Work,
        "回家" to Icons.Default.Home,
        "上学" to Icons.Default.School,
        "购物" to Icons.Default.ShoppingCart,
        "就医" to Icons.Default.LocalHospital
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("标注这条线路") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEach { (label, icon) ->
                    val isSelected = currentLabel == label
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLabelSelected(label) },
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
                                contentDescription = label,
                                tint = if (isSelected) BluePrimary else GrayText
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 清除标签选项
                if (currentLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { onLabelSelected(null) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("清除标签", color = RedAlert)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
