# BusAssistant — Smart Commute Assistant (Android MVP)

> Open the app and instantly see where your frequently used buses are. No map, zero interaction, fully local.

## Core Features

| Feature | Description |
|---|---|
| **Zero-interaction dashboard** | Open the app and within 3 seconds see real-time vehicle positions and arrival times for your favorite lines |
| **No map SDK** | Pure progress-bar visualization, APK < 20MB, cold start < 2 seconds |
| **Fully local storage** | All data stays on the device; nothing is uploaded |
| **Habit learning** | Automatically records viewing behavior and pins high-frequency lines to the top (P1) |
| **User labels** | Tag lines (Work / Home / School, etc.) for stronger personalization |

## Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Hilt** dependency injection
- **Room** local database
- **Retrofit** network requests (reserved)
- **Coroutines + Flow** async programming
- **Mock data** built-in simulated bus data for development

## Project Structure

```
com.saidi.busassistant/
├── MainActivity.kt              # Entry point + Navigation
├── BusAssistantApp.kt           # Application (Hilt)
├── data/
│   ├── local/                   # Room database
│   │   ├── entity/
│   │   │   ├── BusLineEntity.kt         # Favorite lines
│   │   │   └── BehaviorLogEntity.kt     # Behavior logs
│   │   ├── AppDatabase.kt
│   │   ├── BusLineDao.kt
│   │   └── BehaviorLogDao.kt
│   ├── remote/                  # Retrofit API
│   │   ├── BeijingBusApi.kt
│   │   └── dto/
│   │       └── BusRealTimeDto.kt
│   └── repository/
│       └── BusRepository.kt     # Data integration + Mock
├── di/
│   └── AppModule.kt             # Hilt module
├── ui/
│   ├── home/
│   │   └── HomeScreen.kt        # Home real-time dashboard
│   ├── addline/
│   │   └── AddLineScreen.kt     # Add-line flow
│   ├── settings/
│   │   └── SettingsScreen.kt    # Settings screen
│   ├── components/
│   │   ├── BusLineCard.kt       # Line card
│   │   └── ProgressIndicator.kt # Progress bar component
│   ├── viewmodel/
│   │   └── HomeViewModel.kt     # Home logic
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── util/
    └── BusLabel.kt              # Label utilities (reserved)
```

## Development Status

### Current: MVP (Mock Data)

The current version uses **mock data** to simulate Beijing bus lines, so the app can run and demonstrate the interaction flow without a real API.

### Switching to a Real API

Modify the `getRealTimeData` method in `BusRepository.kt`:

```kotlin
// Uncomment the real API call and remove mock data:
val response = busApi.getRealTimeData(lineId, direction)
if (response.isSuccessful && response.body()?.status == 200) {
    val data = response.body()?.data
    if (data != null) {
        cache[cacheKey] = CacheEntry(data)
        lastRequestTime[cacheKey] = System.currentTimeMillis()
        return@withContext Result.success(data)
    }
}
```

Also update `BeijingBusApi.BASE_URL` to the actual Beijing bus API endpoint.

## Build & Run

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

1. Open the project in Android Studio.
2. Sync Gradle.
3. Connect a device or start an emulator (API 26+).
4. Click Run.

### Command Line

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Feature Checklist

### P0 — MVP Core (completed)
- [x] Manually add bus lines
- [x] Real-time dashboard (home)
- [x] Real-time data gateway (with mock)
- [x] Local data storage (Room)

### P1 — Intelligence Layer (framework ready, pending integration)
- [x] User line labels
- [x] Automatic behavior logging
- [ ] Geofence-based zone detection (requires location permission)
- [x] Smart sorting algorithm
- [x] Settings screen

### P2 — Experience Improvements (future iterations)
- [ ] Lock screen / home screen widgets
- [ ] Arrival reminder notifications
- [ ] History statistics screen

## Privacy

- **All data is stored locally on the device** using Room.
- **No data is uploaded to any server.**
- Location is used only to determine commute direction and is not collected continuously in the background.
- Users can clear all learning data or disable habit recording at any time in Settings.

## License

MIT License — free to use and modify.
