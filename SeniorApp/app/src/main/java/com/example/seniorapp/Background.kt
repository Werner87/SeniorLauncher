package com.example.seniorapp

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seniorapp.ui.theme.Typography

@Composable
fun BackgroundSection(
    backgroundColor: Color,
    isImageBackground: Boolean,
    backgroundImage: Bitmap?,
    onPickImage: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Choose background image button
        OutlinedButton(
            onClick = { onPickImage() },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(1, 1, 3, 47))
        ) {
            Text(text = stringResource(id = R.string.choose_background_image), style = Typography.labelSmall, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(text = stringResource(id = R.string.background_color), style = Typography.bodyLarge, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            contentPadding = PaddingValues(15.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                listOf(
                    Color.White, Color.Gray, Color(176, 224, 230), Color(199, 21, 133),
                    Color(255, 99, 71), Color(255, 215, 0), Color(1, 182, 155, 255),
                    Color(9, 23, 143, 255), Color(56, 129, 3, 255), Color(139, 0, 0, 255),
                    Color(103, 58, 183, 255), Color(189, 0, 0, 255)
                )
            ) { color ->
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(color)
                        .clickable { onColorSelected(color) }
                        .padding(8.dp)
                )
            }
        }
    }
}
