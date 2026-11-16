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

package com.pyamsoft.tetherfi.status.diagram

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines

/**
 * The Carrier schematic sheet: one bordered panel whose sections are divided
 * by hairline rules, not floating cards. The status screen is a single sheet;
 * the diagram, the readouts and the config sections all live inside it.
 */
@Composable
internal fun DiagramSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.large,
      border =
          BorderStroke(
              width = 1.5.dp,
              color = MaterialTheme.colorScheme.outlineVariant,
          ),
      color = MaterialTheme.colorScheme.surface,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        content = content,
    )
  }
}

@Composable
internal fun SheetSectionHeader(
    modifier: Modifier = Modifier,
    text: String,
) {
  Text(
      modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
      text = text,
      style =
          MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.W700,
              letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
              color = MaterialTheme.colorScheme.primary,
          ),
  )
}

@Composable
internal fun SheetDivider(
    modifier: Modifier = Modifier,
) {
  androidx.compose.material3.HorizontalDivider(
      modifier = modifier.padding(vertical = MaterialTheme.keylines.content),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant,
  )
}

/**
 * A key/value readout row. Keys are plain labels; values are machine
 * readouts, so they are always monospace.
 */
@Composable
internal fun SheetReadoutRow(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    valueColor: Color = Color.Unspecified,
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.weight(1F),
        text = key,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = value,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W400,
            ),
        color = valueColor,
    )
  }
}
