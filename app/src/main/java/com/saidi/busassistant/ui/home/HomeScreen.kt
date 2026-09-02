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
import androidx.compose.material.icons.filled.DirectionsBus
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saidi.busassistant.R
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.ui.components.BusLineCard
import com.saidi.busassistant.ui.components.CommuteCorridorCard
import com.saidi.busassistant.ui.components.LabelSelectionDialog
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GrayText
import com.saidi.busassistant.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页 —— 实时公交看板（支持通勤走廊多线极速聚合）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onAddLineClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val corridorState by viewModel.corridorState.collectAsState()
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
                            text = stringResource(R.string.home_title),
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
                            contentDescription = stringResource(R.string.refresh),
                            tint = BluePrimary
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
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
                    contentDescription = stringResource(R.string.add_line),
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
            if (uiState.lines.isEmpty() && corridorState.isEmpty) {
                // 空状态
                EmptyState(onAddLineClick = onAddLineClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ========== 顶部：智能通勤走廊看板 ==========
                    if (!corridorState.isEmpty && corridorState.corridor != null) {
                        item(key = "commute_corridor") {
                            CommuteCorridorCard(
                                corridorState = corridorState,
                                onToggleDirection = { viewModel.toggleCorridorDirection() }
                            )
                        }

                        item(key = "section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "全部独立线路 (${uiState.lines.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "30秒自动刷新",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrayText
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.lines_added_count, uiState.lines.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = GrayText,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // ========== 单条线路列表 ==========
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = GrayText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.empty_state_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddLineClick,
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.add_first_line))
        }
    }
}

/**
 * 获取当前时间展示文本
 */
private fun getCurrentTimeText(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
