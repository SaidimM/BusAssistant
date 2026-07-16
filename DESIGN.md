# BusAssistant — Material 3 Design Document

> This document defines the visual and interaction design language for BusAssistant. All UI work must follow [Material Design 3 (Material You)](https://m3.material.io/) principles and use Jetpack Compose Material 3 components.

---

## 1. Design Philosophy

BusAssistant is a **zero-interaction commute dashboard**. The user opens the app and immediately sees the status of their favorite bus lines. Design decisions should prioritize:

1. **Clarity first** — the most important information (minutes until arrival) must be scannable within 1 second.
2. **Calm confidence** — the UI should feel reliable and uncluttered, not flashy.
3. **Personal, but private** — personalization (labels, smart sorting) is surfaced subtly; no data leaves the device.
4. **Material fidelity** — use real Material 3 components, tokens, and motion rather than custom approximations.

---

## 2. Core Material 3 Principles

### 2.1 Personal Color (Material You)

- **Primary**: Use a seed derived from the app identity (transit blue). On Android 12+ (API 31+), support `dynamicColorScheme` so users who want it can use their wallpaper-derived palette.
- **Secondary / Tertiary**: Reserved for accents and semantic highlights.
- Always reference colors through **color roles** (`primary`, `onPrimary`, `surfaceVariant`, `outline`, etc.) instead of hard-coded hex values in UI code.

### 2.2 Tonal Surfaces & Elevation

- Use `surface`, `surfaceVariant`, and `background` roles to create hierarchy.
- Prefer **tonal elevation** (surface color + `surfaceTint`) over shadow elevation for light-theme hierarchy.
- Cards use `surface` at `0dp` elevation with subtle `surfaceVariant` backgrounds for grouped items.

### 2.3 Typography Scale

- Use the Material 3 type scale tokens: `display`, `headline`, `title`, `body`, `label`.
- Never invent arbitrary font sizes; map every text element to one of the scale tokens.
- Chinese text should use a slightly larger line-height than Latin defaults for readability.

### 2.4 Shape System

- Use a consistent shape family:
  - **Cards / Dialogs**: `ShapeLarge` (16 dp)
  - **Buttons / TextFields**: `ShapeMedium` (12 dp)
  - **Chips / small surfaces**: `ShapeSmall` (8 dp)
  - **FAB**: fully rounded / circular

### 2.5 Motion

- Use Material motion defaults for enter/exit transitions between screens.
- Micro-interactions (progress updates, arrival state changes) use `animate*AsState` with `FastOutSlowInEasing`.
- Avoid jarring spring animations; keep motion subtle and informative.

---

## 3. Color System

### 3.1 App Identity Seed

The app identity is built around a **transit blue** seed:

```kotlin
val md_theme_seed = Color(0xFF2563EB)
```

### 3.2 Color Roles (Light Theme)

| Token | Role | Current / Proposed Value |
|---|---|---|
| `primary` | Main brand / active actions | `#2563EB` |
| `onPrimary` | Text/icons on primary | `#FFFFFF` |
| `primaryContainer` | Highlighted containers | `#D3E3FD` |
| `onPrimaryContainer` | Text on primary containers | `#0B3D91` |
| `secondary` | Transit/teal accent | `#14B8A6` |
| `tertiary` | Success / "arriving" | `#22C55E` |
| `error` | Destructive / delay alert | `#DC2626` |
| `background` | Screen background | `#F8FAFC` |
| `surface` | Cards, sheets, dialogs | `#FFFFFF` |
| `surfaceVariant` | Subtle grouped backgrounds | `#E2E8F0` |
| `outline` | Dividers, borders | `#94A3B8` |
| `onSurfaceVariant` | Secondary text on variants | `#475569` |

### 3.3 Color Roles (Dark Theme)

| Token | Value |
|---|---|
| `primary` | `#93C5FD` |
| `onPrimary` | `#0F172A` |
| `background` | `#0F172A` |
| `surface` | `#1E293B` |
| `surfaceVariant` | `#334155` |
| `outline` | `#64748B` |
| `onSurfaceVariant` | `#CBD5E1` |

### 3.4 Semantic Color Usage

- **Arriving now**: `tertiary` (green) container + on-tertiary text.
- **1–5 minutes**: `primary` (blue) — normal state.
- **5–10 minutes**: `secondary` (teal) or `outline` — still fine, less urgent.
- **Delayed / >10 min**: `error` container for warning.
- Do **not** introduce one-off hex colors like `OrangeWarning` in components; map urgency to the semantic palette or use `secondaryContainer` / `tertiaryContainer` roles.

---

## 4. Typography

### 4.1 Type Scale

| Token | Size | Line Height | Weight | Usage |
|---|---|---|---|---|
| `displaySmall` | 36 sp | 44 sp | Bold | Splash / empty-state hero |
| `headlineSmall` | 24 sp | 32 sp | SemiBold | Screen titles ("My Buses") |
| `titleLarge` | 22 sp | 28 sp | Medium | Card line names |
| `titleMedium` | 16 sp | 24 sp | Medium | Section headers |
| `bodyLarge` | 16 sp | 24 sp | Regular | Primary content |
| `bodyMedium` | 14 sp | 20 sp | Regular | Secondary content |
| `labelLarge` | 14 sp | 20 sp | Medium | Buttons, arrival times |
| `labelMedium` | 12 sp | 16 sp | Medium | Metadata, captions |
| `labelSmall` | 11 sp | 16 sp | Medium | Timestamp, badges |

### 4.2 Chinese Readability

- Minimum body text size: **14 sp**.
- Line height for Chinese body text should be ≥ 1.5× font size.
- Avoid ultra-thin weights (`FontWeight.Thin`) for Chinese characters.

---

## 5. Layout & Spacing

### 5.1 Baseline Grid

All spacing is based on multiples of **4 dp**:

- `4 dp` — tight icon/text gaps
- `8 dp` — inline spacing, chip padding
- `12 dp` — card internal compact gaps
- `16 dp` — default screen and card padding
- `24 dp` — section separators
- `32 dp` — large section breaks

### 5.2 Screen Padding

- Horizontal screen padding: **16 dp**.
- Card internal padding: **16 dp**.
- LazyColumn item spacing: **12 dp**.
- FAB bottom clearance: **80 dp** minimum spacer at list bottom.

### 5.3 Responsive Behavior

- Home dashboard is a single-column list on phones.
- On foldable / tablet widths ≥ 600 dp, consider a two-column grid with the same cards.
- Text should not scale wider than ~600 dp for readability; center content on very wide screens.

---

## 6. Components

### 6.1 App Bars

- Use `CenterAlignedTopAppBar` for settings and add-line screens where the title is the only action.
- Use `TopAppBar` for the home screen because it hosts action icons (refresh, settings).
- App bar container color = `background` for a seamless, edge-to-edge look.
- Use `TopAppBarDefaults.topAppBarColors(...)`; do not hard-code colors.

### 6.2 Cards (`Card`)

- Shape: `MaterialTheme.shapes.extraLarge` (16 dp).
- Elevation: default `0 dp` in light theme, rely on surface color contrast.
- In dark theme, use `CardDefaults.cardElevation(defaultElevation = 1.dp)` for subtle elevation.
- Content padding: 16 dp.

### 6.3 Floating Action Button (FAB)

- Primary FAB on Home screen: circular, `FABDefaults.shape`, container color `primary`.
- Icon: `Icons.Default.Add`, tint `onPrimary`.
- Use `extended` variant only if adding context text is needed; current design uses icon-only FAB.

### 6.4 Buttons

- Primary CTA: `Button`, filled, shape 12 dp, container `primary`.
- Secondary action: `OutlinedButton` with `contentColor = primary`.
- Text-only action: `TextButton` for dialogs and low-emphasis actions.

### 6.5 Text Fields

- Use `OutlinedTextField` for search.
- Shape: `MaterialTheme.shapes.medium` (12 dp).
- Colors: `TextFieldDefaults.outlinedTextFieldColors(...)` — migrate to `OutlinedTextFieldDefaults.colors(...)` for Material 3 stable API.

### 6.6 Chips

- User labels ("Work", "Home") use `AssistChip` or `InputChip`.
- Selected label container color = `secondaryContainer`, label color = `onSecondaryContainer`.
- Use leading icon from Material icon set mapped to label semantics.

### 6.7 Dialogs

- Use `AlertDialog` for confirmations (delete line, clear data).
- Destructive action uses `TextButton` with `contentColor = colorScheme.error`.
- Dialog shape defaults to `MaterialTheme.shapes.extraLarge`.

### 6.8 Progress Indicators

- Pull-to-refresh: use `PullToRefreshBox` / `PullRefreshIndicator` from Material 3 or the material-pullrefresh artifact.
- Bus progress bar is custom Canvas; it must still use color roles and 4 dp aligned dimensions.

---

## 7. Screen Specifications

### 7.1 Home Screen

**Purpose**: instant dashboard.

- **Top app bar title**: `headlineSmall` "My Buses" + `labelSmall` current date/time in `onSurfaceVariant`.
- **Actions** (trailing):
  - Refresh icon: `primary` tint when idle.
  - Settings icon: `onSurfaceVariant` tint.
- **Empty state**:
  - 80 dp circular container in `primaryContainer`.
  - Bus icon tinted `onPrimaryContainer`.
  - `titleLarge` heading + `bodyMedium` subtitle in `onSurfaceVariant`.
  - Primary CTA button: "Add your first line".
- **List state**:
  - Header label: `labelMedium`, `onSurfaceVariant`: "%d lines added".
  - Cards spaced 12 dp apart.
  - FAB bottom-right, 16 dp margin.

### 7.2 Bus Line Card

**Purpose**: communicate "where is my bus and when will it arrive" at a glance.

Structure (top to bottom):

1. **Header row**
   - 40 dp circular avatar in `primaryContainer` with bus icon (`onPrimaryContainer`).
   - Line name: `titleLarge`.
   - Direction: `labelMedium`, `onSurfaceVariant`.
   - Trailing: label chip + delete icon.
2. **Boarding station**
   - `labelMedium`: "Boarding stop: {name}", `onSurfaceVariant`.
3. **Progress bar**
   - Custom Canvas, 48 dp height.
   - Track color: `surfaceVariant`.
   - Traveled segment: `primaryContainer` / `tertiaryContainer` depending on urgency.
   - Bus marker: filled circle in the urgency color.
4. **Footer row**
   - Left: "%d stops away · ~ %d min", `labelMedium`.
   - Right: "Arriving soon" badge in `tertiaryContainer` / `onTertiaryContainer`.

### 7.3 Add Line Flow

Three steps managed inside a single screen with a `TopAppBar` back button.

1. **Search step**
   - `OutlinedTextField` with search icon.
   - Primary "Search" `Button`.
   - Results shown as `Surface` list items with bus icon avatar.
2. **Station selection step**
   - Summary card at top in `primaryContainer`.
   - Station list: each row shows index badge + station name.
   - Selected station advances to confirm step.
3. **Confirm step**
   - Card displaying line metadata with `InfoRow` style.
   - Primary "Confirm add" + secondary "Go back" buttons stacked at bottom.

### 7.4 Settings Screen

- Group settings into `Card` sections with `labelLarge` section titles in `onSurfaceVariant`.
- Each setting row: 24 dp leading icon, title + subtitle, trailing control (`Switch`, chevron, or value).
- Destructive action (clear data) uses `error` tinted icon and confirmation dialog.
- About section: plain info rows + version string.

---

## 8. Motion & Feedback

### 8.1 Screen Transitions

- Use Material 3 shared-axis or fade-through transitions between Home ↔ AddLine ↔ Settings.
- Recommended: `androidx.compose.animation.AnimatedContent` with `with(MaterialMotionSpec) { ... }` if using the Material Motion library, or simple `Crossfade` as a baseline.

### 8.2 Micro-interactions

- **Progress updates**: animate `progress` changes with `animateFloatAsState(targetValue = progress, animationSpec = tween(800, easing = FastOutSlowInEasing))`.
- **Arrival state change**: when `isArriving` becomes true, pulse the bus marker subtly (scale 1.0 → 1.1 → 1.0, 600 ms).
- **List reorder**: when smart sort changes card order, use `LazyColumn` keys + `animateItemPlacement()` (requires Compose Foundation 1.6+).
- **FAB / button press**: use default Material 3 ripple; do not disable ripple.

### 8.3 Loading & Error States

- **Pull-to-refresh**: native Material indicator at top center.
- **Search loading**: replace button text with `CircularProgressIndicator(size = 20.dp, strokeWidth = 2.dp)`.
- **Error**: show a `Snackbar` via `Scaffold(snackbarHost = { SnackbarHost(...) })` rather than inline error banners.

---

## 9. Accessibility

- Every icon button must have a non-null `contentDescription`.
- Dynamic text (arrival time) should have `semantics { contentDescription = ... }` that reads naturally with TalkBack, e.g. "Route 375, 3 stops away, about 5 minutes, arriving soon".
- Touch targets minimum 48 × 48 dp; the current 32 dp icon buttons need a 48 dp minimum size or adequate padding.
- Color is never the sole indicator of state; pair urgency colors with text ("Arriving soon", "~ 5 min").
- Support system font size scaling; avoid fixed heights that clip text.
- Respect `isSystemInDarkTheme()` and support dark theme fully.

---

## 10. Implementation Notes

### 10.1 Theme Migration Checklist

- [ ] Generate a complete Material 3 color scheme from the seed using `ColorScheme` builder or Material KTX utilities.
- [ ] Replace hard-coded hex colors in components (`BluePrimary`, `GrayText`, etc.) with `MaterialTheme.colorScheme` roles.
- [ ] Add missing roles: `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `tertiaryContainer`, `surfaceTint`, `outlineVariant`.
- [ ] Migrate legacy `material` (M2) imports to `material3` where possible; keep M2 only for `pullRefresh` until a Material 3 alternative is available.
- [ ] Define `shapes` in `Theme.kt`: `extraSmall = 4.dp`, `small = 8.dp`, `medium = 12.dp`, `large = 16.dp`, `extraLarge = 28.dp`.
- [ ] Ensure `Typography` covers all required Material 3 roles (`displaySmall`, `headlineSmall`, `titleLarge`, etc.).

### 10.2 Dynamic Color (Optional)

```kotlin
@Composable
fun busAssistantColorScheme(darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }
}
```

### 10.3 Edge-to-Edge

- Status bar color = `background`.
- `WindowCompat.getInsetsController(...).isAppearanceLightStatusBars = !darkTheme`.
- Ensure content respects system bars via `Scaffold` padding.

---

## 11. Do's and Don'ts

| Do ✅ | Don't ❌ |
|---|---|
| Use `MaterialTheme.colorScheme` roles everywhere | Hard-code hex colors in UI code |
| Prefer Material 3 components (`Card`, `Button`, `Switch`) | Rebuild components from `Box`/`Row` when a standard one exists |
| Keep cards at 0–1 dp elevation and rely on color | Use heavy shadows for hierarchy in light mode |
| Use 4 dp grid spacing | Invent arbitrary 3, 5, 7 dp values |
| Provide `contentDescription` for every icon | Leave accessibility labels blank |
| Animate state changes smoothly | Snap progress bars or lists instantly |
| Support dark theme via `darkColorScheme` | Maintain only a light theme |
| Use shape tokens consistently | Mix 8 dp, 12 dp, 16 dp, 20 dp arbitrarily |

---

## 12. Future Considerations

- **Widgets (P2)**: follow Material 3 Glance widget guidelines; use the same color tokens.
- **Notifications (P2)**: use notification categories and tinted icons aligned with `primary`.
- **Large screens**: evaluate a two-pane layout for tablets without changing the card design.

---

*Last updated: 2026-07-16*
