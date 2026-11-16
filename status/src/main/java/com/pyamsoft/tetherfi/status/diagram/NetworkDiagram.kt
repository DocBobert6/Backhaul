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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.server.clients.key
import com.pyamsoft.tetherfi.server.status.RunningStatus
import com.pyamsoft.tetherfi.status.R
import kotlin.math.PI
import kotlin.math.sin

/**
 * The Carrier diagram: this phone as a node, a breaker switch on the line,
 * a live waveform while traffic flows, and one node per connected device.
 *
 * The breaker drives the SAME start/stop path as the old toggle button — it
 * is a presentation change, not a second source of truth for on/off.
 * Tapping a device node opens that device's details.
 */
@Composable
internal fun NetworkDiagram(
    modifier: Modifier = Modifier,
    hotspotStatus: RunningStatus,
    isButtonEnabled: Boolean,
    clients: List<TetherClient>,
    blocked: List<TetherClient>,
    isFlowing: Boolean,
    onToggleProxy: () -> Unit,
    onClientSelected: (TetherClient) -> Unit,
) {
  val density = LocalDensity.current
  val textMeasurer = rememberTextMeasurer()

  val isSharing = hotspotStatus is RunningStatus.Running
  val isOff = hotspotStatus is RunningStatus.NotRunning
  val isError = hotspotStatus is RunningStatus.Error
  val isTransitioning = !isSharing && !isOff && !isError

  val breakerLabel =
      when {
        isSharing -> stringResource(R.string.diagram_breaker_sharing)
        isOff -> stringResource(R.string.diagram_breaker_off)
        isError -> stringResource(R.string.status_error)
        hotspotStatus is RunningStatus.Starting -> stringResource(R.string.status_starting)
        else -> stringResource(R.string.status_stopping)
      }

  // One node per client; an empty diagram still draws one dimmed node so the
  // line has somewhere to go.
  val nodeCount = clients.size.coerceAtLeast(1)
  val diagramHeight = diagramHeight(nodeCount)

  var canvasSize by remember { mutableStateOf(IntSize.Zero) }

  // The wave phase advances one frame at a time, and ONLY while traffic is
  // flowing. The previous version used rememberInfiniteTransition, created
  // unconditionally at first composition — the animation clock stayed
  // armed even with the hotspot off, and the Robolectric startup gate
  // hung on it (run 43802686493 stuck 30+ min in the unit-test step).
  // A frame-driven phase schedules nothing at all while idle.
  var wavePhase by remember { mutableFloatStateOf(0F) }
  LaunchedEffect(isFlowing) {
    if (!isFlowing) {
      return@LaunchedEffect
    }
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dtSeconds = (nanos - lastNanos) / 1_000_000_000F
          // One full dash cycle per 1.1 seconds, matching the old tween.
          wavePhase = (wavePhase + dtSeconds / 1.1F) % 1F
        }
        lastNanos = nanos
      }
    }
  }

  val colors = MaterialTheme.colorScheme
  val wireColor = colors.primary
  val liveColor = colors.tertiary
  val mutedColor = colors.onSurfaceVariant
  val errorColor = colors.error
  val inkColor = colors.onSurface

  // Node labels resolve here, in the composable body — stringResource is
  // a @Composable call and cannot run inside the Canvas onDraw block.
  val phoneLabel = stringResource(R.string.diagram_phone)
  val thisDeviceLabel = stringResource(R.string.diagram_this_device)

  Canvas(
      modifier =
          modifier
              .fillMaxWidth()
              .height(diagramHeight)
              .onSizeChanged { canvasSize = it }
              .pointerInput(isButtonEnabled, clients, canvasSize) {
                detectTapGestures { offset ->
                  val layout = computeLayout(density, canvasSize, nodeCount) ?: return@detectTapGestures
                  if (isButtonEnabled &&
                      (offset - layout.breakerCenter).getDistance() <= layout.breakerTouchRadius) {
                    onToggleProxy()
                    return@detectTapGestures
                  }
                  clients.forEachIndexed { index, client ->
                    if (index < layout.deviceRects.size &&
                        layout.deviceRects[index].contains(offset)) {
                      onClientSelected(client)
                    }
                  }
                }
              },
      onDraw = {
        val layout =
            computeLayout(
                density,
                IntSize(size.width.toInt(), size.height.toInt()),
                nodeCount,
            )
                ?: return@Canvas
        val dimmed = isOff || isError

        drawWires(
            layout = layout,
            wireColor = if (dimmed) mutedColor else wireColor,
        )

        if (isSharing && isFlowing) {
          drawWave(
              layout = layout,
              waveColor = liveColor,
              isFlowing = isFlowing,
              phase = wavePhase,
          )
        }

        drawBreaker(
            layout = layout,
            label = breakerLabel,
            isSharing = isSharing,
            isError = isError,
            isTransitioning = isTransitioning,
            textMeasurer = textMeasurer,
            liveColor = liveColor,
            mutedColor = mutedColor,
            errorColor = errorColor,
            inkColor = inkColor,
        )

        drawPhoneNode(
            layout = layout,
            textMeasurer = textMeasurer,
            wireColor = wireColor,
            mutedColor = mutedColor,
            inkColor = inkColor,
            isDimmed = dimmed,
            phoneLabel = phoneLabel,
            thisDeviceLabel = thisDeviceLabel,
        )

        drawDeviceNodes(
            layout = layout,
            clients = clients,
            blocked = blocked,
            textMeasurer = textMeasurer,
            wireColor = wireColor,
            mutedColor = mutedColor,
            errorColor = errorColor,
            inkColor = inkColor,
            isDimmed = dimmed,
        )
      },
  )
}

