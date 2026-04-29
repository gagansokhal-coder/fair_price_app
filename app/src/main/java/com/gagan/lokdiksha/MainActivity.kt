package com.gagan.lokdiksha

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.gagan.lokdiksha.ui.theme.FairPriceTheme
import com.gagan.lokdiksha.utils.LocaleManager

import com.gagan.lokdiksha.network.RetrofitClient

/**
 * Main entry point — Single Activity with Compose.
 * Edge-to-edge rendering for the premium "Dignified Anchor" design.
 * Locale wrapping ensures all getString() calls respect user's language choice.
 */
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(this)
        enableEdgeToEdge()
        setContent {
            FairPriceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    FairPriceApp()
                }
            }
        }
    }
}
