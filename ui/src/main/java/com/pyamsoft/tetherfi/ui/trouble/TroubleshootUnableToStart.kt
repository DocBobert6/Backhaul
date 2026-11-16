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

package com.pyamsoft.tetherfi.ui.trouble

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.server.broadcast.BroadcastType
import com.pyamsoft.tetherfi.server.status.ErrorKind
import com.pyamsoft.tetherfi.ui.R
import com.pyamsoft.tetherfi.ui.plainErrorSentence
import org.jetbrains.annotations.TestOnly

/**
 * The plain-language error sheet for a failed start.
 *
 * One title naming what failed, one plain sentence naming the problem
 * and the fix, then a short checklist in plain words. The throwable is
 * used only to pick the sentence — its message and its stack trace are
 * never shown.
 */
@Composable
@CheckResult
private fun resolveTitle(
    isBroadcastError: Boolean,
    isProxyError: Boolean,
): String {
  return if (isBroadcastError && isProxyError) {
    stringResource(R.string.trouble_title_both)
  } else if (isProxyError) {
    stringResource(R.string.trouble_title_proxy)
  } else {
    stringResource(R.string.trouble_title_broadcast)
  }
}

@Composable
@CheckResult
private fun resolveKind(
    isBroadcastError: Boolean,
    isProxyError: Boolean,
): ErrorKind {
  return if (isBroadcastError && isProxyError) {
    ErrorKind.GENERIC
  } else if (isProxyError) {
    ErrorKind.PROXY
  } else {
    ErrorKind.BROADCAST
  }
}

@Composable
fun TroubleshootUnableToStart(
    modifier: Modifier = Modifier,
    appName: String,
    broadcastType: BroadcastType?,
    isBroadcastError: Boolean,
    isProxyError: Boolean,
    throwable: Throwable?,
) {
  val showTroubleshooting =
      remember(isBroadcastError, isProxyError) { isBroadcastError || isProxyError }

  val title = resolveTitle(isBroadcastError, isProxyError)
  val sentence = plainErrorSentence(throwable, resolveKind(isBroadcastError, isProxyError))

  Column(
      modifier = modifier.padding(horizontal = MaterialTheme.keylines.content),
  ) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.titleLarge,
    )
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
        text = sentence,
        fontWeight = FontWeight.W700,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )

    if (showTroubleshooting) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = stringResource(R.string.trouble_double_check),
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.W700,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    if (isBroadcastError) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = stringResource(R.string.trouble_location_service),
          style = MaterialTheme.typography.bodyLarge,
      )

      if (broadcastType == BroadcastType.WIFI_DIRECT) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = stringResource(R.string.trouble_broadcast_wifi_on),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = stringResource(R.string.trouble_broadcast_wifi_restart),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = stringResource(R.string.trouble_broadcast_password_length),
            style = MaterialTheme.typography.bodyLarge,
        )
      } else if (broadcastType == BroadcastType.RNDIS) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = stringResource(R.string.trouble_connect_rndis),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = stringResource(R.string.trouble_broadcast_rndis),
            style = MaterialTheme.typography.bodyLarge,
        )
      }
    }

    if (isProxyError) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = stringResource(R.string.trouble_proxy_already_used),
          style = MaterialTheme.typography.bodyLarge,
      )

      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = stringResource(R.string.trouble_proxy_port),
          style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}

@TestOnly
@Composable
private fun PreviewTroubleshootUnableToStart(
    broadcastType: BroadcastType,
    isBroadcastError: Boolean,
    isProxyError: Boolean,
) {
  TroubleshootUnableToStart(
      appName = "TEST",
      broadcastType = broadcastType,
      isBroadcastError = isBroadcastError,
      isProxyError = isProxyError,
      throwable = RuntimeException("PREVIEW"),
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootUnableToStartBroadcast() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.WIFI_DIRECT,
      isBroadcastError = true,
      isProxyError = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootUnableToStartProxy() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.WIFI_DIRECT,
      isBroadcastError = false,
      isProxyError = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootUnableToStartNone() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.WIFI_DIRECT,
      isBroadcastError = false,
      isProxyError = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootUnableToStartAll() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.WIFI_DIRECT,
      isBroadcastError = true,
      isProxyError = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootRNDISUnableToStartBroadcast() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.RNDIS,
      isBroadcastError = true,
      isProxyError = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootRNDISUnableToStartProxy() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.RNDIS,
      isBroadcastError = false,
      isProxyError = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootRNDISUnableToStartNone() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.RNDIS,
      isBroadcastError = false,
      isProxyError = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewTroubleshootRNDISUnableToStartAll() {
  PreviewTroubleshootUnableToStart(
      broadcastType = BroadcastType.RNDIS,
      isBroadcastError = true,
      isProxyError = true,
  )
}
