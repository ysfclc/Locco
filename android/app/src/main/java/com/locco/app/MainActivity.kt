package com.locco.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nativeStatus = runCatching {
            "Rust ABI ${NativeBridge.nativeAbiVersion()} · ping ${NativeBridge.nativePing()}"
        }.getOrElse { "Native core not linked: ${it.javaClass.simpleName}" }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Locco", style = MaterialTheme.typography.headlineLarge)
                        Text("Foundation shell", style = MaterialTheme.typography.titleMedium)
                        Text(nativeStatus, modifier = Modifier.padding(top = 16.dp))
                        Text("No product features are implemented in Phase 0.")
                    }
                }
            }
        }
    }
}
