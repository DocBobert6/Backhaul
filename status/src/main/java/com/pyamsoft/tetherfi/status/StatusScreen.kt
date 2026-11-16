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

@file:LintIgnoreTooManyFunctions

package com.pyamsoft.tetherfi.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.core.LintIgnoreTooManyFunctions
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.tetherfi.connections.ConnectionViewState
import com.pyamsoft.tetherfi.server.ServerNetworkBand
import com.pyamsoft.tetherfi.server.broadcast.BroadcastType
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.server.clients.TransferAmount
import com.pyamsoft.tetherfi.server.network.PreferredNetwork
import com.pyamsoft.tetherfi.server.status.ErrorKind
import com.pyamsoft.tetherfi.server.status.RunningStatus
import com.pyamsoft.tetherfi.server.status.overallStatus
import com.pyamsoft.tetherfi.status.diagram.ClientNodeDialog
import com.pyamsoft.tetherfi.status.diagram.DiagramSheet
import com.pyamsoft.tetherfi.status.diagram.NetworkDiagram
import com.pyamsoft.tetherfi.status.diagram.SheetDashedDivider
import com.pyamsoft.tetherfi.status.diagram.SheetDivider
import com.pyamsoft.tetherfi.status.diagram.SheetReadoutRow
import com.pyamsoft.tetherfi.status.diagram.SheetSectionHeader
import com.pyamsoft.tetherfi.status.diagram.ThroughputSparkline
import com.pyamsoft.tetherfi.status.diagram.rememberThroughput
import com.pyamsoft.tetherfi.status.sections.network.ViewPassword
import com.pyamsoft.tetherfi.ui.LANDSCAPE_MAX_WIDTH
import com.pyamsoft.tetherfi.ui.LoadingSpinner
import com.pyamsoft.tetherfi.ui.ServerPortTypes
import com.pyamsoft.tetherfi.ui.ServerViewState
import com.pyamsoft.tetherfi.ui.plainErrorSentence
import com.pyamsoft.tetherfi.ui.rememberPortNumber
import com.pyamsoft.tetherfi.ui.rememberServerHostname
import com.pyamsoft.tetherfi.ui.rememberServerSSID
import com.pyamsoft.tetherfi.ui.renderPYDroidExtras
import com.pyamsoft.tetherfi.ui.test.TEST_PASSWORD
import com.pyamsoft.tetherfi.ui.test.TEST_SSID
import com.pyamsoft.tetherfi.ui.test.TestServerState
import com.pyamsoft.tetherfi.ui.test.makeTestServerState
import java.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.annotations.TestOnly

private enum class StatusScreenContentTypes {
  DIAGRAM,
  LOADING,
}

