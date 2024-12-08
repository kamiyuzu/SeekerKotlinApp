package com.seeker.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

const val TAG_JWT = "JWT_REPOSITORY"
const val TAG_JWT_UNIQUE_WORK = "JWT_UNIQUE_WORK"

class WorkManagerSeekerRepository(context: Context) : SeekerRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun validateJWTWork() {
        // Create network constraint
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Add PeriodicWorkRequest to validate JWT
        val workJWTBuilder = PeriodicWorkRequestBuilder<ValidateJWTWorker>(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS, PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAG_JWT)
            .build()

        // Start PeriodicWorkRequest to validate JWT
        workManager.enqueueUniquePeriodicWork(TAG_JWT_UNIQUE_WORK, ExistingPeriodicWorkPolicy.UPDATE, workJWTBuilder)
    }
}
