package com.spoolsense.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.spoolsense.shared.data.MockDataInitializer
import com.spoolsense.shared.di.initKoin
import com.spoolsense.shared.domain.repository.SpoolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.java.KoinJavaComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
        }

        val repository = KoinJavaComponent.get<SpoolRepository>(SpoolRepository::class.java)
        MockDataInitializer.initializeIfEmpty(repository, CoroutineScope(SupervisorJob() + Dispatchers.IO))
    }
}