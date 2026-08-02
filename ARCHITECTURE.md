# SpoolSense: Technical Design Document (MVP)

## 1. Goals & Non-Goals

### Goals
* **Defect Prevention**: Eliminate 3D printing failures caused by sudden filament depletion by tracking plastic consumption in real-time.
* **Automated Tracking**: Read plastic consumption data from the Klipper/Moonraker API and automatically subtract it from a local spool database.
* **Offline-First Architecture**: Operate fully autonomously with a local SQLDelight database; no internet required.
* **Non-Blocking UI**: Network operations (WebSockets, HTTP) must be fully asynchronous and never block the Compose UI thread.
* **Cross-Platform Support**: Run seamlessly on Android, iOS (via Compose Multiplatform), Desktop (JVM), and Web (WASM/JS).

### Non-Goals (Out of Scope for MVP)
* **Cloud Synchronization**: No backend servers, user accounts, or cloud backups; all data remains on-device.
* **Native iOS UI (SwiftUI)**: iOS uses Compose Multiplatform, not native SwiftUI.
* **Advanced Analytics**: Complex charts, cost estimation, multi-printer networks, and predictive alerts excluded.
* **In-App Notifications**: Push notifications for low filament (can be added in later phases).

---

## 2. High-Level Architecture & Layer Boundaries

The project follows **Clean Architecture** with strict inward-pointing dependency rules:

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER (MVI)                                    │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Screens (Compose)  → ViewModel (State + Intents)        │ │
│ │ SpoolInventoryScreen → InventorySpoolViewModel          │ │
│ │ InsertSpoolScreen → InsertSpoolViewModel                │ │
│ └─────────────────────────────────────────────────────────┘ │
└──────────────────┬──────────────────────────────────────────┘
                   │ uses
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER (Pure Kotlin)                                  │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ UseCases: ObserveSpoolsUseCase, InsertSpoolUseCase      │ │
│ │ Models: Spool, PrinterState, ConnectionState           │ │
│ │ Interfaces: SpoolRepository, PrinterRepository          │ │
│ │ (ZERO external dependencies)                            │ │
│ └─────────────────────────────────────────────────────────┘ │
└──────────────────┬──────────────────────────────────────────┘
                   │ implements
                   ↓
┌─────────────────────────────────────────────────────────────┐
│ DATA LAYER (SQLDelight + Ktor)                              │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Repositories (SpoolRepositoryImpl, PrinterRepositoryImpl)│ │
│ │ SQLDelight: spoolEntity, printJobEntity                 │ │
│ │ Ktor Client: REST API + WebSocket to Moonraker         │ │
│ │ Mappers: Entity ↔ Domain DTOs ↔ Network DTOs           │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Layer Dependency Rules
* **Domain → Nothing**: Domain layer imports ZERO external dependencies.
* **Data → Domain**: Data layer implements Domain interfaces and maps to Domain models.
* **Presentation → Domain**: Presentation calls UseCases and renders State.
* **No Back-References**: No layer ever imports from layers above it (no circular deps).

### Layer Definitions

| Layer | Responsibility | Key Files |
|-------|---|---|
| **Presentation** | MVI: ViewModel, State, Intent; Compose screens | `*ViewModel.kt`, `*State.kt`, `*Intent.kt`, `*Screen.kt` |
| **Domain** | Business logic, entities, repository interfaces | `UseCase.kt`, `Model.kt`, `Repository.kt` |
| **Data** | DB queries, network calls, data mapping | `RepositoryImpl.kt`, `Mapper.kt`, `.sq` files |

---

## 3. Component Specifications

### A. Database Schema (SQLDelight)

**spoolEntity Table**
```sql
CREATE TABLE spoolEntity (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    vendor TEXT NOT NULL,
    material TEXT NOT NULL,
    totalWeightGrams INTEGER NOT NULL,
    remainingWeightGrams INTEGER NOT NULL,
    colorHex TEXT NOT NULL,
    colorName TEXT NOT NULL DEFAULT 'Unknown'
);
```

**printJobEntity Table**
```sql
CREATE TABLE printJobEntity (
    id TEXT PRIMARY KEY,
    fileName TEXT NOT NULL,
    spoolId TEXT NOT NULL,
    weightUsedGrams INTEGER NOT NULL,
    timestamp INTEGER NOT NULL,
    status TEXT NOT NULL
    -- Future: Add FOREIGN KEY constraint to spoolEntity(id)
);
```

**Key Design Decisions:**
* ✅ `colorHex` (e.g., "FF6B35") is stored for UI rendering.
* ✅ `colorName` (e.g., "Orange") is stored for user readability.
* ✅ `remainingWeightGrams` updated reactively after each print job.
* ⚠️ No explicit index on `printJobEntity.spoolId` yet (add if queries slow down).

