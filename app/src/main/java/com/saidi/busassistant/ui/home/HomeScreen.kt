package com.saidi.busassistant.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Psychology
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
import com.saidi.busassistant.ui.components.NearbyStationBoard
import com.saidi.busassistant.ui.theme.BluePrimary
import com.saidi.busassistant.ui.theme.GrayText
import com.saidi.busassistant.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - Real-time transit departure dashboard and nearby station radar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onAddLineClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHabitInsightsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val corridorState by viewModel.corridorState.collectAsState()
    val nearbyStationState by viewModel.nearbyStationState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val realTimeDataMap by viewModel.realTimeDataMap.collectAsState()

    var selectedLineForLabel by remember { mutableStateOf<BusLineEntity?>(null) }

    // Request runtime location permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.refreshNearbyStations()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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
                    IconButton(onClick = onHabitInsightsClick) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = stringResource(R.string.habit_insights_title),
                            tint = BluePrimary
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            tint = GrayText
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========== 1. Nearby Station Radar Card (Zero-Interaction) ==========
                if (nearbyStationState.activeStation != null) {
                    item(key = "nearby_station_radar") {
                        NearbyStationBoard(
                            state = nearbyStationState,
                            onToggleDirection = { viewModel.toggleNearbyDirection() },
                            onSaveFavoriteLine = { lineNumber ->
                                viewModel.saveNearbyLineAsFavorite(lineNumber)
                            }
                        )
                    }
                }

                // ========== 2. Commute Corridor Card ==========
                if (!corridorState.isEmpty && corridorState.corridor != null) {
                    item(key = "commute_corridor") {
                        CommuteCorridorCard(
                            corridorState = corridorState,
                            onSwitchCorridor = { viewModel.switchToNextCorridor() }
                        )
                    }
                }

                // ========== 3. Favorite Bus Lines List ==========
                if (uiState.lines.isNotEmpty()) {
                    item(key = "section_header_favorite_lines") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.lines_added_count, uiState.lines.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                } else if (nearbyStationState.activeStation == null && corridorState.isEmpty) {
                    // Empty state
                    item {
                        EmptyState(onAddLineClick = onAddLineClick)
                    }
                }

                // Bottom spacer for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
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

    // Label selection dialog
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
 * Empty state screen.
 */
@Composable
private fun EmptyState(
    onAddLineClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
 * Formats current timestamp for header display.
 */
private fun getCurrentTimeText(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
