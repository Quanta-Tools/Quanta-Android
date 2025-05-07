package tools.quanta.sdk.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*

class ForegroundLoopManager(
    private val task: suspend () -> Unit,
    private val delayMillis: Long = 1000L // Default delay of 1 second
) {

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            startLoop()
        }

        override fun onStop(owner: LifecycleOwner) {
            stopLoop()
        }
    }

    private fun startLoop() {
        if (job?.isActive == true) return // Loop already running
        job = scope.launch {
            while (isActive) {
                try {
                    task()
                } catch (e: Exception) {
                    // Handle exceptions from the task, e.g., log them
                    e.printStackTrace()
                    // Optionally, decide if the loop should continue or stop based on the error
                }
                delay(delayMillis)
            }
        }
    }

    private fun stopLoop() {
        job?.cancel()
        job = null
    }

    /**
     * Starts observing the application lifecycle to manage the loop.
     * The loop will automatically start when the app enters the foreground
     * and stop when it enters the background.
     */
    fun startObserving() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        // If the app is already in the foreground when startObserving is called,
        // explicitly start the loop.
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
            startLoop()
        }
    }

    /**
     * Stops observing the application lifecycle and cancels the loop if it's running.
     */
    fun stopObserving() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        stopLoop()
        // Clean up the scope if the manager is intended to be completely shut down
        // and not reused. Be cautious if other coroutines might be using this scope.
        // scope.cancel() // Uncomment if complete cleanup is needed and scope is not shared.
    }
}
