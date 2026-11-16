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
import com.pyamsoft.tetherfi.server.broadcast.wifidirect.WifiDirectServer.WifiP2PExceptionCollection
import java.net.BindException

/**
 * Which part of the job failed. The caller knows this; the classifier
 * only refines it when the failure itself says more.
 */
enum class ErrorKind {
  BROADCAST,
  PROXY,
  GENERIC,
}

/**
 * One plain sentence, naming the problem and the fix. The UI renders
 * one of these — never an exception's toString() or a reasons=[...]
 * list.
 */
enum class ErrorSentence {
  PASSWORD,
  PORT_IN_USE,
  BROADCAST,
  PROXY,
  GENERIC,
}

object ErrorSentences {

  /**
   * Sort a start failure into one plain sentence.
   *
   * A passphrase Android refuses surfaces as IllegalArgumentException
   * (thrown by WifiP2pConfig.Builder.build()); a port another app
   * already holds surfaces as BindException or as a bind failure whose
   * message says the address is already in use. Anything else falls
   * back to the sentence for the part that failed.
   */
  @CheckResult
  fun classify(
      throwable: Throwable?,
      kind: ErrorKind,
  ): ErrorSentence {
    if (throwable != null && matchesPasswordError(throwable)) {
      return ErrorSentence.PASSWORD
    }
    if (throwable != null && matchesPortInUseError(throwable)) {
      return ErrorSentence.PORT_IN_USE
    }
    return when (kind) {
      ErrorKind.BROADCAST -> ErrorSentence.BROADCAST
      ErrorKind.PROXY -> ErrorSentence.PROXY
      ErrorKind.GENERIC -> ErrorSentence.GENERIC
    }
  }

  @CheckResult
  private fun matchesPasswordError(throwable: Throwable): Boolean {
    return walkThrowables(throwable).any { it is IllegalArgumentException }
  }

  @CheckResult
  private fun matchesPortInUseError(throwable: Throwable): Boolean {
    return walkThrowables(throwable).any { t ->
      t is BindException ||
          t.message?.contains("Address already in use", ignoreCase = true) == true
    }
  }

  /**
   * Every throwable in the chain, including the reasons held by a
   * Wi-Fi Direct exception collection. Visited nodes are remembered so
   * a cyclic cause chain ends instead of looping.
   */
  @CheckResult
  private fun walkThrowables(throwable: Throwable): Sequence<Throwable> = sequence {
    val seen = mutableSetOf<Throwable>()
    val pending = ArrayDeque<Throwable>()
    pending.add(throwable)
    while (pending.isNotEmpty()) {
      val current = pending.removeFirst()
      if (!seen.add(current)) {
        continue
      }
      yield(current)
      if (current is WifiP2PExceptionCollection) {
        pending.addAll(current.reasons)
      }
      val cause = current.cause
      if (cause != null) {
        pending.add(cause)
      }
    }
  }
}
