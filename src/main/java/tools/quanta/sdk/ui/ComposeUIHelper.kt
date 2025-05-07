package tools.quanta.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

object ComposeUIHelper {

    /**
     * Example helper: Gets a string resource within a Composable function.
     */
    @Composable
    fun stringResource(id: Int): String {
        return LocalContext.current.getString(id)
    }

    /**
     * Example helper: Applies a conditional modifier.
     */
    @Composable
    fun Modifier.conditional(condition: Boolean, modifier: @Composable Modifier.() -> Modifier): Modifier {
        return if (condition) {
            then(modifier(Modifier))
        } else {
            this
        }
    }
    // Add more Jetpack Compose specific UI helper functions here
}