### B. Domain Models

**Spool Model**
```kotlin
data class Spool(
    val id: String,
    val name: String = "",
    val vendor: String,
    val material: String = "PLA",
    val totalWeightGrams: Int = 1000,
    val remainingWeightGrams: Int = 1000,
    val colorHex: String,
    val colorName: String = "Unknown"
)
```

**Key Invariants:**
* `remainingWeightGrams ≤ totalWeightGrams` always.
* `id` must be unique (generated as `spool_${timestamp}_${random}`).
* `colorHex` must be exactly 6 characters (validated in Insert form).

### C. Use Cases

Each UseCase encapsulates exactly ONE business action:

| UseCase | Input | Output | Behavior |
|---------|-------|--------|----------|
| `ObserveSpoolsUseCase` | None | `Flow<List<Spool>>` | Emits updated list whenever DB changes; never completes |
| `InsertSpoolUseCase` | `Spool` | `Result<Unit>` (suspend) | Validates spool, inserts into DB; throws if invalid |
| `UpdateSpoolWeightUseCase` | `spoolId`, `weightGrams` | `Result<Unit>` (suspend) | Subtracts weight; throws if goes negative |
| `ObservePrinterStateUseCase` | None | `Flow<ConnectionState>` | Real-time WebSocket updates from Moonraker API |

---

## 4. Presentation Layer (MVI Pattern)

### Model-View-Intent Pattern

**State** (Immutable Data)
```kotlin
data class InventorySpoolState(
    val allSpools: List<Spool> = emptyList(),
    val filteredSpools: List<Spool> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val formErrors: Map<String, String> = emptyMap()
)
```

**Intent** (User/System Actions)
```kotlin
sealed interface InventorySpoolIntent {
    data class UpdateSearch(val value: String) : InventorySpoolIntent
    object Refresh : InventorySpoolIntent
}

sealed interface InsertSpoolIntent {
    data class UpdateVendor(val value: String) : InsertSpoolIntent
    data class UpdateMaterial(val value: String) : InsertSpoolIntent
    data class UpdateColorName(val value: String) : InsertSpoolIntent
    data class UpdateColorHex(val value: String) : InsertSpoolIntent
    data class UpdateTotalWeight(val value: String) : InsertSpoolIntent
    object Submit : InsertSpoolIntent
    object ClearForm : InsertSpoolIntent
}
```

**ViewModel Reducer Logic**
```kotlin
fun handleIntent(intent: InventorySpoolIntent) {
    when (intent) {
        is InventorySpoolIntent.UpdateSearch -> {
            val filtered = filterSpools(state.allSpools, intent.value)
            _state.update { it.copy(searchQuery = intent.value, filteredSpools = filtered) }
        }
        InventorySpoolIntent.Refresh -> loadSpools()
    }
}
```

### Validation Rules (Insert Form)

| Field | Rule | Error Message |
|-------|------|---------------|
| `vendor` | Non-empty | "Vendor is required" |
| `material` | Non-empty | "Material is required" |
| `colorName` | Non-empty | "Color name is required" |
| `colorHex` | Exactly 6 hex digits | "Valid hex color required (6 digits)" |
| `totalWeightGrams` | Integer > 0 | "Weight must be greater than 0" |

---

## 5. Data Flow Scenarios

### Scenario A: App Startup & Display Inventory

1. **User launches app** → Compose renders `SpoolInventoryScreen`
2. **Screen subscribes** to `viewModel.state` (StateFlow)
3. **ViewModel init** calls `loadSpools()` which calls `ObserveSpoolsUseCase()`
4. **UseCase** subscribes to `repository.observeAllSpools()`
5. **Repository** returns `Flow` from SQLDelight query
6. **Flow emits** current DB contents as `List<Spool>`
7. **ViewModel** updates `_state` → `filteredSpools = spools`
8. **Screen** recomposes and displays grid of `SpoolCard` components
9. ✅ User sees all filament spools instantly (from local DB)

**Key Points:**
* Zero network calls; fully offline.
* If DB is empty, screen shows "No spools yet" placeholder.
* If search query is active, only matching spools are displayed.

### Scenario B: User Adds a New Spool

1. **User taps "Add Spool"** → Navigate to `InsertSpoolScreen`
2. **User fills form**: Vendor, Material, Color Name, Hex, Weight
3. **User taps "Submit"** → ViewModel receives `InsertSpoolIntent.Submit`
4. **ViewModel validates** form fields (see Validation Rules above)
5. **If errors** → Display `formErrors: Map<String, String>`; do NOT proceed
6. **If valid** → Call `InsertSpoolUseCase(newSpool)` with `isLoading = true`
7. **UseCase** → `repository.insertSpool(spool)` (suspend function)
8. **Repository** → `queries.insertSpool(...)` SQL insert
9. **SQLDelight** → Writes to local DB; `observeAllSpools()` Flow automatically re-emits
10. **ViewModel** receives new list → `_state.update { copy(filteredSpools = ..., successMessage = "...") }`
11. **Screen** shows success snackbar → Auto-dismiss after 2s
12. ✅ New spool appears in Inventory grid; form clears

