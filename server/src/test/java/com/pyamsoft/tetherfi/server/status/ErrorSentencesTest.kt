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

import com.pyamsoft.tetherfi.server.broadcast.wifidirect.WifiDirectServer.WifiP2PExceptionCollection
import java.net.BindException
import kotlin.test.assertEquals
import org.junit.Test

class ErrorSentencesTest {

  @Test
  fun `a passphrase validation failure sorts to the password sentence`() {
    val throwable =
        IllegalArgumentException("The length of a passphrase must be between 8 and 63")
    assertEquals(
        ErrorSentence.PASSWORD,
        ErrorSentences.classify(throwable, ErrorKind.BROADCAST),
    )
  }

  @Test
  fun `the same failure buried in a Wi-Fi Direct exception collection still sorts to the password sentence`() {
    val throwable =
        WifiP2PExceptionCollection(
            listOf(
                IllegalArgumentException("The length of a passphrase must be between 8 and 63"),
                IllegalArgumentException("The length of a passphrase must be between 8 and 63"),
                IllegalArgumentException("The length of a passphrase must be between 8 and 63"),
            ),
        )
    assertEquals(
        ErrorSentence.PASSWORD,
        ErrorSentences.classify(throwable, ErrorKind.BROADCAST),
    )
  }

  @Test
  fun `a bound port sorts to the port sentence`() {
    assertEquals(
        ErrorSentence.PORT_IN_USE,
        ErrorSentences.classify(BindException("Address already in use"), ErrorKind.PROXY),
    )
    assertEquals(
        ErrorSentence.PORT_IN_USE,
        ErrorSentences.classify(
            RuntimeException("bind(..) failed: Address already in use"),
            ErrorKind.PROXY,
        ),
    )
  }

  @Test
  fun `an unknown failure falls back to the sentence for the part that failed`() {
    assertEquals(
        ErrorSentence.BROADCAST,
        ErrorSentences.classify(RuntimeException("boom"), ErrorKind.BROADCAST),
    )
    assertEquals(
        ErrorSentence.PROXY,
        ErrorSentences.classify(RuntimeException("boom"), ErrorKind.PROXY),
    )
    assertEquals(
        ErrorSentence.GENERIC,
        ErrorSentences.classify(null, ErrorKind.GENERIC),
    )
  }
}
