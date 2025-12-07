package com.cs407.lineup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.cs407.lineup.data.ApiKeyProvider
import com.cs407.lineup.ui.theme.LineupTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch { ApiKeyProvider.initialize() }

        enableEdgeToEdge()
        setContent {
            LineupTheme {
                LineupApp()
            }
        }
    }
}