package com.reminimalism.materialslivewallpaper2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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

const val MATERIAL = "materialTemplate"
const val MATERIAL_DEFAULT = "default"
const val MATERIAL_OPTION_DEFAULT = "default"
const val MATERIAL_OPTION_BRUSHED_GOLD = "brushed_gold"
val MATERIAL_OPTIONS = mapOf(
    Pair(MATERIAL_OPTION_DEFAULT, "Gold Tiles"),
    Pair(MATERIAL_OPTION_BRUSHED_GOLD, "Faded Brushed Gold")
)
const val ROTATION_SMOOTHING = "smoothRotation"
const val ROTATION_SMOOTHING_DEFAULT = true
const val EXPOSURE = "exposure"
const val EXPOSURE_DEFAULT = 1f
const val ANISOTROPY_SAMPLES = "anisotropy_samples"
const val ANISOTROPY_SAMPLES_DEFAULT = 4

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
                val versionName = context?.let { context ->
                    context.packageManager.getPackageInfo(
                        context.packageName, 0
                    ).versionName
                } ?: "UNKNOWN"

                SettingsHeaderView("Material")

                SettingsOptionView(
                    preferences, MATERIAL,
                    "Material", MATERIAL_DEFAULT,
                    MATERIAL_OPTIONS
                )

                SettingsItemView(
                    "More Options...",
                    "Will be added in the future."
                        + " This is a preview."
                )

                SettingsHeaderView("General")

                SettingsToggleView(
                    preferences, ROTATION_SMOOTHING, "Rotation Smoothing",
                    "Smoothes out the rotation sensor data."
                            + " May add minimal latency but does not look choppy."
                            + " This is recommended to be on for most cases.",
                    "Uses raw sensor rotation data."
                            + " Reduces latency a little bit but may look choppy."
                            + " This is recommended to be on for most cases.",
                    ROTATION_SMOOTHING_DEFAULT
                )

                SettingsSliderView(
                    preferences, EXPOSURE,
                    "Exposure", "The brightness",
                    EXPOSURE_DEFAULT,
                    0f, 2f,
                    100
                )

                SettingsHeaderView("Performance")

                SettingsIntSliderView(
                    preferences, ANISOTROPY_SAMPLES,
                    "Anisotropy Samples",
                    "The number of samples to render brushed materials."
                            + " More samples results in smoother look"
                            + " but consumes more battery"
                            + " and may reduce frame rates."
                            + " It's recommended to keep this"
                            + " as low as possible.",
                    ANISOTROPY_SAMPLES_DEFAULT,
                    1, 32
                )

                SettingsHeaderView("More")

                SettingsItemView(
                    "App Version",
                    versionName
                )

                // TODO: Privacy Policy

                SettingsHeaderView("FUTURE")

                SettingsItemView(
                    "Other options coming soon...",
                    "This is an alpha preview version."
                            + " Customization options will be added"
                            + " in the future updates."
                )

                //val options1 = buildMap {
                //    for (i in 1..20)
                //    {
                //        put(i.toString(), "Item $i")
                //    }
                //}
                //val options2 = buildMap {
                //    for (i in 1..10)
                //    {
                //        put(i.toString(), "Item $i")
                //    }
                //}
                //SettingsOptionView(preferences, "options1", "Options Test 1",
                //    "1",
                //    options1)
                //SettingsOptionView(preferences, "options2", "Options Test 2",
                //    "1",
                //    options2)
                //SettingsToggleView(preferences, "toggle", "Toggle Test")
                //for (i in 1..10)
                //{
                //    SettingsToggleView(preferences, "toggle$i", "Toggle Test $i",
                //        "On ".repeat(i * 2), "Off ".repeat(i * 2))
                //}
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