private fun diagramHeight(nodeCount: Int): Dp {
  // 232dp fits up to three device nodes; taller diagrams grow per node.
  val nodesHeight = nodeCount * 46 + (nodeCount - 1) * 10 + 64
  return maxOf(232, nodesHeight).dp
}

private data class DiagramLayout(
    val phoneRect: Rect,
    val breakerCenter: Offset,
    val breakerRadius: Float,
    val breakerTouchRadius: Float,
    val deviceRects: List<Rect>,
    val junctionX: Float,
    val midY: Float,
)

private fun computeLayout(
    density: androidx.compose.ui.unit.Density,
    size: IntSize,
    nodeCount: Int,
): DiagramLayout? {
  if (size.width <= 0 || size.height <= 0) {
    return null
  }

  with(density) {
    val width = size.width.toFloat()
    val height = size.height.toFloat()
    val midY = height / 2F

    val phoneW = 64.dp.toPx()
    val phoneH = 88.dp.toPx()
    val phoneRect =
        Rect(
            left = 0F,
            top = midY - phoneH / 2F,
            right = phoneW,
            bottom = midY + phoneH / 2F,
        )

    val deviceW = 104.dp.toPx()
    val deviceH = 46.dp.toPx()
    val deviceGap = 10.dp.toPx()
    val deviceLeft = width - deviceW
    val devicesHeight = nodeCount * deviceH + (nodeCount - 1) * deviceGap
    val devicesTop = midY - devicesHeight / 2F
    val deviceRects =
        (0 until nodeCount).map { index ->
          val top = devicesTop + index * (deviceH + deviceGap)
          Rect(
              left = deviceLeft,
              top = top,
              right = width,
              bottom = top + deviceH,
          )
        }

    val breakerRadius = 22.dp.toPx()
    val breakerCenter =
        Offset(
            x = (phoneRect.right + deviceLeft) / 2F,
            y = midY,
        )

    return DiagramLayout(
        phoneRect = phoneRect,
        breakerCenter = breakerCenter,
        breakerRadius = breakerRadius,
        breakerTouchRadius = 32.dp.toPx(),
        deviceRects = deviceRects,
        junctionX = deviceLeft - 20.dp.toPx(),
        midY = midY,
    )
  }
}

private fun DrawScope.drawWires(
    layout: DiagramLayout,
    wireColor: Color,
) {
  val midY = layout.midY
  val breakerLeft = layout.breakerCenter.x - layout.breakerRadius
  val breakerRight = layout.breakerCenter.x + layout.breakerRadius

  // Phone -> breaker -> junction
  drawLine(
      color = wireColor,
      start = Offset(layout.phoneRect.right, midY),
      end = Offset(breakerLeft, midY),
      strokeWidth = 2.dp.toPx(),
      alpha = 0.55F,
  )
  drawLine(
      color = wireColor,
      start = Offset(breakerRight, midY),
      end = Offset(layout.junctionX, midY),
      strokeWidth = 2.dp.toPx(),
      alpha = 0.55F,
  )

  // Trunk + branches to each device node
  val firstCenter = layout.deviceRects.first().center.y
  val lastCenter = layout.deviceRects.last().center.y
  drawLine(
      color = wireColor,
      start = Offset(layout.junctionX, firstCenter),
      end = Offset(layout.junctionX, lastCenter),
      strokeWidth = 2.dp.toPx(),
      alpha = 0.55F,
  )
  layout.deviceRects.forEach { rect ->
    drawLine(
        color = wireColor,
        start = Offset(layout.junctionX, rect.center.y),
        end = Offset(rect.left, rect.center.y),
        strokeWidth = 2.dp.toPx(),
        alpha = 0.55F,
    )
  }

  // Junction dots
  drawCircle(
      color = wireColor,
      radius = 4.dp.toPx(),
      center = Offset(layout.junctionX, midY),
  )
}

