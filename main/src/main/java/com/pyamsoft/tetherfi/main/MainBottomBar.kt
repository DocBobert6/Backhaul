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

package com.pyamsoft.tetherfi.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * The Carrier tab strip: a thin rule, then text tabs — no filled pill.
 */
@Composable
fun MainBottomBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    allDestinations: List<MainView>,
    onDestinationChanged: (MainView) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )

    MainNavigationBar(
        pagerState = pagerState,
        allDestinations = allDestinations,
        onDestinationChanged = onDestinationChanged,
    )

    Spacer(
        modifier = Modifier.navigationBarsPadding(),
    )
  }
}

@Preview
@Composable
private fun PreviewMainBottomBar() {
  val allDestinations = rememberAllDestinations()
  MainBottomBar(
      pagerState =
          rememberPagerState(
              initialPage = 0,
              initialPageOffsetFraction = 0F,
              pageCount = { allDestinations.size },
          ),
      allDestinations = allDestinations,
      onDestinationChanged = {},
  )
}
