
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
    var scale by remember { mutableStateOf(1f) }
    val scaleAnim by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 150),
        label = "scaleAnimation"
    )

    val coroutineScope = rememberCoroutineScope()

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier
            .size(40.dp)
            .scale(scaleAnim)
            .clickable {
                // Uruchamiamy animację w CoroutineScope
                coroutineScope.launch {
                    scale = 1.1f
                    delay(150) // Czekamy na zakończenie animacji powiększenia
                    scale = 1f // Powrót do oryginalnego rozmiaru
                }
                onClick()
            },
        tint = tint
    )
}
