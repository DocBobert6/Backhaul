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

package com.pyamsoft.tetherfi.server.status

import kotlin.test.assertSame
import org.junit.Test

/**
 * Regression script for the tile/app toggle consistency (Phase 3, item 6).
 *
 * The app toggle, the Behavior screen and the QS tile all read the same
 * two status singletons, and they must all agree on what the pair means.
 * Before overallStatus() existed, each surface combined the pair with
 * its own inline rules — and the rules disagreed:
 *
 * - The tile returned the broadcast status whenever neither had an
 *   error, so it showed Running (active) while the proxy was Stopping.
 * - The status screen showed Running while the proxy was still
 *   Starting (broadcast up, proxy not up yet).
 *
 * This pins all 25 broadcast/proxy pairs. If any pair changes meaning,
 * this test fails first — fix the derivation, do not weaken the table.
 * The device half of the script (toggle from the app, toggle from the
 * tile, toggle from the tile with the app killed, toggle on then off
 * from the other surface) cannot run in CI; it is Robert's device gate.
 */
class OverallStatusTest {

  private val broadcastError = RunningStatus.HotspotError(RuntimeException("broadcast"))
  private val proxyError = RunningStatus.ProxyError(RuntimeException("proxy"))

  @Test
  fun `broadcast error wins over everything`() {
    // Includes the both-errors pair.
    for (proxy in ALL + proxyError) {
      assertSame(
          broadcastError,
          overallStatus(broadcastError, proxy),
          "broadcast=$broadcastError proxy=$proxy",
      )
    }
  }

  @Test
  fun `proxy error wins when broadcast has no error`() {
    for (broadcast in NO_ERROR) {
      assertSame(
          proxyError,
          overallStatus(broadcast, proxyError),
          "broadcast=$broadcast proxy=$proxyError",
      )
    }
  }

  @Test
  fun `matrix - every pair agrees on one overall status`() {
    // broadcast, proxy, expected
    val cases =
        listOf(
            // Broadcast not running
            Triple(RunningStatus.NotRunning, RunningStatus.NotRunning, RunningStatus.NotRunning),
            Triple(RunningStatus.NotRunning, RunningStatus.Starting, RunningStatus.Starting),
            Triple(RunningStatus.NotRunning, RunningStatus.Running, RunningStatus.NotRunning),
            Triple(RunningStatus.NotRunning, RunningStatus.Stopping, RunningStatus.Stopping),
            // Broadcast starting
            Triple(RunningStatus.Starting, RunningStatus.NotRunning, RunningStatus.Starting),
            Triple(RunningStatus.Starting, RunningStatus.Starting, RunningStatus.Starting),
            Triple(RunningStatus.Starting, RunningStatus.Running, RunningStatus.Starting),
            Triple(RunningStatus.Starting, RunningStatus.Stopping, RunningStatus.Starting),
            // Broadcast running — the proxy decides, but a proxy that is
            // not up yet still counts as starting, never as running.
            Triple(RunningStatus.Running, RunningStatus.NotRunning, RunningStatus.Starting),
            Triple(RunningStatus.Running, RunningStatus.Starting, RunningStatus.Starting),
            Triple(RunningStatus.Running, RunningStatus.Running, RunningStatus.Running),
            Triple(RunningStatus.Running, RunningStatus.Stopping, RunningStatus.Stopping),
            // Broadcast stopping
            Triple(RunningStatus.Stopping, RunningStatus.NotRunning, RunningStatus.Stopping),
            Triple(RunningStatus.Stopping, RunningStatus.Starting, RunningStatus.Starting),
            Triple(RunningStatus.Stopping, RunningStatus.Running, RunningStatus.Stopping),
            Triple(RunningStatus.Stopping, RunningStatus.Stopping, RunningStatus.Stopping),
        )

    for ((broadcast, proxy, expected) in cases) {
      assertSame(
          expected,
          overallStatus(broadcast, proxy),
          "broadcast=$broadcast proxy=$proxy",
      )
    }
  }

  private companion object {
    // The four non-error states. Error pairs are covered by the two
    // dedicated error tests above (including both-errors).
    val ALL =
        listOf(
            RunningStatus.NotRunning,
            RunningStatus.Starting,
            RunningStatus.Running,
            RunningStatus.Stopping,
        )

    val NO_ERROR = ALL
  }
}
