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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit

@Composable
fun SettingsHeaderView(text: String)
{
    Row(
        Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp),
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
        }
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
        mutableStateOf(preferences?.getString(key, defaultOption) ?: defaultOption)
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart))
    {
        SettingsItemView(
            title,
            options[setting.value] ?: "",
            onClick = {
                expanded.value = true
            }
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
                modifier = Modifier.padding(16.dp, 32.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                )
                {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
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
                            )
                            {
                                Text(
                                    text = option.value,
                                    fontSize = 24.sp,
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
fun SettingsItemView(title: String, subtitle: String = "",
                     content: @Composable () -> Unit = {},
                     onClick: () -> Unit = {})
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
                    subtitle
                )
        }

        Box(
            Modifier.padding(16.dp)
        )
        {
            content()
        }
    }
}