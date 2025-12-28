package org.nihongo.mochi.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module
import org.nihongo.mochi.domain.kana.AndroidResourceLoader
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.koin.android.ext.koin.androidContext

val appModule = module {
    single<ResourceLoader> { AndroidResourceLoader(androidContext()) }
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
}
