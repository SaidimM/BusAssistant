# BusAssistant 🚌⚡

> **A "Zero-Click / 1-Second" Habitual Transit Dashboard for Daily Commuters.**  
> Know which bus arrives first at your nearest stop—instantly, without maps, typing, or waiting.

---

## 🎯 The Motivation & Problem

Every day, millions of commuters travel between the same few origins and destinations (e.g., **Home ➔ Work**, **Work ➔ Home**, or **School / Station**). 

### The Commuter's Daily Dilemma
Consider a typical morning routine:
* You leave your apartment heading to work.
* You walk to the nearest bus station.
* There are **multiple bus lines** (e.g., 3 different lines, like Line 33 and others) that all go to your transfer station or destination, followed by a 10-minute walk to your workplace.
* Your only urgent question is: **"Which of these candidate buses will arrive at my stop first right now so I know whether to run or take my time?"**

### Why Mainstream Map Apps Fail Commuters
| Pain Point | Mainstream Map Apps (Amap, Baidu, Google Maps) | BusAssistant |
|---|---|---|
| **Cold Start Latency** | 5–10 seconds (loads heavy 3D maps, ads, discovery feeds) | **< 500 ms** (lightweight native Jetpack Compose UI) |
| **Interaction Steps** | 4–6 taps (Open app ➔ Search ➔ Enter destination ➔ Pick transit ➔ Check line) | **0 taps** (Automatic context & route prediction) |
| **Multi-Line Comparison** | Shows one route recommendation at a time or buries alternatives | **Simultaneous countdown** across all interchangeable lines |
| **Data Privacy** | Tracks and uploads commute patterns to cloud servers | **100% on-device**; local learning via Room SQLite |
| **App Size** | 150 MB – 300+ MB | **< 20 MB** (No bloated map SDKs) |

---

## 💡 The Solution: Habit-Driven Transit Estimation

Most commuters only travel between a handful of fixed locations. **BusAssistant** uses lightweight, on-device context intelligence to eliminate manual interaction:

```
+-------------------------------------------------------------------------+
|  8:15 AM (Weekday) • Location: Near Apartment                           |
|  👉 Inferred Commute: [Apartment] ➔ [Central Bus Station]               |
+-------------------------------------------------------------------------+
|  🚌 Line 33   | 🟢 Arriving in 2 min (1 stop away)   ⚡ RUN!           |
|  🚌 Line 12   | 🟡 Arriving in 6 min (3 stops away)                     |
|  🚌 Line 84   | ⚪ Arriving in 14 min (7 stops away)                    |
+-------------------------------------------------------------------------+
|  🚶 Walking to workplace: ~10 mins after arrival at Central Station     |
+-------------------------------------------------------------------------+
```

### Core Innovations

1. **Context-Aware Local Prediction Engine**
   - Automatically estimates where you are going based on:
     - **Time of Day & Day of Week** (e.g., Monday 08:15 vs. Friday 18:30)
     - **Geographic Context / Geofencing** (detects proximity to Home, Work, or Station)
     - **Historical Travel Frequency** (learned Markov or frequency matrix)
   - Predicts your next destination and immediately renders the appropriate departure board upon launch.

2. **Interchangeable Route Corridors (Multi-Line Aggregation)**
   - When multiple bus lines cover the same segment of your commute, BusAssistant groups them into a single **Route Corridor**.
   - Instead of checking Line A, then Line B, then Line C, you see all candidate lines ranked by earliest arrival time side-by-side.

3. **Sub-Second "Glance-and-Go" UI**
   - No map view required. Clean, high-contrast Material 3 progress bars and live ETA countdown cards.
   - Built to expand into **Lock Screen Live Activities**, **Android Ongoing Notifications**, and **Home Screen Widgets** for true zero-tap glanceability before you even leave your door.

