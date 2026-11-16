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

import androidx.annotation.CheckResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@CheckResult
fun rememberAllDestinations(): List<MainView> {
  return remember { MainView.entries.toMutableStateList() }
}

@Composable
fun MainNavigationBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    allDestinations: List<MainView>,
    onDestinationChanged: (MainView) -> Unit,
) {
  val currentPage = pagerState.currentPage
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    for (index in allDestinations.indices) {
      val tab = allDestinations[index]
      val isSelected =
          remember(
              index,
              currentPage,
          ) {
            index == currentPage
          }

      MainDestination(
          modifier = Modifier.weight(1F),
          destination = tab,
          isSelected = isSelected,
          onSelected = { onDestinationChanged(tab) },
      )
    }
  }
}

/**
 * One Carrier tab: plain uppercase text. The selected tab is blueprint
 * blue and bold, with a 2dp blue line above it; the rest are muted.
 */
@Composable
private fun MainDestination(
    modifier: Modifier = Modifier,
    destination: MainView,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
  val destinationName = stringResource(destination.displayNameRes).uppercase()
  val colors = MaterialTheme.colorScheme
  val textColor = if (isSelected) colors.primary else colors.onSurfaceVariant

  Column(
      modifier = modifier.clickable { onSelected() },
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(2.dp)
                .background(
                    if (isSelected) colors.primary else Color.Transparent,
                ),
    )
    Text(
        modifier = Modifier.padding(vertical = 12.dp),
        text = destinationName,
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 2.sp,
            ),
        color = textColor,
        maxLines = 1,
        softWrap = false,
    )
  }
}

@Preview
@Composable
private fun PreviewMainNavigationBar() {
  val allDestinations = rememberAllDestinations()
  MainNavigationBar(
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
