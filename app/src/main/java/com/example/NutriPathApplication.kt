package com.example

import android.app.Application
import com.example.backend.data.local.AppDatabase
import com.example.backend.data.repository.NutriPathRepository
import com.example.backend.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NutriPathApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { NutriPathRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Seed initial foods and ensure initial state
        applicationScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    companion object {
        lateinit var instance: NutriPathApplication
            private set
    }
}
