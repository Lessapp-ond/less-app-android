package com.lessapp.less

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.lessapp.less.service.AnalyticsService
import com.lessapp.less.ui.screens.MainScreen
import com.lessapp.less.ui.theme.LESSTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load any pending analytics from previous session
        lifecycleScope.launch {
            AnalyticsService.loadPending(applicationContext)
            AnalyticsService.flush(applicationContext)
        }

        setContent {
            LESSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(activity = this)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Flush analytics when app goes to background
        lifecycleScope.launch {
            AnalyticsService.flush(applicationContext)
        }
    }
}
