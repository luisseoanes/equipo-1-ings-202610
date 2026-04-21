// Ruta: app/src/main/java/com/upb/obsia/di/AppModule.kt

package com.upb.obsia.di

import com.upb.obsia.data.AppDatabase
import com.upb.obsia.data.repository.ChatRepositoryImpl
import com.upb.obsia.data.repository.EngineRepositoryImpl
import com.upb.obsia.domain.repository.ChatRepository
import com.upb.obsia.domain.repository.EngineRepository
import com.upb.obsia.ui.viewmodel.ChatListViewModel
import com.upb.obsia.ui.viewmodel.ChatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // ─── Base de datos ────────────────────────────────────────────────────────
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().chatMessageDao() }
    single { get<AppDatabase>().chatSessionDao() }
    single { get<AppDatabase>().userDao() }

    // ─── Repositorios ─────────────────────────────────────────────────────────
    single<EngineRepository> { EngineRepositoryImpl(androidContext()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }

    // ─── ViewModels ───────────────────────────────────────────────────────────
    viewModel { ChatViewModel(androidContext(), get(), get()) }
    viewModel { ChatListViewModel(androidContext(), get()) }
}
