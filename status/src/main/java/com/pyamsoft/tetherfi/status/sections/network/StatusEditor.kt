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

package com.pyamsoft.tetherfi.status.sections.network

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Carrier schematic-sheet editor: a small-caps label above a thin
 * bordered box holding the value in monospace — no Material text
 * field. Same settings, same stored values; presentation only.
 */
@Composable
internal fun StatusEditor(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    value: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
  Column(
      modifier = modifier.alpha(if (enabled) 1F else 0.38F),
  ) {
    Text(
        text = title.uppercase(),
        style =
            MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    )

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        if (leadingIcon != null) {
          Box(
              modifier = Modifier.padding(end = 8.dp),
          ) {
            leadingIcon()
          }
        }

        BasicTextField(
            modifier = Modifier.weight(1F),
            value = value,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            textStyle =
                LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
        )

        if (trailingIcon != null) {
          Box(
              modifier = Modifier.padding(start = 8.dp),
          ) {
            trailingIcon()
          }
        }
      }
    }
  }
}
