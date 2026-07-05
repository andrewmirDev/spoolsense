# SpoolSense: Technical Design Document (MVP)

## 1. Goals & Non-Goals

### Goals
* **Defect Prevention**: The primary objective is to eliminate 3D printing failures caused by sudden filament depletion.
* **Automated Tracking**: The application must read plastic consumption data from the Klipper/Moonraker API and automatically subtract it from a local spool database.
* **Offline-First Architecture**: The application must operate fully autonomously utilizing a local database.
* **Non-Blocking UI**: The network layer communicating with the printer must operate asynchronously and must not block the user interface.

### Non-Goals (Out of Scope for MVP)
* **Cloud Synchronization**: No backend servers, user accounts, or cloud backups will be implemented; data remains strictly on-device.
* **Native iOS UI (SwiftUI)**: iOS is a future target, but we will not build a native Swift UI We strictly use Compose Multiplatform for the UI framework.
* **Advanced Analytics**: Complex charts, print cost estimations, and multi-printer mesh networks are excluded from the MVP.

---

## 2. High-Level Architecture & Layer Boundaries

The project follows a strict Clean Architecture pattern encapsulated within a single Kotlin Multiplatform module (`:shared`).

### Layer Dependency Rules
Dependencies point strictly inward toward the Domain layer.
* **Domain Layer**: Absolutely pure Kotlin code with zero external dependencies (no Android, no Ktor, no SQLDelight imports).
* **Data Layer**: Depends on the Domain layer to implement interface contracts.
* **Presentation Layer**: Depends on the Domain layer (UseCases) to trigger business logic.

### Layer Definitions
* **`:shared:data`**: Contains the implementation of Repositories, SQLDelight Data Access Objects (DAOs), and Ktor API services. This layer holds Network Data Transfer Objects (DTOs) and Base Entity models.
* **`:shared:domain`**: Contains Repository interfaces, Business Models, and UseCases (Interactors). One business action strictly equals one UseCase.
* **`:shared:presentation`**: Implements the Model-View-Intent (MVI) pattern, encapsulated within ViewModels. The View layer is represented by Compose screens that solely render State and dispatch Intents.

---

## 3. Component Specifications

### A. Data Layer (SQLDelight & Ktor)

**Database Schema (SQLDelight)**
The local database utilizes the following core schema for `.sq` files:
* **`spoolEntity`**: Tracks filament spools. Includes fields for `id` (PRIMARY KEY), `name`, `vendor`, `material`, `totalWeightGrams`, `remainingWeightGrams`, and `colorHex`.
* **`printJobEntity`**: Tracks printing history. Includes fields for `id` (PRIMARY KEY), `fileName`, `spoolId`, `weightUsedGrams`, `timestamp`, and `status`.
* *Index Recommendation*: An index should be created on `spoolId` within `printJobEntity` to optimize querying print history for specific spools.

**Network Client (Ktor + Moonraker API)**
* **REST API**: Uses Ktor Client for standard HTTP requests. Essential endpoint: `/printer/objects/query` to fetch current print objects and static metadata.
* **WebSockets**: Uses Ktor WebSockets to receive real-time temperature and status updates. We will subscribe to Klipper status topics (e.g., `print_stats`, `extruder` temperatures) to monitor active consumption and state changes.

### B. Domain Layer (Use Cases)

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