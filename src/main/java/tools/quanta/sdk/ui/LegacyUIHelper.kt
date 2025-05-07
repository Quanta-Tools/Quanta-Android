package tools.quanta.sdk.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

object LegacyUIHelper {

    /**
     * Example helper: Recursively find all views of a specific type in a ViewGroup.
     */
    fun <T : View> findViewsOfType(root: ViewGroup, type: Class<T>): List<T> {
        val views = mutableListOf<T>()
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (type.isInstance(child)) {
                views.add(type.cast(child))
            }
            if (child is ViewGroup) {
                views.addAll(findViewsOfType(child, type))
            }
        }
        return views
    }

    /**
     * Example helper: Get the root view of an Activity.
     */
    fun getRootView(activity: Activity): View? {
        return activity.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)
    }

    /**
     * Example helper: Get the root view of a Fragment.
     */
    fun getRootView(fragment: Fragment): View? {
        return fragment.view
    }

    // Add more legacy View system specific UI helper functions here
}