**Error Handling:**
* If `insertSpool()` throws (disk full, DB locked) → Catch exception → `errorMessage = "Failed to add spool"`
* User can tap "Retry" to try again

### Scenario C: Real-Time Print Completion (Future Phase)

1. **Printer printing** → `ObservePrinterStateUseCase` WebSocket listener active
2. **Print finishes** → Moonraker API sends `{"print_stats": {"state": "complete", "total_weight_used_grams": 42}}`
3. **ViewModel** receives `ConnectionState.Connected { ... }` with new weight
4. **ViewModel** triggers `UpdateSpoolWeightUseCase(activeSpool.id, 42)`
5. **UseCase** → `repository.updateRemainingWeight(id, remaining - 42)`
6. **Repository** → SQL UPDATE query
7. **SQLDelight** → DB updates; `observeAllSpools()` Flow re-emits
8. **Screen** UI automatically updates** to show new weight ✅

---

## 6. Error Handling Strategy

### Network Errors (Moonraker API)
| Scenario | Handling |
|----------|----------|
| Connection timeout | `ConnectionState.Disconnected` + error toast |
| Invalid JSON from API | Log error; emit `ConnectionState.Disconnected` |
| WebSocket drops | Auto-retry with exponential backoff (not MVP) |

### Database Errors
| Scenario | Handling |
|----------|----------|
| Disk full | Catch exception in UseCase; show alert to user |
| Corrupted DB | App should offer "Factory Reset" option |

### Validation Errors (Form)
| Scenario | Handling |
|----------|----------|
| Invalid hex color | Show red border on TextField + supporting text |
| Non-numeric weight | TextField keyboardType = Number; reject input |
| Empty vendor field | Highlight; do NOT submit |

---

## 7. Testing Strategy

### Unit Tests (Domain Layer)
```kotlin
class ObserveSpoolsUseCaseTest {
    @Test
    fun testEmitsSpoolsFromRepository() {
        // Arrange: Mock repository returns Flow of 2 spools
        // Act: Call useCase()
        // Assert: Flow emits exactly 2 spools
    }
}
```

### Integration Tests (Data Layer)
```kotlin
class SpoolRepositoryImplTest {
    @Test
    fun testInsertAndSelectSpool() {
        // Arrange: In-memory DB
        // Act: Insert spool; query by ID
        // Assert: Spool found with all fields correct
    }
}
```

### UI Tests (Presentation Layer)
```kotlin
class InsertSpoolScreenTest {
    @Test
    fun testFormValidationErrorsDisplayed() {
        // Arrange: Render InsertSpoolScreen with empty form
        // Act: Tap Submit
        // Assert: formErrors map populated; red borders visible
    }
}
```

---

## 8. Performance & Constraints

| Constraint | Target | Rationale |
|-----------|--------|-----------|
| **Inventory list load time** | < 500ms | SQLDelight query on local DB |
| **Insert spool submit** | < 1s | Form validation + single DB insert |
| **Search filter latency** | < 100ms | In-memory filtering (no DB query) |
| **WebSocket reconnect** | < 5s | Manual retry (auto-retry in Phase 2) |
| **Max spools per user** | 100+ | No pagination needed for MVP |

---

## 9. Future Enhancements (Phase 2+)

* ☐ Print history detail screen (view past jobs per spool)
* ☐ Edit/Delete spool functionality
* ☐ Multi-printer support (switch between printers in UI)
* ☐ Low-filament alerts (< 10% remaining)
* ☐ Spool cost tracking & print cost calculation
* ☐ Cloud backup (optional, off-device)
* ☐ Dark mode support
* ☐ Spool barcode scanning

---

## 10. File Structure Reference