@Composable
fun StatusScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: StatusViewState,
    lazyListState: LazyListState,
    serverViewState: ServerViewState,

    // Proxy
    onToggleProxy: () -> Unit,
    onStatusUpdated: (RunningStatus) -> Unit,

    // Network
    onSsidChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSelectBand: (ServerNetworkBand) -> Unit,
    onHttpEnabledChanged: (Boolean) -> Unit,
    onSocksEnabledChanged: (Boolean) -> Unit,
    onPortChanged: (Int) -> Unit,
    onEnableChangeFailed: (ServerPortTypes) -> Unit,

    // Status buttons
    onShowQRCode: () -> Unit,
    onRefreshConnection: () -> Unit,

    // Proxy Behavior
    onSelectBroadcastType: (BroadcastType) -> Unit,
    onSelectPreferredNetwork: (PreferredNetwork) -> Unit,

    // Errors
    onShowNetworkError: () -> Unit,
    onShowHotspotError: () -> Unit,
    onShowBroadcastError: () -> Unit,
    onShowProxyError: () -> Unit,

    // Jump links
    onJumpToHowTo: () -> Unit,
    onViewSlowSpeedHelp: () -> Unit,

    // Connections — the old Clients tab now lives on the diagram
    connectionState: ConnectionViewState,
    onToggleBlock: (TetherClient) -> Unit,
    onOpenManageNickName: (TetherClient) -> Unit,
    onCloseManageNickName: () -> Unit,
    onUpdateNickName: (String) -> Unit,
    onOpenManageTransferLimit: (TetherClient) -> Unit,
    onCloseManageTransferLimit: () -> Unit,
    onUpdateTransferLimit: (TransferAmount?) -> Unit,
    onOpenManageBandwidthLimit: (TetherClient) -> Unit,
    onCloseManageBandwidthLimit: () -> Unit,
    onUpdateBandwidthLimit: (TransferAmount?) -> Unit,
) {
  val wiDiStatus by serverViewState.wiDiStatus.collectAsStateWithLifecycle()
  val proxyStatus by serverViewState.proxyStatus.collectAsStateWithLifecycle()

  val hotspotStatus =
      remember(
          wiDiStatus,
          proxyStatus,
      ) {
        // One shared derivation for the app, the Behavior screen and
        // the QS tile — see overallStatus().
        return@remember overallStatus(wiDiStatus, proxyStatus)
      }

  val isButtonEnabled =
      remember(hotspotStatus) {
        hotspotStatus is RunningStatus.Running ||
            hotspotStatus is RunningStatus.NotRunning ||
            hotspotStatus is RunningStatus.Error
      }

  val isEditable =
      remember(hotspotStatus) {
        when (hotspotStatus) {
          is RunningStatus.Running,
          is RunningStatus.Starting,
          is RunningStatus.Stopping -> false
          else -> true
        }
      }

  val loadingState by state.loadingState.collectAsStateWithLifecycle()

  val handleStatusUpdated by rememberUpdatedState(onStatusUpdated)
  LaunchedEffect(hotspotStatus) { handleStatusUpdated(hotspotStatus) }

  // Clients, folded in from the old Clients tab
  val clients = connectionState.connections.collectAsStateListWithLifecycle()
  val blocked = connectionState.blocked.collectAsStateListWithLifecycle()
  var selectedClient by remember { mutableStateOf<TetherClient?>(null) }

  val isSharing = hotspotStatus is RunningStatus.Running

  LazyColumn(
      modifier = modifier,
      state = lazyListState,
      contentPadding = PaddingValues(horizontal = MaterialTheme.keylines.content),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item(
        contentType = StatusScreenContentTypes.DIAGRAM,
    ) {
      DiagramSheet(
          modifier =
              Modifier.width(LANDSCAPE_MAX_WIDTH).padding(top = MaterialTheme.keylines.content),
      ) {
        if (hotspotStatus is RunningStatus.NotRunning) {
          Text(
              modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
              text = stringResource(R.string.diagram_sharing_off),
              style =
                  MaterialTheme.typography.bodyMedium.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )
        }

        SheetSectionHeader(
            text = stringResource(R.string.diagram_section_network),
        )

        // The sampler is shared: the sparkline draws the history, and the
        // diagram's wave only runs while traffic is actually flowing.
        val throughput = rememberThroughput(
            clients = clients,
            isRunning = isSharing,
        )

        DiagramContent(
            state = state,
            serverViewState = serverViewState,
            wiDiStatus = wiDiStatus,
            proxyStatus = proxyStatus,
            hotspotStatus = hotspotStatus,
            isButtonEnabled = isButtonEnabled,
            clients = clients,
            blocked = blocked,
            isFlowing = isSharing && throughput.currentRateBytesPerSec > 0F,
            onToggleProxy = onToggleProxy,
            onClientSelected = { selectedClient = it },
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onShowBroadcastError = onShowBroadcastError,
            onShowProxyError = onShowProxyError,
        )

        SheetDivider()

        SheetSectionHeader(
            text = stringResource(R.string.diagram_section_throughput),
        )

        ThroughputSparkline(
            throughput = throughput,
            isRunning = isSharing,
        )

        if (isSharing) {
          DiagramActions(
              onShowQRCode = onShowQRCode,
              onRefreshConnection = onRefreshConnection,
          )

          if (clients.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
                text = stringResource(R.string.diagram_tap_node),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          } else {
            Text(
                modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
                text = stringResource(R.string.diagram_no_devices),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    renderPYDroidExtras(
        modifier = Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH),
    )

    when (loadingState) {
      StatusViewState.LoadingState.NONE,
      StatusViewState.LoadingState.LOADING -> {
        item(
            contentType = StatusScreenContentTypes.LOADING,
        ) {
          LoadingSpinner(
              modifier =
                  Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH)
                      .padding(MaterialTheme.keylines.content),
          )
        }
      }
      StatusViewState.LoadingState.DONE -> {
        renderLoadedContent(
            // Not widthIn because a TextField does not take up "all" by default
            itemModifier = Modifier.width(LANDSCAPE_MAX_WIDTH),
            appName = appName,
            state = state,
            serverViewState = serverViewState,
            isEditable = isEditable,
            wiDiStatus = wiDiStatus,
            proxyStatus = proxyStatus,
            onSsidChanged = onSsidChanged,
            onPasswordChanged = onPasswordChanged,
            onHttpEnabledChanged = onHttpEnabledChanged,
            onSocksEnabledChanged = onSocksEnabledChanged,
            onPortChanged = onPortChanged,
            onSelectBand = onSelectBand,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            onShowQRCode = onShowQRCode,
            onRefreshConnection = onRefreshConnection,
            onShowHotspotError = onShowHotspotError,
            onShowNetworkError = onShowNetworkError,
            onJumpToHowTo = onJumpToHowTo,
            onViewSlowSpeedHelp = onViewSlowSpeedHelp,
            onSelectBroadcastType = onSelectBroadcastType,
            onSelectPreferredNetwork = onSelectPreferredNetwork,
            onEnableChangeFailed = onEnableChangeFailed,
        )
      }
    }
  }

  selectedClient?.also { client ->
    ClientNodeDialog(
        client = client,
        connectionState = connectionState,
        onDismiss = { selectedClient = null },
        onToggleBlock = onToggleBlock,
        onOpenManageNickName = onOpenManageNickName,
        onCloseManageNickName = onCloseManageNickName,
        onUpdateNickName = onUpdateNickName,
        onOpenManageTransferLimit = onOpenManageTransferLimit,
        onCloseManageTransferLimit = onCloseManageTransferLimit,
        onUpdateTransferLimit = onUpdateTransferLimit,
        onOpenManageBandwidthLimit = onOpenManageBandwidthLimit,
        onCloseManageBandwidthLimit = onCloseManageBandwidthLimit,
        onUpdateBandwidthLimit = onUpdateBandwidthLimit,
    )
  }
}

@Composable
private fun DiagramContent(
    state: StatusViewState,
    serverViewState: ServerViewState,
    wiDiStatus: RunningStatus,
    proxyStatus: RunningStatus,
    hotspotStatus: RunningStatus,
    isButtonEnabled: Boolean,
    clients: List<TetherClient>,
    blocked: List<TetherClient>,
    isFlowing: Boolean,
    onToggleProxy: () -> Unit,
    onClientSelected: (TetherClient) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onShowBroadcastError: () -> Unit,
    onShowProxyError: () -> Unit,
) {
  val group by serverViewState.group.collectAsStateWithLifecycle()
  val connection by serverViewState.connection.collectAsStateWithLifecycle()
  val broadcastType by serverViewState.broadcastType.collectAsStateWithLifecycle()
  val port by serverViewState.port.collectAsStateWithLifecycle()
  val band by state.band.collectAsStateWithLifecycle()

  val isSharing = hotspotStatus is RunningStatus.Running
  val readoutColor =
      if (isSharing) {
        androidx.compose.ui.graphics.Color.Unspecified
      } else {
        MaterialTheme.colorScheme.onSurfaceVariant
      }

  NetworkDiagram(
      hotspotStatus = hotspotStatus,
      isButtonEnabled = isButtonEnabled,
      clients = clients,
      blocked = blocked,
      isFlowing = isFlowing,
      onToggleProxy = onToggleProxy,
      onClientSelected = onClientSelected,
  )

  // Errors say what failed, in words, red — broadcast vs proxy.
  if (wiDiStatus is RunningStatus.Error) {
    Text(
        modifier =
            Modifier.padding(top = MaterialTheme.keylines.baseline)
                .clickable { onShowBroadcastError() },
        text = plainErrorSentence(wiDiStatus.throwable, ErrorKind.BROADCAST),
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W700,
            ),
        color = MaterialTheme.colorScheme.error,
    )
  }
  if (proxyStatus is RunningStatus.Error) {
    Text(
        modifier =
            Modifier.padding(top = MaterialTheme.keylines.baseline)
                .clickable { onShowProxyError() },
        text = plainErrorSentence(proxyStatus.throwable, ErrorKind.PROXY),
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.W700,
            ),
        color = MaterialTheme.colorScheme.error,
    )
  }

  // Machine readouts, always monospace — plain words for the keys,
  // dashed rules between the readings of the same drawing.
  val ssid = rememberServerSSID(group)
  val hostname = rememberServerHostname(connection)
  val portNumber = rememberPortNumber(port)
  SheetReadoutRow(
      key = stringResource(R.string.diagram_readout_network),
      value = ssid,
      valueColor = readoutColor,
  )
  SheetDashedDivider()
  SheetReadoutRow(
      key = stringResource(R.string.diagram_readout_proxy),
      value = "$hostname:$portNumber · ${stringResource(R.string.diagram_proxy_both)}",
      valueColor = readoutColor,
  )
  if (band != null) {
    val bandText =
        when (band) {
          ServerNetworkBand.LEGACY -> stringResource(R.string.diagram_band_legacy)
          ServerNetworkBand.MODERN -> stringResource(R.string.diagram_band_modern)
          ServerNetworkBand.MODERN_6 -> stringResource(R.string.diagram_band_modern_6)
          null -> ""
        }
    if (bandText.isNotBlank()) {
      SheetDashedDivider()
      SheetReadoutRow(
          key = stringResource(R.string.diagram_readout_band),
          value = bandText,
          valueColor = readoutColor,
      )
    }
  }

  // The password stays visible on request while running, as before.
  if (isSharing && broadcastType == BroadcastType.WIFI_DIRECT) {
    ViewPassword(
        modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
        state = state,
        serverViewState = serverViewState,
        onTogglePasswordVisibility = onTogglePasswordVisibility,
    )
  }
}

@Composable
private fun DiagramActions(
    onShowQRCode: () -> Unit,
    onRefreshConnection: () -> Unit,
) {
  androidx.compose.foundation.layout.Row(
      modifier = Modifier.padding(top = MaterialTheme.keylines.content),
  ) {
    TextButton(
        onClick = onShowQRCode,
    ) {
      Text(
          text = stringResource(R.string.diagram_show_qr),
      )
    }
    TextButton(
        onClick = onRefreshConnection,
    ) {
      Text(
          text = stringResource(R.string.diagram_refresh),
      )
    }
  }
}

@TestOnly
@Composable
private fun PreviewStatusScreen(
    isLoading: Boolean,
    ssid: String = TEST_SSID,
    password: String = TEST_PASSWORD,
    http: Boolean,
    socks: Boolean,
) {
  StatusScreen(
      state =
          MutableStatusViewState().apply {
            loadingState.value =
                if (isLoading) StatusViewState.LoadingState.LOADING
                else StatusViewState.LoadingState.DONE
            this.ssid.value = ssid
            this.password.value = password
            band.value = ServerNetworkBand.LEGACY
          },
      serverViewState = makeTestServerState(TestServerState.EMPTY, http, socks),
      lazyListState = rememberLazyListState(),
      appName = "TEST",
      onStatusUpdated = {},
      onSelectBand = {},
      onPasswordChanged = {},
      onHttpEnabledChanged = {},
      onSocksEnabledChanged = {},
      onPortChanged = {},
      onSsidChanged = {},
      onToggleProxy = {},
      onTogglePasswordVisibility = {},
      onShowQRCode = {},
      onRefreshConnection = {},
      onShowHotspotError = {},
      onShowNetworkError = {},
      onJumpToHowTo = {},
      onShowBroadcastError = {},
      onShowProxyError = {},
      onViewSlowSpeedHelp = {},
      onSelectBroadcastType = {},
      onSelectPreferredNetwork = {},
      onEnableChangeFailed = {},
      connectionState =
          object : ConnectionViewState {
            override val connections = MutableStateFlow(emptyList<TetherClient>())
            override val blocked = MutableStateFlow<Collection<TetherClient>>(emptySet())
            override val managingNickName = MutableStateFlow<TetherClient?>(null)
            override val managingTransferLimit = MutableStateFlow<TetherClient?>(null)
            override val managingBandwidthLimit = MutableStateFlow<TetherClient?>(null)
          },
      onToggleBlock = {},
      onOpenManageNickName = {},
      onCloseManageNickName = {},
      onUpdateNickName = {},
      onOpenManageTransferLimit = {},
      onCloseManageTransferLimit = {},
      onUpdateTransferLimit = {},
      onOpenManageBandwidthLimit = {},
      onCloseManageBandwidthLimit = {},
      onUpdateBandwidthLimit = {},
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenLoadingHttp() {
  PreviewStatusScreen(
      isLoading = true,
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingHttp() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadSsidHttp() {
  PreviewStatusScreen(
      isLoading = false,
      ssid = "nope",
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPasswordHttp() {
  PreviewStatusScreen(
      isLoading = false,
      password = "nope",
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort1Http() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort2Http() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = false,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenLoadingSocks() {
  PreviewStatusScreen(
      isLoading = true,
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingSocks() {
  PreviewStatusScreen(
      isLoading = false,
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadSsidSocks() {
  PreviewStatusScreen(
      isLoading = false,
      ssid = "nope",
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPasswordSocks() {
  PreviewStatusScreen(
      isLoading = false,
      password = "nope",
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort1Socks() {
  PreviewStatusScreen(
      isLoading = false,
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort2Socks() {
  PreviewStatusScreen(
      isLoading = false,
      http = false,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenLoadingBoth() {
  PreviewStatusScreen(
      isLoading = true,
      http = true,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBoth() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadSsidBoth() {
  PreviewStatusScreen(
      isLoading = false,
      ssid = "nope",
      http = true,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPasswordBoth() {
  PreviewStatusScreen(
      isLoading = false,
      password = "nope",
      http = true,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort1Both() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = true,
  )
}

@Composable
@Preview(showBackground = true)
private fun PreviewStatusScreenEditingBadPort2Both() {
  PreviewStatusScreen(
      isLoading = false,
      http = true,
      socks = true,
  )
}
