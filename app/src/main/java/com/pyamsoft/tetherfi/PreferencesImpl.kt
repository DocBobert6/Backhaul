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

package com.pyamsoft.tetherfi

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.pyamsoft.pydroid.core.LintIgnoreTooGenericExceptionCaught
import com.pyamsoft.pydroid.core.LintIgnoreTooManyFunctions
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tetherfi.core.AppCoroutineScope
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.server.ExpertPreferences
import com.pyamsoft.tetherfi.server.ProxyPreferences
import com.pyamsoft.tetherfi.server.ServerDefaults
import com.pyamsoft.tetherfi.server.ServerNetworkBand
import com.pyamsoft.tetherfi.server.ServerSocketTimeout
import com.pyamsoft.tetherfi.server.StatusPreferences
import com.pyamsoft.tetherfi.server.TweakPreferences
import com.pyamsoft.tetherfi.server.WifiPassword
import com.pyamsoft.tetherfi.server.WifiPreferences
import com.pyamsoft.tetherfi.server.broadcast.BroadcastType
import com.pyamsoft.tetherfi.server.network.PreferredNetwork
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
@LintIgnoreTooManyFunctions
internal class PreferencesImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    private val dispatchers: AppDispatchers,
    private val appScope: AppCoroutineScope,
    dataStore: DataStore<Preferences>,
) :
    StatusPreferences,
    ProxyPreferences,
    TweakPreferences,
    ExpertPreferences,
    WifiPreferences {

  private val preferences by lazy {
    onClearOldPreferences(dataStore)
    return@lazy dataStore
  }

  // Keep this lazy so that the fallback password is always the same
  private val fallbackPassword by lazy { WifiPassword.generate() }

  private fun onClearOldPreferences(store: DataStore<Preferences>) {
    appScope.launch(context = dispatchers.io) {
      store.edit { mutableStore ->
        for (key in OldKeys) {
          mutableStore.remove(key)
        }
      }
    }
  }

  private inline fun <T : Any> setPreference(
      key: Preferences.Key<T>,
      fallbackValue: T,
      crossinline value: suspend (Preferences) -> T,
  ) {
    appScope.launch(context = dispatchers.io) {
      try {
        preferences.edit { it[key] = value(it) }
      } catch (@LintIgnoreTooGenericExceptionCaught e: Throwable) {
        e.ifNotCancellation { preferences.edit { it[key] = fallbackValue } }
      }
    }
  }

  private fun <T : Any> getPreference(key: Preferences.Key<T>, value: T): Flow<T> =
      preferences.data
          .map { it[key] ?: value }
          // Otherwise any time ANY preference updates, ALL preferences will be
          // re-sent
          .distinctUntilChanged()
          .catch { err ->
            Timber.e(err) { "Error reading from dataStore: ${key.name}" }
            preferences.edit { it[key] = value }
            emit(value)
          }

  override fun listenForSsidChanges(): Flow<String> =
      getPreference(key = PreferenceKeys.SSID, value = ServerDefaults.WIFI_SSID)
          .flowOn(context = dispatchers.io)

  override fun setSsid(ssid: String) =
      setPreference(
          key = PreferenceKeys.SSID,
          fallbackValue = ServerDefaults.WIFI_SSID,
          value = { ssid },
      )

  override fun listenForPasswordChanges(): Flow<String> =
      getPreference(key = PreferenceKeys.PASSWORD, value = fallbackPassword)
          .flowOn(context = dispatchers.io)

  override fun setPassword(password: String) =
      setPreference(
          key = PreferenceKeys.PASSWORD,
          fallbackValue = fallbackPassword,
          value = { password },
      )

  override suspend fun repairPasswordIfInvalid(): String {
    val current = listenForPasswordChanges().first()
    if (WifiPassword.isValid(current)) {
      return current
    }

    // A stored password Android will refuse is treated as absent.
    val fresh = WifiPassword.generate()
    preferences.edit { it[PreferenceKeys.PASSWORD] = fresh }
    return fresh
  }

  override fun listenForHttpEnabledChanges(): Flow<Boolean> =
      getPreference(key = PreferenceKeys.IS_HTTP_ENABLED, value = DEFAULT_IS_HTTP_ENABLED)
          .flowOn(context = dispatchers.io)

  override fun setHttpEnabled(enabled: Boolean) =
      setPreference(
          key = PreferenceKeys.IS_HTTP_ENABLED,
          fallbackValue = DEFAULT_IS_HTTP_ENABLED,
          value = { enabled },
      )

  override fun listenForPortChanges(): Flow<Int> =
      getPreference(key = PreferenceKeys.PORT, value = ServerDefaults.HTTP_PORT)
          .flowOn(context = dispatchers.io)

  override fun setPort(port: Int) =
      setPreference(
          key = PreferenceKeys.PORT,
          fallbackValue = ServerDefaults.HTTP_PORT,
          value = { port },
      )

  override fun listenForSocksEnabledChanges(): Flow<Boolean> =
      getPreference(key = PreferenceKeys.IS_SOCKS_ENABLED, value = DEFAULT_IS_SOCKS_ENABLED)
          .flowOn(context = dispatchers.io)

  override fun setSocksEnabled(enabled: Boolean) =
      setPreference(
          key = PreferenceKeys.IS_SOCKS_ENABLED,
          fallbackValue = DEFAULT_IS_SOCKS_ENABLED,
          value = { enabled },
      )

  override fun listenForNetworkBandChanges(): Flow<ServerNetworkBand> =
      getPreference(
              key = PreferenceKeys.NETWORK_BAND,
              value = ServerDefaults.WIFI_NETWORK_BAND.name,
          )
          .map { ServerNetworkBand.valueOf(it) }
          .flowOn(context = dispatchers.io)

  override fun setNetworkBand(band: ServerNetworkBand) =
      setPreference(
          key = PreferenceKeys.NETWORK_BAND,
          fallbackValue = ServerDefaults.WIFI_NETWORK_BAND.name,
          value = { band.name },
      )

  override fun listenForStartIgnoreVpn(): Flow<Boolean> =
      getPreference(key = PreferenceKeys.START_IGNORE_VPN, value = DEFAULT_START_IGNORE_VPN)
          .flowOn(context = dispatchers.io)

  override fun setStartIgnoreVpn(ignore: Boolean) =
      setPreference(
          key = PreferenceKeys.START_IGNORE_VPN,
          fallbackValue = DEFAULT_START_IGNORE_VPN,
          value = { ignore },
      )

  override fun listenForStartIgnoreLocation(): Flow<Boolean> =
      getPreference(
              key = PreferenceKeys.START_IGNORE_LOCATION,
              value = DEFAULT_START_IGNORE_LOCATION,
          )
          .flowOn(context = dispatchers.io)

  override fun setStartIgnoreLocation(ignore: Boolean) =
      setPreference(
          key = PreferenceKeys.START_IGNORE_LOCATION,
          fallbackValue = DEFAULT_START_IGNORE_LOCATION,
          value = { ignore },
      )

  override fun listenForShutdownWithNoClients(): Flow<Boolean> =
      getPreference(
              key = PreferenceKeys.SHUTDOWN_NO_CLIENTS,
              value = DEFAULT_SHUTDOWN_NO_CLIENTS,
          )
          .flowOn(context = dispatchers.io)

  override fun setShutdownWithNoClients(shutdown: Boolean) =
      setPreference(
          key = PreferenceKeys.SHUTDOWN_NO_CLIENTS,
          fallbackValue = DEFAULT_SHUTDOWN_NO_CLIENTS,
          value = { shutdown },
      )

  override fun listenForWakeLock(): Flow<Boolean> =
      getPreference(key = PreferenceKeys.HOLD_WAKELOCK, value = DEFAULT_HOLD_WAKELOCK)
          .flowOn(context = dispatchers.io)

  override fun setWakeLock(wakelock: Boolean) =
      setPreference(
          key = PreferenceKeys.HOLD_WAKELOCK,
          fallbackValue = DEFAULT_HOLD_WAKELOCK,
          value = { wakelock },
      )

  override fun listenForKeepScreenOn(): Flow<Boolean> =
      getPreference(key = PreferenceKeys.KEEP_SCREEN_ON, value = DEFAULT_KEEP_SCREEN_ON)
          .flowOn(context = dispatchers.io)

  override fun setKeepScreenOn(keep: Boolean) =
      setPreference(
          key = PreferenceKeys.KEEP_SCREEN_ON,
          fallbackValue = DEFAULT_KEEP_SCREEN_ON,
          value = { keep },
      )

  override fun listenForBroadcastType(): Flow<BroadcastType> =
      getPreference(
              key = PreferenceKeys.BROADCAST_TYPE,
              value = BroadcastType.WIFI_DIRECT.name,
          )
          .map { BroadcastType.valueOf(it) }
          .flowOn(context = dispatchers.io)

  override fun setBroadcastType(type: BroadcastType) =
      setPreference(
          key = PreferenceKeys.BROADCAST_TYPE,
          fallbackValue = BroadcastType.WIFI_DIRECT.name,
          value = { type.name },
      )

  override fun listenForPreferredNetwork(): Flow<PreferredNetwork> =
      getPreference(key = PreferenceKeys.PREFERRED_NETWORK, value = PreferredNetwork.NONE.name)
          .map { PreferredNetwork.valueOf(it) }
          .flowOn(context = dispatchers.io)

  override fun setPreferredNetwork(network: PreferredNetwork) =
      setPreference(
          key = PreferenceKeys.PREFERRED_NETWORK,
          fallbackValue = PreferredNetwork.NONE.name,
          value = { network.name },
      )

  override fun listenForSocketTimeout(): Flow<ServerSocketTimeout> =
      getPreference(
              key = PreferenceKeys.SOCKET_TIMEOUT,
              value = ServerSocketTimeout.Defaults.BALANCED.timeoutDuration.inWholeSeconds,
          )
          .map { ServerSocketTimeout.create(it) }

  override fun setSocketTimeout(limit: ServerSocketTimeout) =
      setPreference(
          key = PreferenceKeys.SOCKET_TIMEOUT,
          fallbackValue = ServerSocketTimeout.Defaults.BALANCED.timeoutDuration.inWholeSeconds,
          value = {
            if (limit.timeoutDuration.isInfinite()) -1 else limit.timeoutDuration.inWholeSeconds
          },
      )

  companion object {

    private const val DEFAULT_IS_HTTP_ENABLED = true

    private const val DEFAULT_IS_SOCKS_ENABLED = false

    private const val DEFAULT_START_IGNORE_VPN = false

    private const val DEFAULT_START_IGNORE_LOCATION = false

    private const val DEFAULT_SHUTDOWN_NO_CLIENTS = false

    private const val DEFAULT_HOLD_WAKELOCK = false

    private const val DEFAULT_KEEP_SCREEN_ON = false

    private val OldKeys =
        listOf(
            // Server Limits
            intPreferencesKey("key_server_perf_limit_1"),
            // New Engine
            booleanPreferencesKey("key_new_engine_1"),
            // SOCKS specific port
            intPreferencesKey("key_socks_port_1"),
        )
  }
}
