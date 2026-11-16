/*
 * Copyright 2026 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tetherfi

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.CheckResult
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.pyamsoft.pydroid.theme.PYDroidTheme
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.pydroid.ui.haptics.rememberHapticManager
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.uri.LocalExternalUriHandler
import com.pyamsoft.pydroid.ui.uri.rememberExternalUriHandler
import com.pyamsoft.pydroid.util.AppDispatchers

/**
 * Carrier — the Backhaul look. Blueprint schematic + live waveform.
 * Six named tokens per mode, plain names. Dark is the same diagram
 * inverted. Green is the one accent (on / flowing / connected); red
 * is failures only, never decoration.
 */
object CarrierColors {
  object Light {
    /** Cool blueprint paper — the app background. */
    val paper = Color(0xFFE8EDF2)
    /** Text and line work. */
    val ink = Color(0xFF182633)
    /** The drawing — nodes, lines, headings. */
    val blue = Color(0xFF2E5D8A)
    /** The one accent: on, flowing, connected. */
    val green = Color(0xFF1E7D46)
    /** Failures only. */
    val red = Color(0xFFBA1A1A)
    /** Secondary labels and readouts. */
    val muted = Color(0xFF7C93A6)
    /** Cards and sheets, a step lighter than paper. */
    val surface = Color(0xFFF5F8FA)
  }

  object Dark {
    /** Deep blueprint — the same diagram inverted. */
    val paper = Color(0xFF0E1A24)
    val ink = Color(0xFFDCE6EE)
    val blue = Color(0xFF6FA3D0)
    val green = Color(0xFF4CC38A)
    val red = Color(0xFFFF8A80)
    val muted = Color(0xFF6B8296)
    val surface = Color(0xFF16242F)
  }
}

@Composable
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
private fun rememberCanUseDynamic(isMaterialYou: Boolean): Boolean {
  return remember(isMaterialYou) { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isMaterialYou }
}

@Composable
@CheckResult
private fun themeColors(
    activity: Activity,
    isDarkMode: Boolean,
    isMaterialYou: Boolean,
): ColorScheme {
  val canUseDynamic = rememberCanUseDynamic(isMaterialYou)

  return remember(
      activity,
      canUseDynamic,
      isDarkMode,
  ) {
    if (isDarkMode) {

      if (canUseDynamic) {
        dynamicDarkColorScheme(activity)
      } else {
        // Carrier (dark): the same diagram inverted. Container tints
        // are the token darkened toward paper.
        val paper = CarrierColors.Dark.paper
        val ink = CarrierColors.Dark.ink
        val blue = CarrierColors.Dark.blue
        val green = CarrierColors.Dark.green
        val red = CarrierColors.Dark.red
        val muted = CarrierColors.Dark.muted
        val primary = blue
        val onPrimary = paper
        val primaryContainer = Color(0xFF23486B)
        val onPrimaryContainer = ink
        val secondary = muted
        val onSecondary = paper
        val secondaryContainer = Color(0xFF2E4256)
        val onSecondaryContainer = ink
        val tertiary = green
        val onTertiary = paper
        val tertiaryContainer = Color(0xFF1B4D33)
        val onTertiaryContainer = ink
        val error = red
        val errorContainer = Color(0xFF93000A)
        val onError = Color(0xFF690005)
        val onErrorContainer = Color(0xFFFFDAD6)
        val background = paper
        val onBackground = ink
        val surface = CarrierColors.Dark.surface
        val onSurface = ink
        val surfaceVariant = Color(0xFF24384A)
        val onSurfaceVariant = Color(0xFFA7BCCB)
        val outline = muted
        val inverseOnSurface = paper
        val inverseSurface = ink
        val inversePrimary = Color(0xFF2E5D8A)
        val surfaceTint = blue
        val outlineVariant = Color(0xFF2A3D4D)
        val scrim = Color(0xFF000000)

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            errorContainer = errorContainer,
            onError = onError,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = surfaceTint,
            scrim = scrim,
        )
      }
    } else {
      if (canUseDynamic) {
        dynamicLightColorScheme(activity)
      } else {
        // Carrier (light): cool blueprint paper, ink line work, blue
        // draws, green is the one accent, red is failures only.
        // Container tints are the token at 15% over paper.
        val paper = CarrierColors.Light.paper
        val ink = CarrierColors.Light.ink
        val blue = CarrierColors.Light.blue
        val green = CarrierColors.Light.green
        val red = CarrierColors.Light.red
        val muted = CarrierColors.Light.muted
        val primary = blue
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFCCD7E2)
        val onPrimaryContainer = ink
        val secondary = muted
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFD8DFE7)
        val onSecondaryContainer = ink
        val tertiary = green
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFCADCD8)
        val onTertiaryContainer = ink
        val error = red
        val errorContainer = Color(0xFFFFDAD6)
        val onError = Color(0xFFFFFFFF)
        val onErrorContainer = Color(0xFF410002)
        val background = paper
        val onBackground = ink
        val surface = CarrierColors.Light.surface
        val onSurface = ink
        val surfaceVariant = Color(0xFFD9E2EA)
        val onSurfaceVariant = Color(0xFF45566A)
        val outline = muted
        val inverseOnSurface = paper
        val inverseSurface = ink
        val inversePrimary = Color(0xFF8FB8DE)
        val surfaceTint = blue
        val outlineVariant = Color(0xFFC3D0DB)
        val scrim = Color(0xFF000000)

        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            errorContainer = errorContainer,
            onError = onError,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = surfaceTint,
            scrim = scrim,
        )
      }
    }
  }
}

@Composable
fun ComponentActivity.TFTheme(
    dispatchers: AppDispatchers,
    theme: Theming.Mode,
    isMaterialYou: Boolean,
    content: @Composable () -> Unit,
) {
  val self = this

  val hapticManager =
      rememberHapticManager(
          dispatchers = dispatchers,
      )
  val isDarkMode = theme.getSystemDarkMode()
  val uriHandler = rememberExternalUriHandler()

  PYDroidTheme(
      colorScheme = themeColors(self, isDarkMode, isMaterialYou),
  ) {
    CompositionLocalProvider(
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        LocalContentColor provides MaterialTheme.colorScheme.onBackground,

        // We provide the local haptic manager since PYDroid makes it optional
        LocalHapticManager provides hapticManager,

        // We provide external URI handler
        LocalExternalUriHandler provides uriHandler,

        // And the content, finally
        content = content,
    )
  }
}

@Composable
@CheckResult
fun Theming.Mode.getSystemDarkMode(): Boolean {
  val self = this
  val isDarkMode =
      remember(self) {
        when (self) {
          Theming.Mode.LIGHT -> false
          Theming.Mode.DARK -> true
          Theming.Mode.SYSTEM -> null
        }
      }

  return isDarkMode ?: isSystemInDarkTheme()
}
