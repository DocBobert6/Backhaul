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

package com.pyamsoft.tetherfi.status

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tetherfi.connections.ConnectionInjector
import com.pyamsoft.tetherfi.connections.ConnectionViewManagement
import com.pyamsoft.tetherfi.connections.ConnectionViewModel
import com.pyamsoft.tetherfi.server.status.RunningStatus
import com.pyamsoft.tetherfi.ui.ServerPortTypes
import com.pyamsoft.tetherfi.ui.ServerViewState

/** On mount hooks */
@Composable
private fun MountHooks(
    viewModel: StatusViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(
      viewModel,
  ) {
    viewModel.bind(scope = this)
  }
}

/** On mount hooks for the connections viewmodel, folded in from the old Clients tab */
@Composable
private fun MountHooks(
    viewModel: ConnectionViewModel,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
fun StatusEntry(
    modifier: Modifier = Modifier,
    appName: String,
    lazyListState: LazyListState,
    serverViewState: ServerViewState,

    // Main
    onHttpEnabledChanged: (Boolean) -> Unit,
    onSocksEnabledChanged: (Boolean) -> Unit,
    onPortChanged: (Int) -> Unit,

    // Actions
    onShowQRCode: () -> Unit,
    onRefreshConnection: () -> Unit,
    onJumpToHowTo: () -> Unit,
    onShowSlowSpeedHelp: () -> Unit,
    onToggleProxy: () -> Unit,

    // Dialogs
    onOpenNetworkError: () -> Unit,
    onOpenHotspotError: () -> Unit,
    onOpenProxyError: () -> Unit,
    onOpenBroadcastError: () -> Unit,

    // Tile
    onUpdateTile: (RunningStatus) -> Unit,

    // Error
    onEnableChangeFailed: (ServerPortTypes) -> Unit,
) {
  val component = rememberComposableInjector { StatusInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  // The old Clients tab now lives on the diagram, so its viewmodel is
  // bound here too. Same mount hooks, same lifecycle.
  val connectionComponent = rememberComposableInjector { ConnectionInjector() }
  val connectionViewModel = rememberNotNull(connectionComponent.viewModel)

  // Use the LifecycleOwner.CoroutineScope (Activity usually)
  // so that the scope does not die because of navigation events
  val owner = LocalLifecycleOwner.current
  val lifecycleScope = owner.lifecycleScope

  val handleToggleProxy by rememberUpdatedState(onToggleProxy)
  val handleOpenNetworkError by rememberUpdatedState(onOpenNetworkError)
  val handleOpenHotspotError by rememberUpdatedState(onOpenHotspotError)
  val handleOpenBroadcastError by rememberUpdatedState(onOpenBroadcastError)
  val handleOpenProxyError by rememberUpdatedState(onOpenProxyError)

  // Hooks that run on mount
  MountHooks(
      viewModel = viewModel,
  )

  MountHooks(
      viewModel = connectionViewModel,
  )

  StatusScreen(
      modifier = modifier,
      state = viewModel,
      lazyListState = lazyListState,
      serverViewState = serverViewState,
      appName = appName,
      onShowQRCode = onShowQRCode,
      onRefreshConnection = onRefreshConnection,
      onJumpToHowTo = onJumpToHowTo,
      onEnableChangeFailed = onEnableChangeFailed,
      onHttpEnabledChanged = onHttpEnabledChanged,
      onSocksEnabledChanged = onSocksEnabledChanged,
      onPortChanged = onPortChanged,
      onToggleProxy = {
        viewModel.handleToggleProxy(
            onToggleProxy = handleToggleProxy,
        )
      },
      onSsidChanged = { viewModel.handleSsidChanged(it.trim()) },
      onPasswordChanged = { viewModel.handlePasswordChanged(it) },
      onViewSlowSpeedHelp = onShowSlowSpeedHelp,
      onSelectBand = { viewModel.handleChangeBand(it) },
      onStatusUpdated = onUpdateTile,
      onTogglePasswordVisibility = { viewModel.handleTogglePasswordVisibility() },
      onShowNetworkError = { handleOpenNetworkError() },
      onShowHotspotError = { handleOpenHotspotError() },
      onShowProxyError = { handleOpenProxyError() },
      onShowBroadcastError = { handleOpenBroadcastError() },
      onSelectBroadcastType = { viewModel.handleUpdateBroadcastType(it) },
      onSelectPreferredNetwork = { viewModel.handleUpdatePreferredNetwork(it) },
      connectionState = connectionViewModel,
      onToggleBlock = { connectionViewModel.handleToggleBlock(it) },
      onOpenManageNickName = {
        connectionViewModel.handleOpenManage(it, ConnectionViewManagement.NICK_NAME)
      },
      onCloseManageNickName = {
        connectionViewModel.handleCloseManage(ConnectionViewManagement.NICK_NAME)
      },
      onUpdateNickName = {
        connectionViewModel.handleUpdateNickName(
            scope = lifecycleScope,
            nickName = it,
        )
      },
      onOpenManageTransferLimit = {
        connectionViewModel.handleOpenManage(it, ConnectionViewManagement.TRANSFER_LIMIT)
      },
      onCloseManageTransferLimit = {
        connectionViewModel.handleCloseManage(ConnectionViewManagement.TRANSFER_LIMIT)
      },
      onUpdateTransferLimit = {
        connectionViewModel.handleUpdateTransferLimit(
            scope = lifecycleScope,
            limit = it,
        )
      },
      onOpenManageBandwidthLimit = {
        connectionViewModel.handleOpenManage(it, ConnectionViewManagement.BANDWIDTH_LIMIT)
      },
      onCloseManageBandwidthLimit = {
        connectionViewModel.handleCloseManage(ConnectionViewManagement.BANDWIDTH_LIMIT)
      },
      onUpdateBandwidthLimit = {
        connectionViewModel.handleUpdateBandwidthLimit(
            scope = lifecycleScope,
            limit = it,
        )
      },
  )
}
