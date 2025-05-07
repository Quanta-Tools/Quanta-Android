package tools.quanta.sdk.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Listens for the application launch event. The "launch" is defined as the first time the
 * application process is created.
 */
class AppLaunchListener(private val onAppLaunch: () -> Unit) {

    private val launchEventFired = AtomicBoolean(false)

    private val lifecycleObserver =
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    if (launchEventFired.compareAndSet(false, true)) {
                        onAppLaunch()
                        // After the launch event is fired, we can remove the observer
                        // if this listener is only intended for a one-time launch event.
                        // ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
                    }
                }
            }

    /**
     * Starts listening for the app launch event. It's safe to call this multiple times; the
     * observer will only be added once and the launch event will only be reported once.
     */
    fun startListening() {
        // addObserver is main-thread safe and idempotent if the same observer is added multiple
        // times.
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    /**
     * Stops listening for the app launch event. This is useful if the listener needs to be
     * explicitly shut down.
     */
    fun stopListening() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        // Optionally, reset the flag if the listener instance might be reused for some reason,
        // though typically an AppLaunchListener is for a single launch.
        // launchEventFired.set(false)
    }
}
