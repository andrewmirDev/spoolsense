package com.spoolsense.shared.di

import com.spoolsense.shared.data.network.KtorClientFactory
import com.spoolsense.shared.data.repository.ActiveSpoolRepositoryImpl
import com.spoolsense.shared.data.repository.PrinterRepositoryImpl
import com.spoolsense.shared.data.repository.PrinterSettingsRepositoryImpl
import com.spoolsense.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.domain.repository.ActiveSpoolRepository
import com.spoolsense.shared.domain.repository.PrinterRepository
import com.spoolsense.shared.domain.repository.PrinterSettingsRepository
import com.spoolsense.shared.domain.repository.SpoolRepository
import com.spoolsense.shared.domain.usecase.InsertSpoolUseCase
import com.spoolsense.shared.domain.usecase.ObserveActiveSpoolUseCase
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import com.spoolsense.shared.domain.usecase.OnPrintCompletedUseCase
import com.spoolsense.shared.domain.usecase.SetActiveSpoolUseCase
import com.spoolsense.shared.domain.usecase.UpdateSpoolWeightUseCase
import com.spoolsense.shared.presentation.dashboard.DashboardListViewModel
import com.spoolsense.shared.presentation.inventory.InventorySpoolViewModel
import com.spoolsense.shared.presentation.settings.PrinterSettingsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { KtorClientFactory.create() }
    single { SpoolDatabase(get()) }
    single<SpoolRepository> { SpoolRepositoryImpl(get()) }
    single<PrinterSettingsRepository> { PrinterSettingsRepositoryImpl(get()) }
    single<ActiveSpoolRepository> { ActiveSpoolRepositoryImpl(get()) }
    single<PrinterRepository> { PrinterRepositoryImpl(get(), get()) }
}

val domainModule = module {
    factory { ObserveSpoolsUseCase(get()) }
    factory { UpdateSpoolWeightUseCase(get()) }
    factory { ObservePrinterStateUseCase(get()) }
    factory { InsertSpoolUseCase(get()) }
    factory { ObserveActiveSpoolUseCase(get()) }
    factory { SetActiveSpoolUseCase(get()) }
    factory { OnPrintCompletedUseCase(get(), get()) }
}

val presentationModule = module {
    factory { DashboardListViewModel(get(), get(), get(), get(), get()) }
    factory { InventorySpoolViewModel(get()) }
    factory { com.spoolsense.shared.presentation.insert.InsertSpoolViewModel(get()) }
    factory { PrinterSettingsViewModel(get(), get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

fun initKoin() = initKoin {}
