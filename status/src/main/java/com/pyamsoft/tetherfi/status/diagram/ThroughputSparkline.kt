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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.status.R
import kotlinx.coroutines.delay

private const val SAMPLE_INTERVAL_MS = 2_000L
private const val MAX_SAMPLES = 30

/**
 * Live throughput, derived by periodic sampling of the EXISTING per-client
 * data counter (each client's accumulated transfer total). Read-only: the
 * sampler never touches the proxy, the broadcast, or the service.
 *
 * The counter resets when sharing stops (the client list is cleared on
 * stop), so the total is honestly per-session — labelled as such.
 */
internal data class ThroughputState(
    val rates: List<Float>,
    val currentRateBytesPerSec: Float,
    val totalBytes: Long,
)

@Composable
internal fun rememberThroughput(
    clients: List<TetherClient>,
    isRunning: Boolean,
): ThroughputState {
  val rates = remember { mutableStateListOf<Float>() }
  var currentRate by remember { mutableFloatStateOf(0F) }
  var total by remember { mutableLongStateOf(0L) }

  // Latest client totals, read by the sampling loop.
  val latestTotal by rememberUpdatedState(
      clients.sumOf { it.transferToInternet.bytes + it.transferFromInternet.bytes }.toFloat(),
  )

  LaunchedEffect(isRunning) {
    if (!isRunning) {
      rates.clear()
      currentRate = 0F
      total = 0L
      return@LaunchedEffect
    }

    var previous: Float? = null
    while (true) {
      delay(SAMPLE_INTERVAL_MS)
      val now = latestTotal
      total = now.toLong()
      val prev = previous
      previous = now
      if (prev != null) {
        val rate = ((now - prev) / (SAMPLE_INTERVAL_MS / 1_000F)).coerceAtLeast(0F)
        currentRate = rate
        rates.add(rate)
        while (rates.size > MAX_SAMPLES) {
          rates.removeAt(0)
        }
      }
    }
  }

  return ThroughputState(
      rates = rates.toList(),
      currentRateBytesPerSec = currentRate,
      totalBytes = total,
  )
}

internal fun formatBytes(bytes: Long): String {
  if (bytes < 1_024L) {
    return "$bytes bytes"
  }
  val units = arrayOf("KB", "MB", "GB", "TB")
  var value = bytes.toDouble()
  var unitIndex = -1
  while (value >= 1_024.0 && unitIndex < units.lastIndex) {
    value /= 1_024.0
    unitIndex++
  }
  return "%.1f %s".format(value, units[unitIndex])
}

@Composable
internal fun ThroughputSparkline(
    modifier: Modifier = Modifier,
    throughput: ThroughputState,
    isRunning: Boolean,
) {
  val liveColor = MaterialTheme.colorScheme.tertiary
  val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

  val hasTraffic = isRunning && throughput.rates.any { it > 0F }
  val maxRate = (throughput.rates.maxOrNull() ?: 0F).coerceAtLeast(1F)

  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Canvas(
        modifier = Modifier.weight(1F).height(48.dp),
        onDraw = {
          val width = size.width
          val height = size.height

          if (!hasTraffic) {
            // Flat line: idle or off.
            drawLine(
                color = mutedColor,
                start = Offset(0F, height - 2.dp.toPx()),
                end = Offset(width, height - 2.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
            return@Canvas
          }

          val points =
              throughput.rates.mapIndexed { index, rate ->
                val x =
                    if (throughput.rates.size <= 1) {
                      width
                    } else {
                      index.toFloat() / (throughput.rates.size - 1) * width
                    }
                val y = height - 2.dp.toPx() - (rate / maxRate) * (height - 8.dp.toPx())
                Offset(x, y)
              }

          val fillPath =
              Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
              }
          drawPath(
              path = fillPath,
              color = liveColor.copy(alpha = 0.12F),
          )

          val linePath =
              Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
              }
          drawPath(
              path = linePath,
              color = liveColor,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
          )
        },
    )

    Column(
        modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        horizontalAlignment = Alignment.End,
    ) {
      Text(
          text =
              if (isRunning) {
                "${formatBytes(throughput.currentRateBytesPerSec.toLong())}/s"
              } else {
                "0.0 MB/s"
              },
          style =
              MaterialTheme.typography.titleMedium.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.W700,
                  color = if (hasTraffic) liveColor else mutedColor,
              ),
      )
      Text(
          text =
              "${formatBytes(throughput.totalBytes)} ${stringResource(R.string.diagram_shared_session)}",
          style =
              MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
              ),
          color = mutedColor,
      )
    }
  }
}