private fun DrawScope.drawWave(
    layout: DiagramLayout,
    waveColor: Color,
    isFlowing: Boolean,
    phase: Float,
) {
  val midY = layout.midY
  val breakerLeft = layout.breakerCenter.x - layout.breakerRadius
  val breakerRight = layout.breakerCenter.x + layout.breakerRadius

  val path = Path()
  var started = false

  fun appendWave(fromX: Float, toX: Float) {
    val amplitude = 7.dp.toPx()
    val wavelength = 26.dp.toPx()
    var x = fromX
    while (x <= toX) {
      val y = midY + sin((x - fromX) / wavelength * 2F * PI.toFloat()) * amplitude
      if (!started) {
        path.moveTo(x, y)
        started = true
      } else {
        path.lineTo(x, y)
      }
      x += 2.dp.toPx()
    }
  }

  appendWave(layout.phoneRect.right, breakerLeft)
  appendWave(breakerRight, layout.junctionX)

  drawPath(
      path = path,
      color = waveColor,
      style =
          Stroke(
              width = 2.5.dp.toPx(),
              pathEffect =
                  if (isFlowing) {
                    PathEffect.dashPathEffect(
                        intervals = floatArrayOf(6.dp.toPx(), 10.dp.toPx()),
                        phase = phase * 16.dp.toPx(),
                    )
                  } else {
                    null
                  },
          ),
  )
}

private fun DrawScope.drawBreaker(
    layout: DiagramLayout,
    label: String,
    isSharing: Boolean,
    isError: Boolean,
    isTransitioning: Boolean,
    textMeasurer: TextMeasurer,
    liveColor: Color,
    mutedColor: Color,
    errorColor: Color,
    inkColor: Color,
) {
  val center = layout.breakerCenter
  val radius = layout.breakerRadius
  val midY = layout.midY

  val ringColor =
      when {
        isError -> errorColor
        isSharing -> liveColor
        else -> mutedColor
      }

  // Ring
  drawCircle(
      color = ringColor,
      radius = radius,
      center = center,
      style = Stroke(width = 2.dp.toPx()),
  )

  // Lever: closed when sharing, open when off, half-drawn while transitioning
  val leverColor =
      when {
        isError -> errorColor
        isSharing -> liveColor
        else -> mutedColor
      }
  if (isSharing || isTransitioning) {
    drawLine(
        color = leverColor,
        start = Offset(center.x - 13.dp.toPx(), midY),
        end = Offset(center.x + 13.dp.toPx(), midY),
        strokeWidth = 3.dp.toPx(),
    )
  } else {
    drawLine(
        color = leverColor,
        start = Offset(center.x - 13.dp.toPx(), midY),
        end = Offset(center.x + 9.dp.toPx(), midY - 18.dp.toPx()),
        strokeWidth = 3.dp.toPx(),
    )
  }

  // Contact dots
  drawCircle(
      color = leverColor,
      radius = 3.dp.toPx(),
      center = Offset(center.x - 13.dp.toPx(), midY),
  )
  drawCircle(
      color = leverColor,
      radius = 3.dp.toPx(),
      center = Offset(center.x + 13.dp.toPx(), midY),
  )

  // Label above the breaker
  val labelStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          letterSpacing = 1.sp,
          color = if (isError) errorColor else inkColor,
      )
  val measured = textMeasurer.measure(text = label, style = labelStyle)
  drawText(
      textLayoutResult = measured,
      topLeft =
          Offset(
              x = center.x - measured.size.width / 2F,
              y = center.y - radius - 20.dp.toPx(),
          ),
  )
}

