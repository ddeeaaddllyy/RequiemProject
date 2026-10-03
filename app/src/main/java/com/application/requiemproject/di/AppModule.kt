package com.application.requiemproject.di

import com.application.requiemproject.data.api.RetrofitClient
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.data.repository.*
import com.application.requiemproject.data.translator.MyMemoryTranslator
import com.application.requiemproject.domain.model.TranslatorModel
import com.application.requiemproject.domain.repository.*
import com.application.requiemproject.domain.usecase.*
import com.application.requiemproject.presentation.account.AccountViewModel
import com.application.requiemproject.presentation.help.HelpViewModel
import com.application.requiemproject.presentation.home.HomeViewModel
import com.application.requiemproject.presentation.navigation.NavigationViewModel
import com.application.requiemproject.presentation.overlay.OverlayManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().userDao() }
    single { SessionManager(androidContext()) }
    single { RetrofitClient.api }
    single { TranslationSettingsRepository(androidContext()) }
    single<SettingsRepository> { get<TranslationSettingsRepository>() }
    single<AccountRepository> { LocalAccountRepository(get(), get()) }
    single<HelpRepository> { LocalHelpRepository() }
    single<TranslatorModel> { MyMemoryTranslator(get(), get(), get()) }
    factory<TranslationOverlay> { OverlayManager(androidContext()) }
    factory { OCRRepository() }
    factory { AccountUseCase(get()) }
    factory { TranslationSettingsUseCase(get()) }
    factory { SearchHelpUseCase(get()) }
    factory { TranslateBlocksUseCase(get()) }
    viewModel { AccountViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { HelpViewModel(get(), get()) }
    viewModel { NavigationViewModel(get()) }
}
