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

import androidx.annotation.CheckResult

/**
 * The single source of truth for on/off state.
 *
 * The app toggle, the Behavior screen and the QS tile all read the same
 * two status singletons (the Wi-Fi Direct broadcast and the proxy), but
 * each of them used to combine those two into one overall status with
 * its own inline rules — and the rules disagreed:
 *
 * - The status screen said Stopping when either was stopping, but
 *   collapsed every error into one generic error.
 * - The Behavior screen had the fullest rules (either starting, and
 *   broadcast-up-but-proxy-not-up-yet counts as starting).
 * - The tile returned the broadcast status whenever neither had an
 *   error, so it could show Running (active) while the proxy was
 *   Stopping — and the status screen could show Running while the
 *   proxy was still Starting.
 *
 * Every surface now calls this one function. It is pure: same pair in,
 * same status out, no matter who asks. The matrix test next to it
 * (OverallStatusTest) is the regression script — it pins all 25
 * broadcast/proxy pairs.
 *
 * The rules are the Behavior screen's, which were the most careful of
 * the three, with one difference: an error returns the specific error
 * instead of a generic one, so the caller can say what failed. That
 * changes nothing observable on the screens — they only ever tested
 * `is Error` — and the tile already returned the specific error.
 */
@CheckResult
fun overallStatus(
    broadcast: RunningStatus,
    proxy: RunningStatus,
): RunningStatus {
  // A specific error always wins, broadcast first.
  if (broadcast is RunningStatus.Error) {
    return broadcast
  }
  if (proxy is RunningStatus.Error) {
    return proxy
  }

  // If either is starting, we are starting.
  if (broadcast is RunningStatus.Starting || proxy is RunningStatus.Starting) {
    return RunningStatus.Starting
  }

  // If the broadcast is up but the proxy is not up yet, we are starting.
  if (broadcast is RunningStatus.Running && proxy !is RunningStatus.Running) {
    return RunningStatus.Starting
  }

  // If either is stopping, we are stopping.
  if (broadcast is RunningStatus.Stopping || proxy is RunningStatus.Stopping) {
    return RunningStatus.Stopping
  }

  if (broadcast is RunningStatus.Running) {
    // The broadcast is up: the proxy decides.
    return proxy
  }

  // Otherwise fall back to the broadcast.
  return broadcast
}
