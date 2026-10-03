package com.application.requiemproject.di

import android.content.Context
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.application.requiemproject.data.api.RetrofitClient
import com.application.requiemproject.data.local.SessionManager
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.data.repository.LocalAccountRepository
import com.application.requiemproject.data.repository.LocalHelpRepository
import com.application.requiemproject.data.repository.TranslationSettingsRepository
import com.application.requiemproject.data.translator.MyMemoryTranslator
import com.application.requiemproject.domain.usecase.AccountUseCase
import com.application.requiemproject.domain.usecase.SearchHelpUseCase
import com.application.requiemproject.domain.usecase.TranslationSettingsUseCase
import com.application.requiemproject.presentation.account.AccountViewModel
import com.application.requiemproject.presentation.help.HelpViewModel
import com.application.requiemproject.presentation.home.HomeViewModel
import com.application.requiemproject.presentation.navigation.NavigationViewModel
import com.application.requiemproject.presentation.overlay.OverlayManager
import com.application.requiemproject.domain.repository.TranslationOverlay

class AppContainer(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val session = SessionManager(context)
    val settings = TranslationSettingsRepository(context)
    val translator = MyMemoryTranslator(RetrofitClient.api, database.userDao(), session)
    fun createOverlay(): TranslationOverlay = OverlayManager(context)
    private val accountUseCase = AccountUseCase(LocalAccountRepository(database.userDao(), session))
    val viewModelFactory = viewModelFactory {
        initializer { AccountViewModel(accountUseCase) }
        initializer { HomeViewModel(TranslationSettingsUseCase(settings), createSavedStateHandle()) }
        initializer { HelpViewModel(SearchHelpUseCase(LocalHelpRepository()), createSavedStateHandle()) }
        initializer { NavigationViewModel(createSavedStateHandle()) }
    }
}
