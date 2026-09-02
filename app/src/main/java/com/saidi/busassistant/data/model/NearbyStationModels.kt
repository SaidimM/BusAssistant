package com.saidi.busassistant.data.model

/**
 * Physical bus station information (including geographic coordinates and passing lines).
 */
data class NearbyStation(
    val id: String,
    val stationName: String,
    val latitude: Double,
    val longitude: Double,
    val directionText: String, // e.g., "Northbound (Towards Tech Park)"
    val oppositeStationId: String? = null,
    val oppositeStationName: String? = null,
    val passingLineNumbers: List<String> = emptyList()
)

/**
 * Single line departure arrival status at a nearby station.
 */
data class NearbyLineArrival(
    val lineNumber: String,
    val destination: String,
    val arrivalMinutes: Int,
    val stationsAway: Int,
    val isArriving: Boolean,
    val isFastest: Boolean = false,
    val crowdLevel: String = "Moderate"
)

/**
 * Nearby station board UI state.
 */
data class NearbyStationUiState(
    val activeStation: NearbyStation? = null,
    val distanceMeters: Int = 0,
    val walkingMinutes: Int = 0,
    val isOppositeDirection: Boolean = false,
    val arrivals: List<NearbyLineArrival> = emptyList(),
    val fastestArrival: NearbyLineArrival? = null,
    val isLoading: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val detectedHabitSummary: String? = null
)

/**
 * Learned commute routine inferred from on-device logs.
 */
data class LearnedCommuteRoutine(
    val id: String,
    val routineName: String, // e.g., "Morning Commute (Home ➔ Work)"
    val originStation: String,
    val destinationStation: String,
    val preferredLineNumbers: List<String>,
    val typicalTimeWindow: String, // e.g., "Weekdays 08:10 – 08:35"
    val tripCount: Int,
    val confidencePercentage: Int, // Confidence score 60%~99%
    val timeSlotType: TimeSlotType = TimeSlotType.MORNING_COMMUTE
)

enum class TimeSlotType {
    MORNING_COMMUTE,
    EVENING_COMMUTE,
    WEEKEND_OUTING,
    OFF_PEAK
}

/**
 * Commute memory and statistical summary.
 */
data class CommuteStatsSummary(
    val totalTripsTracked: Int = 0,
    val estimatedMinutesSaved: Int = 0,
    val mostFrequentedStation: String = "--",
    val topBusLine: String = "--",
    val morningCommutePeak: String = "08:15",
    val eveningCommutePeak: String = "18:30"
)

/**
 * Habit insights screen UI state.
 */
data class HabitInsightsUiState(
    val learnedRoutines: List<LearnedCommuteRoutine> = emptyList(),
    val statsSummary: CommuteStatsSummary = CommuteStatsSummary(),
    val isLoading: Boolean = false
)
