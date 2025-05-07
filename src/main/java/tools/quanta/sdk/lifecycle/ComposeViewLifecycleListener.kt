package tools.quanta.sdk.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * A listener that provides a Composable function to observe view appearance and disappearance events
 * within a Jetpack Compose UI.
 *
 * @param onAppear Callback invoked when the Composable view is considered to have appeared (ON_START).
 * @param onDisappear Callback invoked when the Composable view is considered to have disappeared (ON_STOP).
 */
class ComposeViewLifecycleListener(
    private val onAppear: () -> Unit,
    private val onDisappear: () -> Unit
) {

    /**
     * A Composable function that observes the lifecycle of its placement.
     * Call this function within your Composable UI where you want to track appearance/disappearance.
     */
    @Composable
    fun ComposableObserver() {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, this) { // Re-run effect if lifecycleOwner or listener instance changes
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> onAppear()
                    Lifecycle.Event.ON_STOP -> onDisappear()
                    else -> Unit // Do nothing for other events
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}
