package org.technoserve.farmcollector

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.technoserve.farmcollector.database.sync.SyncWorker
import java.util.concurrent.TimeUnit

class FarmCollectorApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        val config = Configuration.Builder()
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//        WorkManager.initialize(this, config)
//        android.util.Log.d("WorkManager", "WorkManager initialized successfully")
        initializeWorkManager()
    }

    // private
    fun initializeWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_work_tag",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
