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

package com.pyamsoft.tetherfi.server

import androidx.annotation.CheckResult
import kotlin.random.Random

/**
 * The hotspot password: what Android will accept, and how a fresh one
 * is made.
 *
 * Android refuses a Wi-Fi Direct passphrase shorter than 8 or longer
 * than 63 characters — WifiP2pConfig.Builder.build() throws
 * IllegalArgumentException. A stored password that fails this check is
 * treated as absent everywhere it is read: the reader repairs it to a
 * freshly generated password instead of using it verbatim, so no start
 * path can ever hand Android a passphrase it will refuse.
 */
object WifiPassword {

  private const val MIN_LENGTH = 8
  private const val MAX_LENGTH = 63

  private const val ALL_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

  @CheckResult
  fun isValid(password: String): Boolean {
    // Android's own rule, on the raw passphrase length.
    return password.length in MIN_LENGTH..MAX_LENGTH
  }

  @JvmStatic
  @CheckResult
  fun generate(size: Int = MIN_LENGTH): String {
    val chars = ALL_CHARS

    var pass = ""
    while (true) {
      pass += chars[Random.nextInt(0, chars.length)]

      // Stop once generated
      if (pass.length >= size) {
        break
      }
    }
    return pass
  }
}
