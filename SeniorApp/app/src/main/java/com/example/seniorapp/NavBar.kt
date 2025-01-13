package com.example.seniorapp

import AnimatedIcon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavBar(
    currentTime: String,
    onNavigateToSettings: () -> Unit,
    isDraggingLocked: Boolean,
    isDeleting: Boolean,
    iconSize: Int = 48,
    isScrolled: Boolean,
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onLockToggle: () -> Unit,
    onDeleteToggle: () -> Unit)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 35.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTime,
            fontSize = 55.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.animateContentSize()
        )

        AnimatedIcon(
            imageVector = if (isDraggingLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
            contentDescription = if (isDraggingLocked) "Unlock" else "Lock",
            onClick = { onLockToggle() },
            modifier = Modifier.size(iconSize.dp)
        )
        AnimatedIcon(
            imageVector = if (isDeleting) Icons.Filled.Delete else Icons.Filled.DeleteOutline,
            contentDescription = "Delete",
            onClick = { onDeleteToggle() },
            modifier = Modifier.size(iconSize.dp)
        )
        AnimatedIcon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            onClick = { onNavigateToSettings() },
            modifier = Modifier.size(iconSize.dp)
        )
    }

    AnimatedVisibility(visible = !isScrolled) {
        TextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = { Text(text = stringResource(id = R.string.search)) },
            modifier = Modifier.fillMaxWidth().padding(top=8.dp,bottom=8.dp, start=15.dp, end=15.dp),
            singleLine = true
        )
    }
}
