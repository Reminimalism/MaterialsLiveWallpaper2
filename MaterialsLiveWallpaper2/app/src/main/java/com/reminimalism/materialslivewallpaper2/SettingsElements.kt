package com.reminimalism.materialslivewallpaper2

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SettingsHeaderView(text: String)
{
    Row(
        Modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsToggleView(preferences: SharedPreferences?, key: String, title: String,
                       subtitleOn: String = "", subtitleOff: String = "",
                       defaultValue: Boolean = false)
{
    val setting = remember {
        mutableStateOf(preferences?.getBoolean(key, defaultValue) ?: defaultValue)
    }

    SettingsItemView(
        title,
        if (setting.value) subtitleOn else subtitleOff,
        content = {
            Switch(
                checked = setting.value,
                onCheckedChange = {
                    setting.value = it
                    preferences?.edit { putBoolean(key, it) }
                }
            )
        },
        onReset = if (setting.value == defaultValue) null else ({
            setting.value = defaultValue
            preferences?.edit { putBoolean(key, defaultValue) }
        })
    )
}

@Composable
fun SettingsOptionView(preferences: SharedPreferences?, key: String, title: String,
                       defaultOption: String,
                       options: Map<String, String>)
{
    val useDialogForMenu = true

    if (!options.containsKey(defaultOption))
    {
        SettingsItemView(
            title,
            "Error: Default option not found!"
        )
        return
    }

    val expanded = remember {
        mutableStateOf(false)
    }
    val setting = remember {
        var value = preferences?.getString(key, defaultOption) ?: defaultOption
        if (!options.containsKey(value))
            value = defaultOption
        mutableStateOf(value)
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart))
    {
        SettingsItemView(
            title,
            options[setting.value] ?: "",
            onClick = {
                expanded.value = true
            },
            onReset = if (setting.value == defaultOption) null else ({
                setting.value = defaultOption
                preferences?.edit { putString(key, defaultOption) }
            })
        )

        if (!useDialogForMenu)
        {
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            )
            {
                for (option in options)
                {
                    DropdownMenuItem(
                        text = { Text(option.value) },
                        onClick = {
                            setting.value = option.key
                            expanded.value = false
                            preferences?.edit { putString(key, option.key) }
                        }
                    )
                }
            }
        }
    }

    if (useDialogForMenu && expanded.value)
    {
        Dialog(
            onDismissRequest = { expanded.value = false }
        )
        {
            Box(
                modifier = Modifier.padding(32.dp, 48.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    )
                    {
                        for (option in options)
                        {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        setting.value = option.key
                                        expanded.value = false
                                        preferences?.edit { putString(key, option.key) }
                                    }
                                    .padding(8.dp)
                            )
                            {
                                Text(
                                    text = option.value,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSliderView(preferences: SharedPreferences?, key: String, title: String,
                       subtitle: String = "", defaultValue: Float = 0f,
                       rangeMin: Float = 0f, rangeMax: Float = 1f,
                       steps: Int = 100)
{
    val setting = remember {
        mutableFloatStateOf(preferences?.getFloat(key, defaultValue) ?: defaultValue)
    }
    val getSliderValue = {
        val value = (setting.floatValue - rangeMin) / (rangeMax - rangeMin)
        max(0f, min(1f, value))
    }
    val sliderValue = remember {
        mutableFloatStateOf(getSliderValue())
    }

    SettingsItemView(
        title,
        subtitle,
        content = {
            Text(setting.floatValue.toString(), fontSize = 24.sp)
        },
        bottomContent = {
            Row(
                modifier = Modifier
                    .padding(24.dp, 0.dp)
            )
            {
                Slider(
                    //value = setting.floatValue, // Inaccurate
                    //valueRange = rangeMin..rangeMax, // Inaccurate
                    value = sliderValue.floatValue,
                    // For some reason it also include 2 extra steps on 2 ends
                    steps = steps - 2,
                    onValueChange = {
                        //setting.floatValue = it // Inaccurate
                        sliderValue.floatValue = it
                        setting.floatValue = rangeMin +
                                ((it * (steps - 1)).roundToInt() / (steps - 1).toFloat()) *
                                (rangeMax - rangeMin)
                    },
                    onValueChangeFinished = {
                        preferences?.edit { putFloat(key, setting.floatValue) }
                    },
                    modifier = Modifier.padding(8.dp, 0.dp)
                )
            }
        },
        onReset = if (setting.floatValue == defaultValue) null else ({
            setting.floatValue = defaultValue
            sliderValue.floatValue = getSliderValue()
            preferences?.edit { putFloat(key, defaultValue) }
        })
    )
}

@Composable
fun SettingsIntSliderView(preferences: SharedPreferences?, key: String, title: String,
                       subtitle: String = "", defaultValue: Int = 0,
                       rangeMin: Int = 0, rangeMax: Int = 100)
{
    val setting = remember {
        mutableIntStateOf(preferences?.getInt(key, defaultValue) ?: defaultValue)
    }

    SettingsItemView(
        title,
        subtitle,
        content = {
            Text(setting.intValue.toString(), fontSize = 24.sp)
        },
        bottomContent = {
            Row(
                modifier = Modifier
                    .padding(24.dp, 0.dp)
            )
            {
                Slider(
                    value = setting.intValue.toFloat(),
                    valueRange = rangeMin.toFloat()..rangeMax.toFloat(),
                    // For some reason it also include 2 extra steps on 2 ends
                    steps = rangeMax - rangeMin + 1 - 2,
                    onValueChange = {
                        setting.intValue = it.roundToInt()
                    },
                    onValueChangeFinished = {
                        preferences?.edit { putInt(key, setting.intValue) }
                    },
                    modifier = Modifier.padding(8.dp, 0.dp)
                )
            }
        },
        onReset = if (setting.intValue == defaultValue) null else ({
            setting.intValue = defaultValue
            preferences?.edit { putInt(key, defaultValue) }
        })
    )
}

@Composable
fun SettingsItemView(title: String, subtitle: String = "",
                     content: @Composable () -> Unit = {},
                     bottomContent: @Composable () -> Unit = {},
                     onClick: () -> Unit = {},
                     onReset: (() -> Unit)? = null)
{
    Column(
        Modifier
            .fillMaxSize()
            .padding(0.dp)
    )
    {
        Row(
            Modifier
                .fillMaxSize()
                .padding(0.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Column(
                Modifier
                    .padding(16.dp)
                    .weight(1f)
            )
            {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 24.sp
                )
                if (subtitle.isNotEmpty())
                    Text(
                        subtitle,
                        fontSize = 14.sp
                    )
            }

            if (onReset != null)
            {
                Box(
                    Modifier
                        .padding(16.dp)
                        .clickable(onClick = onReset)
                )
                {
                    Text(" ⟲ ", fontSize = 16.sp)
                }
            }

            Box(
                Modifier.padding(16.dp)
            )
            {
                content()
            }
        }

        bottomContent()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsElementsActivityPreview()
{
    SettingsActivityPreview()
}