4. **Zero-Cloud Privacy**
   - All habit logs, visited locations, and commute routines are stored exclusively on your device in SQLite. No tracking, no user profiles, and no external analytics.

---

## 📱 Architecture & Tech Stack

```
BusAssistant/
├── UI Layer (Jetpack Compose + Material 3)
│   ├── HomeScreen             # Zero-tap multi-line arrival board
│   ├── AddLineScreen          # Setup route corridors & favorite lines
│   ├── SettingsScreen         # Habit management & privacy controls
│   └── Components             # ProgressIndicator, BusLineCard, ArrivalBadges
│
├── Domain & ViewModel
│   └── HomeViewModel          # Context evaluation, smart sorting & auto-refresh
│
├── Data Layer (Repository Pattern)
│   ├── Local (Room Database)
│   │   ├── BusLineEntity      # Configured lines, corridor tags & stops
│   │   ├── BehaviorLogEntity  # On-device commute time & location frequency logs
│   │   └── DAOs               # Fast local querying for smart ordering
│   └── Remote (Retrofit)
│       ├── Transit API Gate   # Real-time vehicle positions (GTFS-RT / City API)
│       └── Mock Transit Engine# Built-in simulator for rapid local testing
```

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Dependency Injection:** Hilt
- **Local Persistence:** Room (SQLite)
- **Networking:** Retrofit + Kotlinx Serialization / Gson
- **Concurrency:** Coroutines + Flow (Reactive UI updates)

---

## 🗺️ Roadmap

### Phase 1: MVP Core (Completed)
- [x] Lightweight, map-free Jetpack Compose dashboard (<500ms launch)
- [x] Multi-line real-time arrival cards with progress indicators
- [x] Local Room database for favorite lines and tags (Work, Home, School)
- [x] Simulated real-time bus telemetry engine (Algorithmic simulation)
- [x] Context logging foundation (Day of week, hour, frequency sorting)

### Phase 2: Route Corridors & Smart Estimation (Completed)
- [x] **Route Corridor Auto-Clustering:** Automatically groups multiple bus lines serving the same origin-destination corridor into a single glanceable card.
- [x] **Geofence & Location-Aware Context Engine:** Automatic location zone logging & geofence trigger support via LocationContextManager.
- [x] **Smart Arrival Recommendation:** Highlights fastest approaching line with instant action badges ("首选 / 即将进站").

### Phase 3: Zero-Tap Ecosystem & Ambient Intelligence (Completed)
- [x] **Zero-Interaction Nearest Station Radar:** On launch, auto-detects nearest physical bus stop and displays live arrivals for all passing lines without any setup.
- [x] **Autonomous Habit Learning & Memory Screen:** 100% on-device clustering of daily commute routines, trip statistics, and time-saving metrics in a dedicated dashboard (`HabitInsightsScreen`).
- [x] **Android Home Screen AppWidget:** Native home screen widget with live multi-line countdowns, route header, and one-tap refresh.
- [ ] **Lock Screen Persistent Notification:** Auto-activates 15 minutes before typical commute times.
- [ ] **WearOS / Smartwatch Companion:** Check bus ETAs on your wrist while walking to the station.
- [ ] **GTFS-RT & Open Transit Integration:** Support standard city transit real-time feeds.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** Hedgehog (2023.1.1) or newer
- **JDK:** 17
- **Target Android SDK:** 34 (Android 14)
- **Minimum Android SDK:** 26 (Android 8.0)

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/SaidimM/BusAssistant.git
   cd BusAssistant
   ```

2. Open the project in Android Studio and let Gradle sync.

3. Build and install the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Privacy & Local-First Philosophy

* **On-Device Storage:** All commute habits, times, and frequently visited lines reside strictly on your device inside Room SQLite.
* **No Telemetry / No Tracking:** Zero analytics SDKs. No background location transmission.
* **User Control:** Clear all learned commute habits with a single tap in `Settings -> Clear Learning Data`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free to use and modify.
