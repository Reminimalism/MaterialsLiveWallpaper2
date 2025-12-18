package com.reminimalism.materialslivewallpaper2

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminimalism.materialslivewallpaper2.ui.theme.MaterialsLiveWallpaper2Theme

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialsLiveWallpaper2Theme {
                Scaffold(modifier = Modifier.fillMaxSize())
                { innerPadding ->
                    MainActivityView(
                        this,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainActivityView(context: Context? = null, modifier: Modifier = Modifier)
{
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center)
    {
        Column(modifier.padding(10.dp, 10.dp), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Text(
                    text = "Materials",
                    fontSize = 30.sp,
                    color = Color(0x00, 0xDC, 0xC8)
                )
                Text(
                    text = "Live",
                    fontSize = 30.sp,
                    color = Color(0xFF, 0x00, 0x64)
                )
                Text(
                    text = "Wallpaper",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "2",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (context == null)
                        return@Button
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    intent.putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(context, MaterialsWallpaperService::class.java)
                    )
                    context.startActivity(intent);
                },
                content = {
                    Text(
                        text = "Set Wallpaper"
                    )
                }
            )

            Button(
                onClick = {
                    context?.startActivity(
                        Intent(context, SettingsActivity::class.java)
                    )
                },
                content = {
                    Text(
                        text = "Settings"
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview()
{
    MaterialsLiveWallpaper2Theme {
        MainActivityView()
    }
}