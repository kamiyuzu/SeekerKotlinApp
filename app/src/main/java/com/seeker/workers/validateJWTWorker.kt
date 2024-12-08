package com.seeker.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seeker.external.services.validateJWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val JWT_WORKER_TAG = "validateJWTWorker"

class ValidateJWTWorker(private val ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        Log.println(Log.DEBUG, JWT_WORKER_TAG, "Starting ValidateJWTWorker...")
        return withContext(Dispatchers.IO) {
            return@withContext try {
                if (validateJWT() == "valid") {
                    Log.println(Log.DEBUG, JWT_WORKER_TAG, "ValidateJWTWorker validated JWT successfully")
                    makeStatusNotification("Your current JWT is valid", ctx, true)
                    Result.success()
                }
                else {
                    Log.println(Log.DEBUG, JWT_WORKER_TAG, "ValidateJWTWorker validated JWT with errors")
                    makeStatusNotification("Your current JWT is not valid", ctx, false)
                    Result.failure()
                }
            } catch (throwable: Throwable) {
                Log.println(Log.DEBUG, JWT_WORKER_TAG, "Error in validate JWT worker: $throwable")
                Result.failure()
            }
        }
    }
}