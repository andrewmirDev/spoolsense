package com.spoolsense.shared.di

import com.spoolsense.app.shared.data.network.KtorClientFactory
import com.spoolsense.app.shared.data.repository.SpoolRepositoryImpl
import com.spoolsense.app.shared.domain.repository.SpoolRepository
import com.spoolsense.shared.data.repository.PrinterRepositoryImpl
import com.spoolsense.shared.database.SpoolDatabase
import com.spoolsense.shared.domain.repository.PrinterRepository
import com.spoolsense.shared.domain.usecase.ObservePrinterStateUseCase
import com.spoolsense.shared.domain.usecase.ObserveSpoolsUseCase
import com.spoolsense.shared.domain.usecase.UpdateSpoolWeightUseCase
import com.spoolsense.shared.presentation.dashboard.SpoolListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { KtorClientFactory.create() }
    single { SpoolDatabase(get()) }
    single<SpoolRepository> { SpoolRepositoryImpl(get()) }
    single<PrinterRepository> { PrinterRepositoryImpl(get()) }
}

val domainModule = module{
    factory { ObserveSpoolsUseCase(get()) }
    factory { UpdateSpoolWeightUseCase(get()) }
    factory { ObservePrinterStateUseCase(get()) }
}

val presentationModule = module {
    factory { SpoolListViewModel(get(), get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

fun initKoin() = initKoin {}