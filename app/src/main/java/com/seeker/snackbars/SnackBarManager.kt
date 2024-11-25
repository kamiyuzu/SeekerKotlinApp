package com.seeker.snackbars

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.AtomicReference
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList

class SnackbarManager(
    private val snackbarHostState: SnackbarHostState,
) {

    private val mutex = Mutex()
    private val isRunning: AtomicReference<Boolean> = AtomicReference(false)
    private val snackbarQueue = LinkedList<SnackbarData>()

    suspend fun queue(snackbarData: SnackbarData) {
        mutex.withLock {
            snackbarQueue.add(snackbarData)
            if (isRunning.compareAndSet(false, true)) {
                startDequeue()
            }
        }
    }

    private suspend fun startDequeue() {

        val shouldContinue: suspend () -> Unit = {
            if (snackbarQueue.isEmpty()) isRunning.set(false)
            else startDequeue()
        }

        val poppedItem = snackbarQueue.pop()
        val result = snackbarHostState.showSnackbar(
            poppedItem.message,
            poppedItem.actionLabel,
            poppedItem.withDismissAction,
            poppedItem.duration
        )

        when (result) {
            SnackbarResult.Dismissed -> {
                shouldContinue()
            }

            SnackbarResult.ActionPerformed -> {
                shouldContinue()
            }
        }
    }

    data class SnackbarData(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val withDismissAction: Boolean = false,
        val actionLabel: String? = null,
    )
}