package com.spoolsense.shared.data.repository

import com.spoolsense.shared.data.prefs.SettingsStorage
import com.spoolsense.shared.domain.repository.ActiveSpoolRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ActiveSpoolRepositoryImpl(
    private val storage: SettingsStorage
) : ActiveSpoolRepository {

    private val _flow = MutableStateFlow<String?>(loadInitial(storage))

    override suspend fun getActiveSpoolId(): String? = _flow.value

    override suspend fun setActiveSpoolId(id: String?) {
        storage.putString(KEY_ACTIVE_SPOOL, id ?: "")
        _flow.value = id
    }

    override fun observeActiveSpoolId(): Flow<String?> = _flow.asStateFlow()

    private fun loadInitial(storage: SettingsStorage): String? =
        storage.getString(KEY_ACTIVE_SPOOL, "").takeIf { it.isNotBlank() }

    private companion object {
        const val KEY_ACTIVE_SPOOL = "active_spool_id"
    }
}
