package com.spoolsense.shared.di

import com.spoolsense.app.shared.data.network.KtorClientFactory
import com.spoolsense.shared.data.MockDataInitializer
import com.spoolsense.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.shared.domain.repository.SpoolRepository
import com.spoolsense.shared.data.repository.PrinterRepositoryImpl
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.domain.repository.PrinterRepository
import com.spoolsense.shared.domain.usecase.InsertSpoolUseCase
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import com.spoolsense.shared.domain.usecase.UpdateSpoolWeightUseCase
import com.spoolsense.shared.presentation.dashboard.DashboardListViewModel
import com.spoolsense.shared.presentation.inventory.InventorySpoolViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { KtorClientFactory.create() }
    single { 
        println("🗄️ Creating SpoolDatabase...")
        SpoolDatabase(get()) 
    }
    single<SpoolRepository> { 
        println("🗄️ Creating SpoolRepositoryImpl...")
        SpoolRepositoryImpl(get()) 
    }
    single<PrinterRepository> { PrinterRepositoryImpl(get()) }
}

val domainModule = module{
    factory { ObserveSpoolsUseCase(get()) }
    factory { UpdateSpoolWeightUseCase(get()) }
    factory { ObservePrinterStateUseCase(get()) }
    factory { InsertSpoolUseCase(get()) }
}

val presentationModule = module {
    factory { DashboardListViewModel(get(), get()) }
    factory { InventorySpoolViewModel(get()) }
    factory { com.spoolsense.shared.presentation.insert.InsertSpoolViewModel(get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

fun initKoin() = initKoin {}