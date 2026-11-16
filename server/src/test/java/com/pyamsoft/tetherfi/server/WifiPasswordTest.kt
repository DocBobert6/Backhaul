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

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class WifiPasswordTest {

  @Test
  fun `isValid accepts 8 to 63 characters`() {
    assertTrue(WifiPassword.isValid("12345678"))
    assertTrue(WifiPassword.isValid("a".repeat(63)))
  }

  @Test
  fun `isValid rejects anything shorter than 8 or longer than 63`() {
    assertFalse(WifiPassword.isValid(""))
    assertFalse(WifiPassword.isValid("short"))
    assertFalse(WifiPassword.isValid("1234567"))
    assertFalse(WifiPassword.isValid("a".repeat(64)))
  }

  @Test
  fun `generate makes a valid password of the requested length`() {
    val generated = WifiPassword.generate()
    assertEquals(8, generated.length)
    assertTrue(WifiPassword.isValid(generated))
    assertTrue(WifiPassword.isValid(WifiPassword.generate(size = 12)))
  }
}
