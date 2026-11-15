package com.pyamsoft.tetherfi.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Startup-path gate: MainActivity must launch and render without crashing.
 *
 * This exists because green CI and ordinary APK checks missed launch deaths
 * before (AutoSpace r159, Notscape beta.48, HowlingDog v1.3.0 VerifyError —
 * the oversized-composable class ART rejected at launch while the JVM
 * stayed green). The dex guard catches the register-count half of that
 * failure; this test catches the other half — the startup path itself
 * (TFApp.onCreate, the Dagger graph, the theme, the first composition).
 *
 * The test launches the real MainActivity under Robolectric: TFApp runs,
 * the ObjectGraph is built, permissions are registered, and the home
 * screen composes. If any of that throws, this test fails first, before
 * any device ever sees it.
 *
 * Pinned to emulated SDK 35: Robolectric's newer SDK shadows are new
 * enough that the pin costs nothing (the launch path is what is under
 * test) and avoids a failure mode unrelated to the app.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainActivityStartupTest {

  @Test
  fun activityLaunchesWithoutCrashing() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertNotNull(activity)
    assertFalse(activity.isFinishing)
    assertNotNull(activity.window)
    controller.destroy()
  }
}
