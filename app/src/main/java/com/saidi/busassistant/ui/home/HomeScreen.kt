package com.saidi.busassistant.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.ui.components.BusLineCard
import com.saidi.busassistant.ui.components.LabelSelectionDialog
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GrayText
import com.saidi.busassistant.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页 —— 实时公交看板
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onAddLineClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val realTimeDataMap by viewModel.realTimeDataMap.collectAsState()

    var selectedLineForLabel by remember { mutableStateOf<BusLineEntity?>(null) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "我的公交",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getCurrentTimeText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = BluePrimary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = GrayText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLineClick,
                shape = CircleShape,
                containerColor = BluePrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加线路",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            if (uiState.lines.isEmpty()) {
                // 空状态
                EmptyState(onAddLineClick = onAddLineClick)
            } else {
                // 线路列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 智能提示（如果有学习数据）
                    item {
                        Text(
                            text = "已添加 ${uiState.lines.size} 条线路",
                            style = MaterialTheme.typography.labelMedium,
                            color = GrayText,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(
                        items = uiState.lines,
                        key = { it.id }
                    ) { line ->
                        val realTimeData = realTimeDataMap[line.id]

                        BusLineCard(
                            line = line,
                            realTimeData = realTimeData,
                            onDelete = { viewModel.deleteLine(line) },
                            onLabelClick = { selectedLineForLabel = line },
                            onCardClick = { viewModel.recordLineViewed(line) }
                        )
                    }

                    // 底部留白（避免 FAB 遮挡）
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = BluePrimary
            )
        }
    }

    // 标签选择对话框
    selectedLineForLabel?.let { line ->
        LabelSelectionDialog(
            currentLabel = line.userLabel,
            onDismiss = { selectedLineForLabel = null },
            onLabelSelected = { label ->
                viewModel.updateLineLabel(line.id, label)
                selectedLineForLabel = null
            }
        )
    }
}

/**
 * 空状态页面
 */
@Composable
private fun EmptyState(
    onAddLineClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 公交车图标占位
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    BluePrimary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = BluePrimary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "还没有添加线路",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "添加你常坐的公交线路\n打开 App 即可看到实时到站信息",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddLineClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary
            )
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加第一条线路")
        }
    }
}

/**
 * 获取当前时间文本
 */
private fun getCurrentTimeText(): String {
    val sdf = SimpleDateFormat("MM月dd日 EEEE HH:mm", Locale.CHINA)
    return sdf.format(Date())
}