private fun DrawScope.drawPhoneNode(
    layout: DiagramLayout,
    textMeasurer: TextMeasurer,
    wireColor: Color,
    mutedColor: Color,
    inkColor: Color,
    isDimmed: Boolean,
    phoneLabel: String,
    thisDeviceLabel: String,
) {
  val rect = layout.phoneRect
  val outline = if (isDimmed) mutedColor else wireColor

  drawRoundRect(
      color = outline,
      topLeft = rect.topLeft,
      size = rect.size,
      cornerRadius = CornerRadius(9.dp.toPx()),
      style = Stroke(width = 2.5.dp.toPx()),
      alpha = if (isDimmed) 0.5F else 1F,
  )
  // Screen
  drawRoundRect(
      color = outline,
      topLeft = Offset(rect.left + 9.dp.toPx(), rect.top + 12.dp.toPx()),
      size = Size(40.dp.toPx(), 44.dp.toPx()),
      cornerRadius = CornerRadius(3.dp.toPx()),
      style = Stroke(width = 1.5.dp.toPx()),
      alpha = if (isDimmed) 0.5F else 1F,
  )

  val labelStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = inkColor,
      )
  val label = textMeasurer.measure(text = phoneLabel, style = labelStyle)
  drawText(
      textLayoutResult = label,
      topLeft =
          Offset(
              x = rect.center.x - label.size.width / 2F,
              y = rect.bottom + 8.dp.toPx(),
          ),
      alpha = if (isDimmed) 0.5F else 1F,
  )

  val subStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = mutedColor,
      )
  val sub = textMeasurer.measure(text = thisDeviceLabel, style = subStyle)
  drawText(
      textLayoutResult = sub,
      topLeft =
          Offset(
              x = rect.center.x - sub.size.width / 2F,
              y = rect.bottom + 26.dp.toPx(),
          ),
      alpha = if (isDimmed) 0.5F else 1F,
  )
}

private fun DrawScope.drawDeviceNodes(
    layout: DiagramLayout,
    clients: List<TetherClient>,
    blocked: List<TetherClient>,
    textMeasurer: TextMeasurer,
    wireColor: Color,
    mutedColor: Color,
    errorColor: Color,
    inkColor: Color,
    isDimmed: Boolean,
) {
  val nameStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = inkColor,
      )
  val subStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = mutedColor,
      )
  val blockedStyle =
      TextStyle(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          color = errorColor,
      )

  if (clients.isEmpty()) {
    val rect = layout.deviceRects.first()
    drawRoundRect(
        color = mutedColor,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(6.dp.toPx()),
        style = Stroke(width = 2.dp.toPx()),
        alpha = 0.45F,
    )
    val name = textMeasurer.measure(text = "—", style = nameStyle)
    drawText(
        textLayoutResult = name,
        topLeft =
            Offset(
                x = rect.center.x - name.size.width / 2F,
                y = rect.top + 7.dp.toPx(),
            ),
        alpha = 0.45F,
    )
    return
  }

  clients.forEachIndexed { index, client ->
    if (index >= layout.deviceRects.size) {
      return@forEachIndexed
    }
    val rect = layout.deviceRects[index]
    val isBlocked = blocked.any { it.key() == client.key() }
    val outline =
        when {
          isBlocked -> errorColor
          isDimmed -> mutedColor
          else -> wireColor
        }
    val alpha = if (isDimmed) 0.45F else 1F

    drawRoundRect(
        color = outline,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(6.dp.toPx()),
        style = Stroke(width = 2.dp.toPx()),
        alpha = alpha,
    )

    val name = client.nickName.ifBlank { client.key() }
    val measuredName =
        textMeasurer.measure(
            text = name,
            style = nameStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            constraints =
                androidx.compose.ui.unit.Constraints(
                    maxWidth = (rect.width - 12.dp.toPx()).toInt(),
                ),
        )
    drawText(
        textLayoutResult = measuredName,
        topLeft =
            Offset(
                x = rect.center.x - measuredName.size.width / 2F,
                y = rect.top + 7.dp.toPx(),
            ),
        alpha = alpha,
    )

    if (isBlocked) {
      val blockedText = textMeasurer.measure(text = "blocked", style = blockedStyle)
      drawText(
          textLayoutResult = blockedText,
          topLeft =
              Offset(
                  x = rect.center.x - blockedText.size.width / 2F,
                  y = rect.top + 26.dp.toPx(),
              ),
          alpha = alpha,
      )
    } else {
      val sub =
          textMeasurer.measure(
              text = client.key(),
              style = subStyle,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              constraints =
                  androidx.compose.ui.unit.Constraints(
                      maxWidth = (rect.width - 12.dp.toPx()).toInt(),
                  ),
          )
      drawText(
          textLayoutResult = sub,
          topLeft =
              Offset(
                  x = rect.center.x - sub.size.width / 2F,
                  y = rect.top + 26.dp.toPx(),
              ),
          alpha = alpha,
      )
    }
  }
}
