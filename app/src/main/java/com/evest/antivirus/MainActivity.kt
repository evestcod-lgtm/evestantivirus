package com.evest.antivirus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.evest.antivirus.data.SettingsRepository
import com.evest.antivirus.notifications.NotificationHelper
import com.evest.antivirus.ui.navigation.EvestNavGraph
import com.evest.antivirus.ui.navigation.Routes
import com.evest.antivirus.ui.theme.EvestAntivirusTheme
import com.evest.antivirus.ui.viewmodel.EvestViewModelFactory
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannels(this)

        val deepLinkRoute = intent?.getStringExtra("route")

        setContent {
            EvestAntivirusTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EvestRoot(deepLinkRoute)
                }
            }
        }
    }
}

@Composable
private fun EvestRoot(deepLinkRoute: String?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { SettingsRepository(context) }
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val firstLaunchDone = settings.firstLaunchDone.first()
        startDestination = when {
            deepLinkRoute != null -> deepLinkRoute
            !firstLaunchDone -> Routes.SPLASH
            else -> Routes.HOME
        }
        if (!firstLaunchDone) {
            settings.setFirstLaunchDone(true)
        }
    }

    val resolvedStart = startDestination
    if (resolvedStart != null) {
        val navController = rememberNavController()
        val factory = remember {
            EvestViewModelFactory(context.applicationContext as android.app.Application)
        }
        EvestNavGraph(navController = navController, startDestination = resolvedStart, factory = factory)
    }
}
