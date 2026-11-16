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

package com.pyamsoft.tetherfi.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyamsoft.tetherfi.server.broadcast.wifidirect.WiFiDirectError
import com.pyamsoft.tetherfi.server.status.RunningStatus

/**
 * The Carrier title block: the app name in letterspaced monospace, a
 * state pill, and a thin rule — no filled surface, no Material top bar.
 */
@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    appName: String,
    overallStatus: RunningStatus,
) {
  val colors = MaterialTheme.colorScheme

  val (pillText, pillColor) =
      when (overallStatus) {
        is RunningStatus.Running -> "● ON" to colors.tertiary
        is RunningStatus.Error -> "● ERROR" to colors.error
        is RunningStatus.Starting -> "● STARTING" to colors.primary
        is RunningStatus.Stopping -> "● STOPPING" to colors.primary
        is RunningStatus.NotRunning -> "○ OFF" to colors.onSurfaceVariant
      }

  Column(
      modifier = modifier.statusBarsPadding(),
  ) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = appName.uppercase(),
          style =
              MaterialTheme.typography.titleLarge.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 4.sp,
              ),
          color = colors.onSurface,
      )

      Spacer(
          modifier = Modifier.weight(1F),
      )

      Box(
          modifier =
              Modifier.border(
                      width = 1.dp,
                      color = pillColor,
                      shape = MaterialTheme.shapes.small,
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp),
      ) {
        Text(
            text = pillText,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            color = pillColor,
        )
      }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = colors.outlineVariant,
    )
  }
}

@Preview
@Composable
private fun PreviewMainTopBar() {
  MainTopBar(
      appName = "TEST",
      overallStatus = RunningStatus.Running,
  )
}

@Preview
@Composable
private fun PreviewMainTopBarOff() {
  MainTopBar(
      appName = "TEST",
      overallStatus = RunningStatus.NotRunning,
  )
}

@Preview
@Composable
private fun PreviewMainTopBarError() {
  MainTopBar(
      appName = "TEST",
      overallStatus =
          WiFiDirectError(
              WiFiDirectError.Reason.Error,
              IllegalStateException("TEST"),
          ),
  )
}
