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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.connections.BandwidthLimitDialog
import com.pyamsoft.tetherfi.connections.ConnectionViewState
import com.pyamsoft.tetherfi.connections.NickNameDialog
import com.pyamsoft.tetherfi.connections.TransferDialog
import com.pyamsoft.tetherfi.connections.sections.list.ConnectionItem
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.server.clients.TransferAmount
import com.pyamsoft.tetherfi.ui.dialog.CardDialog

/**
 * Tapping a device node on the diagram opens that device's details — the
 * same card the Clients list used to show, with the same allow/block switch
 * and the same nickname / transfer-limit / bandwidth-limit management.
 * Nothing here is new behaviour; it is the old Clients tab, hosted on the
 * diagram instead of on its own tab.
 */
@Composable
internal fun ClientNodeDialog(
    modifier: Modifier = Modifier,
    client: TetherClient,
    connectionState: ConnectionViewState,
    onDismiss: () -> Unit,
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
  val blocked by connectionState.blocked.collectAsStateWithLifecycle()
  val manageNickName by connectionState.managingNickName.collectAsStateWithLifecycle()
  val manageTransfer by connectionState.managingTransferLimit.collectAsStateWithLifecycle()
  val manageBandwidth by connectionState.managingBandwidthLimit.collectAsStateWithLifecycle()

  CardDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    ConnectionItem(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        client = client,
        blocked = blocked.toList(),
        onToggleBlock = onToggleBlock,
        onManageNickName = onOpenManageNickName,
        onManageTransferLimit = onOpenManageTransferLimit,
        onManageBandwidthLimit = onOpenManageBandwidthLimit,
    )
  }

  manageNickName?.also { manage ->
    NickNameDialog(
        client = manage,
        onUpdateNickName = onUpdateNickName,
        onDismiss = { onCloseManageNickName() },
    )
  }

  manageTransfer?.also { manage ->
    TransferDialog(
        client = manage,
        onUpdateLimit = onUpdateTransferLimit,
        onDismiss = { onCloseManageTransferLimit() },
    )
  }

  manageBandwidth?.also { manage ->
    BandwidthLimitDialog(
        client = manage,
        onUpdateLimit = onUpdateBandwidthLimit,
        onDismiss = { onCloseManageBandwidthLimit() },
    )
  }
}
