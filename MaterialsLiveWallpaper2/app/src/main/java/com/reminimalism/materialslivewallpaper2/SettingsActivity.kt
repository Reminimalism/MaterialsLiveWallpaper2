package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminimalism.materialslivewallpaper2.ui.theme.MaterialsLiveWallpaper2Theme

class SettingsActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialsLiveWallpaper2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsActivityView(
                        modifier = Modifier.padding(innerPadding),
                        context = this
                    )
                }
            }
        }
    }
}

fun getAppPreferences(context: Context): SharedPreferences
{
    return context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
}

@Composable
fun SettingsActivityView(modifier: Modifier = Modifier, context: Context? = null)
{
    val preferences = if (context == null) null else getAppPreferences(context)

    Box(modifier = modifier.fillMaxSize())
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
        )
        {
            // Activity Header
            Text(
                text = "Settings",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
            )

            // Settings Scroll View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(0.dp, 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                SettingsHeaderView("General")

                val options = buildMap {
                    for (i in 1..20)
                    {
                        put(i.toString(), "Item $i")
                    }
                }

                SettingsOptionView(preferences, "options", "Options Test",
                    "1",
                    options)

                SettingsToggleView(preferences, "toggle", "Toggle Test")
                for (i in 1..10)
                {
                    SettingsToggleView(preferences, "toggle$i", "Toggle Test $i",
                        "On ".repeat(i * 2), "Off ".repeat(i * 2))
                }
            }
        }
    }
}

// Some potentially useful resource:
// https://stackoverflow.com/questions/68718655/building-a-preference-screen-with-android-jetpack-compose

@Preview(showBackground = true)
@Composable
fun SettingsActivityPreview()
{
    MaterialsLiveWallpaper2Theme {
        SettingsActivityView()
    }
}