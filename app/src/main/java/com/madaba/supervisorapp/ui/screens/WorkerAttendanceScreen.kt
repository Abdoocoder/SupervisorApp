// FILE: app/src/main/java/com/madaba/supervisorapp/ui/screens/WorkerAttendanceScreen.kt
package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun WorkerAttendanceScreen(workerId: String, onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Attendance for worker: $workerId")
    }
}