```
shared/src/commonMain/kotlin/com/spoolsense/shared/
├── presentation/
│   ├── inventory/
│   │   ├── InventorySpoolIntent.kt
│   │   ├── InventorySpoolState.kt
│   │   ├── InventorySpoolViewModel.kt
│   │   ├── SpoolInventoryScreen.kt
│   │   └── SpoolInventoryComponents.kt
│   ├── insert/
│   │   ├── InsertSpoolIntent.kt
│   │   ├── InsertSpoolState.kt
│   │   ├── InsertSpoolViewModel.kt
│   │   └── InsertSpoolScreen.kt
│   └── monitor/ (future)
│       └── PrinterMonitorScreen.kt
├── domain/
│   ├── model/
│   │   ├── Spool.kt
│   │   └── PrinterState.kt
│   ├── repository/
│   │   ├── SpoolRepository.kt
│   │   └── PrinterRepository.kt
│   └── usecase/
│       ├── ObserveSpoolsUseCase.kt
│       ├── InsertSpoolUseCase.kt
│       ├── UpdateSpoolWeightUseCase.kt
│       └── ObservePrinterStateUseCase.kt
├── data/
│   ├── repository/
│   │   ├── SpoolRepositoryImpl.kt
│   │   └── PrinterRepositoryImpl.kt
│   ├── mapper/
│   │   └── SpoolMapper.kt
│   ├── network/
│   │   ├── KtorClientFactory.kt
│   │   └── dto/
│   │       └── MoonrakerPrinterStateDto.kt
│   └── database/
│       └── SpoolDatabase.kt
└── sqldelight/
    └── SpoolDatabase.sq
```

---

## References & Resources

* [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/)
* [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
* [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
* [Ktor Client Documentation](https://ktor.io/docs/client-index.html)
* [Klipper API Documentation](https://github.com/Klipper/klipper/tree/master/docs)
* [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

Each UseCase encapsulates a single, isolated business rule.

* `GetFilamentListUseCase()`
    * **Input**: None.
    * **Output**: `Flow<List<Spool>>` (Emits updated lists whenever the local SQL database changes).
* `UpdateFilamentWeightUseCase(spoolRepository: SpoolRepository)`
    * **Input**: `spoolId: String`, `weightUsedGrams: Int`.
    * **Output**: `Result<Unit>` (Handles business logic validation before updating the data layer).
* `ObservePrinterStateUseCase(printerRepository: PrinterRepository)`
    * **Input**: None.
    * **Output**: `Flow<ConnectionState>` (Emits realtime updates mapping Ktor WebSocket DTOs to Domain ConnectionState).

### C. Presentation Layer (MVI Contract)

The Presentation layer relies on a strict MVI architecture.

**State**
The screen state is defined by a single immutable data class representing a screen snapshot:
* `PrinterMonitorState` contains `connectionState` (defaults to Disconnected), `activeSpool`, and an optional `errorMessage`.
* `ConnectionState` is a sealed interface encompassing `Disconnected`, `Connecting`, and `Connected` (which holds status, nozzleTemperature, bedTemperature, and progress).

**Intent**
User and system actions are modeled as a sealed interface:
* `PrinterMonitorIntent` includes actions such as `Connect(val ipAddress: String)`, `Disconnect`, `TogglePause`, and `SelectSpool(val spoolId: String)`.

**Reducer Logic**
* `Intent.Connect`: Mutates state to `ConnectionState.Connecting`. Triggers the connection UseCase. Upon success, mutates to `ConnectionState.Connected`.
* `Intent.SelectSpool`: Mutates the `activeSpool` field within `PrinterMonitorState` based on the provided `spoolId`.

---

## 4. Critical Data Flows (Step-by-Step)

### Scenario A: Initial Application Startup & Real-time Connection
1.  The user launches the application and navigates to the main Compose screen.
2.  The UI automatically binds to the ViewModel's state flow, triggering the collection of local data via `GetFilamentListUseCase`.
3.  The database (via SQLDelight) emits a `Flow` containing the saved spools, and the Reducer updates `PrinterMonitorState.activeSpool` with the previously selected or default spool.
4.  Simultaneously, the ViewModel dispatches a `PrinterMonitorIntent.Connect` action.
5.  The ViewModel invokes `ObservePrinterStateUseCase`, transitioning the state to `ConnectionState.Connecting`.
6.  The Data layer establishes a Ktor WebSocket connection to Moonraker API 23.
7.  Upon connection success, the incoming WebSocket stream is mapped to domain objects, mutating the state to `ConnectionState.Connected` and rendering live temperatures and progress to the user.

### Scenario B: Print Completion & Filament Deduction
1.  The Ktor WebSocket client receives an asynchronous payload from the Moonraker API indicating the printer status has changed to `SUCCESS`.
2.  The network Data Transfer Object (DTO) containing the total extruded plastic metrics is parsed in the Data Layer.
3.  `ObservePrinterStateUseCase` emits this finalized data up to the ViewModel.
4.  The ViewModel recognizes the print completion and automatically triggers `UpdateFilamentWeightUseCase`, passing the `activeSpool.id` and the total `weightUsedGrams`.
5.  The Repository executes an SQL query via SQLDelight to subtract the consumed amount from `remainingWeightGrams` in the `spoolEntity` table.
6.  It also inserts a new record into `printJobEntity` with `status = SUCCESS`.
7.  Because `GetFilamentListUseCase` is a reactive Kotlin Flow observing the database, the updated filament weight is automatically pushed to the UI, reflecting the newly reduced spool capacity without manual refreshes.