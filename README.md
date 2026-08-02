# SpoolSense: 3D Printer Filament Tracker

**SpoolSense** is a Kotlin Multiplatform application designed to eliminate 3D printing failures caused by filament depletion. It tracks plastic consumption in real-time by connecting to your Klipper/Moonraker API and maintaining an offline-first local database.

## 🎯 Problem & Solution

**Problem:** 3D prints can fail catastrophically if filament runs out mid-print, wasting hours of print time and material.

**Solution:** SpoolSense automatically monitors which spool is active, reads consumption data from your printer via Moonraker API, and alerts you before the filament depletes.

## ✨ Features (MVP)

✅ **Inventory Management**: Add, view, and search filament spools  
✅ **Real-Time Tracking**: Monitor remaining filament as prints progress  
✅ **Offline-First**: All data stored locally; no internet required  
✅ **Cross-Platform**: Android, iOS (Compose Multiplatform), Desktop, Web  
✅ **Non-Blocking UI**: Network operations never freeze the UI  

## 🏗️ Architecture

SpoolSense follows **Clean Architecture** with strict layer separation:

- **Presentation Layer**: MVI pattern with Compose Multiplatform UI
- **Domain Layer**: Pure Kotlin business logic (zero external deps)
- **Data Layer**: SQLDelight for persistence, Ktor for printer API calls

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed technical documentation.

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Android Studio 2024+ or IntelliJ IDEA 2024+
- Gradle 8.0+

### Running the Application

**Android:**
```bash
./gradlew :androidApp:assembleDebug
```

**Desktop:**
```bash
./gradlew :desktopApp:run
```

**Web (WASM - Recommended):**
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

**iOS:**
Open `/iosApp` in Xcode and run from there.

### Running Tests

```bash
./gradlew :shared:testAndroidHostTest    # Android tests
./gradlew :shared:jvmTest                # Desktop tests
./gradlew :shared:wasmJsTest             # Web tests (WASM)
```

## 📂 Project Structure

```
spoolsense/
├── shared/src/commonMain/
│   ├── kotlin/
│   │   ├── presentation/      # MVI pattern (Screens, ViewModels, State, Intent)
│   │   ├── domain/            # Business logic (UseCases, Models, Interfaces)
│   │   ├── data/              # Persistence (Repositories, Mappers, Network)
│   │   └── di/                # Koin dependency injection
│   └── sqldelight/            # Database schema (.sq files)
├── androidApp/                # Android entry point
├── desktopApp/                # Desktop (JVM) entry point
├── iosApp/                    # iOS entry point
└── webApp/                    # Web (WASM/JS) entry point
```

## 🗂️ Key Files

| File | Purpose |
|------|---------|
| `ARCHITECTURE.md` | Detailed technical design & data flows |
| `shared/src/commonMain/sqldelight/SpoolDatabase.sq` | SQLDelight schema |
| `shared/src/commonMain/kotlin/presentation/inventory/` | Inventory list screen |
| `shared/src/commonMain/kotlin/presentation/insert/` | Add new spool form |
| `shared/src/commonMain/kotlin/di/Koin.kt` | Dependency injection setup |

## 🔄 Data Flow Example

### Adding a New Spool
1. User taps "Add Spool" → Opens `InsertSpoolScreen`
2. User fills form (vendor, material, color, weight)
3. User taps "Submit" → ViewModel validates form
4. Valid → `InsertSpoolUseCase(spool)` → DB insert
5. SQLDelight triggers Flow update → Inventory screen re-renders
6. ✅ New spool appears instantly

### Tracking Print Consumption (Future Phase)
1. Print starts → WebSocket listener connects to Moonraker API
2. Print finishes → API sends `weightUsedGrams: 42`
3. `UpdateSpoolWeightUseCase(spoolId, 42)` → DB update
4. Flow re-emits → UI shows updated remaining weight automatically

## 🐛 Testing

- **Unit Tests**: Domain layer UseCases (pure logic, no deps)
- **Integration Tests**: Data layer Repositories (SQLDelight queries)
- **UI Tests**: Compose screens (form validation, state updates)

Example:
```bash
./gradlew :shared:jvmTest --tests "InsertSpoolUseCaseTest"
```

## 🛠️ Development

### Code Style
- Follow Kotlin style guide
- MVI pattern: one Intent per user action
- State immutability: use `data class` with `copy()`
- No circular dependencies between layers

### Adding a New Feature
1. Create domain model + UseCase (Domain layer)
2. Implement repository method (Data layer)
3. Create ViewModel + State + Intent (Presentation layer)
4. Build Compose screen that subscribes to `viewModel.state`

### Database Changes
1. Modify `SpoolDatabase.sq` schema
2. Kotlin code-gen creates queries automatically
3. Update Mapper if model fields change

## 📱 Platform-Specific Notes

- **Android**: Uses native SQLDelight driver; full feature parity
- **iOS**: Compose Multiplatform (no SwiftUI); same code as Android
- **Desktop (JVM)**: Uses SQLite JDBC driver
- **Web (WASM)**: IndexedDB or localStorage for persistence

## 🔐 Security & Privacy

- ✅ All data stored locally (no cloud sync)
- ✅ No user accounts or authentication required
- ✅ No analytics or telemetry
- ✅ Offline-first: works completely without internet

## 📖 Learn More

- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Ktor Client Documentation](https://ktor.io/docs/client-index.html)
- [Klipper API Docs](https://github.com/Klipper/klipper/tree/master/docs)

## 📝 License

[To be determined]

## 🤝 Contributing

Contributions welcome! Please open an issue or pull request.

---

**Status**: MVP Phase 1 in development  
**Last Updated**: 2026-08-01  
**Maintainer**: @andrewmirDev
