package com.saidi.busassistant.ui.addline

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saidi.busassistant.R
import com.saidi.busassistant.data.local.entity.BusLineEntity
import com.saidi.busassistant.data.remote.dto.LineSearchResult
import com.saidi.busassistant.data.remote.dto.StationResult
import com.saidi.busassistant.data.repository.BusRepository
import com.saidi.busassistant.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

/**
 * 添加线路页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLineScreen(
    onBack: () -> Unit,
    viewModel: AddLineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_line_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                .padding(16.dp)
        ) {
            when (uiState.step) {
                AddStep.SEARCH -> SearchStep(
                    keyword = uiState.searchKeyword,
                    results = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onKeywordChange = viewModel::onKeywordChange,
                    onSearch = viewModel::search,
                    onLineSelected = viewModel::selectLine
                )
                AddStep.SELECT_STATION -> StationSelectStep(
                    line = uiState.selectedLine!!,
                    onStationSelected = viewModel::selectBoardingStation,
                    onBack = viewModel::backToSearch
                )
                AddStep.CONFIRM -> ConfirmStep(
                    line = uiState.lineToAdd!!,
                    onConfirm = {
                        viewModel.confirmAdd()
                        onBack()
                    },
                    onBack = viewModel::backToStation
                )
            }
        }
    }

    // 错误提示
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            // 可以在这里显示 Snackbar
        }
    }
}

/**
 * 搜索步骤
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SearchStep(
    keyword: String,
    results: List<LineSearchResult>,
    isSearching: Boolean,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLineSelected: (LineSearchResult) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        // 搜索框
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = GrayText
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearch()
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = GrayLight
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 搜索按钮
        Button(
            onClick = {
                keyboardController?.hide()
                onSearch()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = keyword.isNotBlank() && !isSearching,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BluePrimary
            )
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.search))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索结果
        if (results.isNotEmpty()) {
            Text(
                text = stringResource(R.string.search_results, results.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { line ->
                    LineSearchResultItem(
                        line = line,
                        onClick = { onLineSelected(line) }
                    )
                }
            }
        } else if (!isSearching && keyword.isNotEmpty()) {
            // 无结果
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
            }
        }
    }
}

/**
 * 搜索结果项
 */
@Composable
private fun LineSearchResultItem(
    line: LineSearchResult,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(BluePrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.lineName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${line.direction} · ${line.startStation} → ${line.endStation}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GrayText
                )
            }
        }
    }
}

/**
 * 选择上车站点步骤
 */
@Composable
private fun StationSelectStep(
    line: LineSearchResult,
    onStationSelected: (StationResult) -> Unit,
    onBack: () -> Unit
) {
    Column {
        // 线路信息摘要
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BluePrimary.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = BluePrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = line.lineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${line.startStation} → ${line.endStation}",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.select_boarding_station),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(line.stations ?: emptyList()) { station ->
                StationItem(
                    station = station,
                    onClick = { onStationSelected(station) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.search_again))
        }
    }
}

/**
 * 站点项
 */
@Composable
private fun StationItem(
    station: StationResult,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 站点序号
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BluePrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${station.index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = BluePrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * 确认步骤
 */
@Composable
private fun ConfirmStep(
    line: BusLineEntity,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.confirm_add),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 信息卡片
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoRow(label = stringResource(R.string.line), value = line.lineName)
                    InfoRow(label = stringResource(R.string.direction), value = line.direction)
                    InfoRow(label = stringResource(R.string.start), value = line.startStation)
                    InfoRow(label = stringResource(R.string.end), value = line.endStation)
                    InfoRow(
                        label = stringResource(R.string.boarding_stop),
                        value = line.userBoardingStation,
                        isHighlight = true
                    )
                }
            }
        }

        // 底部按钮
        Column {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                )
            ) {
                Text(stringResource(R.string.confirm_add))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.go_back))
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText
        )
        Text(
            text = value,
            style = if (isHighlight)
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            else
                MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ========== ViewModel ==========

data class AddLineUiState(
    val step: AddStep = AddStep.SEARCH,
    val searchKeyword: String = "",
    val searchResults: List<LineSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedLine: LineSearchResult? = null,
    val lineToAdd: BusLineEntity? = null,
    val error: String? = null
)

enum class AddStep {
    SEARCH, SELECT_STATION, CONFIRM
}

@HiltViewModel
class AddLineViewModel @Inject constructor(
    private val busRepository: BusRepository
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(AddLineUiState())
    val uiState: StateFlow<AddLineUiState> = _uiState

    fun onKeywordChange(keyword: String) {
        _uiState.update { it.copy(searchKeyword = keyword) }
    }

    fun search() {
        val keyword = _uiState.value.searchKeyword.trim()
        if (keyword.isEmpty()) return

        _uiState.update { it.copy(isSearching = true, error = null) }

        viewModelScope.launch {
            try {
                val result = busRepository.searchLines(keyword)
                result.onSuccess { lines ->
                    _uiState.update {
                        it.copy(
                            searchResults = lines,
                            isSearching = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isSearching = false
                    )
                }
            }
        }
    }

    fun selectLine(line: LineSearchResult) {
        _uiState.update {
            it.copy(
                selectedLine = line,
                step = AddStep.SELECT_STATION
            )
        }
    }

    fun selectBoardingStation(station: StationResult) {
        val selectedLine = _uiState.value.selectedLine ?: return

        val entity = BusLineEntity(
            lineNumber = selectedLine.lineId,
            lineName = selectedLine.lineName,
            direction = selectedLine.direction,
            startStation = selectedLine.startStation,
            endStation = selectedLine.endStation,
            userBoardingStation = station.name,
            userAlightingStation = selectedLine.endStation,
            boardingStationIndex = station.index
        )

        _uiState.update {
            it.copy(
                lineToAdd = entity,
                step = AddStep.CONFIRM
            )
        }
    }

    fun confirmAdd() {
        val entity = _uiState.value.lineToAdd ?: return

        viewModelScope.launch {
            busRepository.addLine(entity)
        }
    }

    fun backToSearch() {
        _uiState.update {
            it.copy(
                step = AddStep.SEARCH,
                selectedLine = null
            )
        }
    }

    fun backToStation() {
        _uiState.update {
            it.copy(
                step = AddStep.SELECT_STATION,
                lineToAdd = null
            )
        }
    }
}
