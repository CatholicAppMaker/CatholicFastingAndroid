package com.kevpierce.catholicfastingapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kevpierce.catholicfasting.core.model.AppDeepLinks
import com.kevpierce.catholicfasting.core.ui.CatholicFastingTheme
import com.kevpierce.catholicfastingapp.ui.CatholicFastingApp

class MainActivity : ComponentActivity() {
    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLink = intent?.initialDeepLink()
        setContent {
            CatholicFastingTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CatholicFastingApp(initialDeepLink = deepLink)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink = intent.initialDeepLink()
    }

    private fun Intent.initialDeepLink(): String? = dataString ?: getStringExtra(AppDeepLinks.EXTRA_INITIAL_DEEP_LINK)
}
