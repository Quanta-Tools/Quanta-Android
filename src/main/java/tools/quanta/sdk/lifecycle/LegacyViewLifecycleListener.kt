package tools.quanta.sdk.lifecycle

import android.view.View
import java.util.WeakHashMap

/**
 * Listens for view appearance (attach) and disappearance (detach) events for legacy Android Views.
 *
 * @param onAppear Callback invoked when the observed view is attached to a window.
 * @param onDisappear Callback invoked when the observed view is detached from a window.
 */
class LegacyViewLifecycleListener(
    private val onAppear: (View) -> Unit,
    private val onDisappear: (View) -> Unit
) {

    private val listeners = WeakHashMap<View, View.OnAttachStateChangeListener>()

    /**
     * Starts observing the given view for attach and detach events.
     * If the view is already attached when this method is called, the onAppear callback will be invoked immediately.
     *
     * @param view The View to observe.
     */
    fun observeView(view: View) {
        if (listeners.containsKey(view)) return // Already observing this view

        val listener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAppear(v)
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDisappear(v)
            }
        }
        view.addOnAttachStateChangeListener(listener)
        listeners[view] = listener

        // If the view is already attached, trigger onAppear immediately.
        if (view.isAttachedToWindow) {
            onAppear(view)
        }
    }

    /**
     * Stops observing the given view for attach and detach events.
     *
     * @param view The View to stop observing.
     */
    fun stopObservingView(view: View) {
        listeners.remove(view)?.let {
            view.removeOnAttachStateChangeListener(it)
        }
    }

    /**
     * Stops observing all currently observed views.
     */
    fun stopObservingAllViews() {
        listeners.forEach { (view, listener) ->
            view.removeOnAttachStateChangeListener(listener)
        }
        listeners.clear()
    }
}
