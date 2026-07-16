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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saidi.busassistant.ui.theme.*
import com.saidi.busassistant.ui.viewmodel.HomeViewModel

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    val logCount by remember { mutableIntStateOf(0) }

    // 加载日志数量
    LaunchedEffect(Unit) {
        // viewModel.getBehaviorLogCount() 可以在这里调用
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
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
            // 数据与隐私
            SettingsSection(title = "数据与隐私") {
                // 位置权限
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = "使用位置信息",
                    subtitle = "识别通勤方向，自动展示对应线路",
                    checked = true, // TODO: 从 DataStore 读取
                    onCheckedChange = { /* TODO */ }
                )

                // 习惯记录
                SettingsSwitchItem(
                    icon = Icons.Default.BarChart,
                    title = "记录使用习惯",
                    subtitle = "用于智能推荐常坐线路",
                    checked = true, // TODO: 从 DataStore 读取
                    onCheckedChange = { /* TODO */ }
                )
            }

            // 数据管理
            SettingsSection(title = "数据管理") {
                SettingsClickableItem(
                    icon = Icons.Default.Delete,
                    title = "清除所有学习数据",
                    subtitle = "删除记录的使用习惯和偏好设置",
                    iconTint = RedAlert,
                    onClick = { showClearConfirm = true }
                )
            }

            // 关于
            SettingsSection(title = "关于") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "数据说明",
                    subtitle = "所有数据仅存储在您的手机本地，不会上传任何服务器。\n\n实时公交数据来自北京公交官方接口。"
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
                        text = "版本",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1.0.0-mvp",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部提示
            Text(
                text = "BusAssistant MVP\n所有数据本地存储，保护您的隐私",
                style = MaterialTheme.typography.labelSmall,
                color = GrayText.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // 清除确认对话框
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清除学习数据") },
            text = {
                Text("确定要删除所有使用习惯数据吗？\n这将重置智能推荐功能。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLearningData()
                        showClearConfirm = false
                    }
                ) {
                    Text("清除", color = RedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 设置分区
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
 * 带 Switch 的设置项
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
 * 可点击的设置项
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
 * 纯信息展示项
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
