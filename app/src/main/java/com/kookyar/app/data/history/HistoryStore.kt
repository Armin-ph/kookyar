package com.kookyar.app.data.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kookyar.app.data.model.MetronomePreset
import com.kookyar.app.data.model.TuningRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "kookyar_history")
private val Context.presetDataStore: DataStore<Preferences> by preferencesDataStore(name = "kookyar_presets")

private val json = Json { ignoreUnknownKeys = true }

/** Persists tuning sessions (capped) for the history & insights page. */
class HistoryStore(private val context: Context) {

    private object Keys {
        val RECORDS = stringPreferencesKey("records_json")
    }

    val records: Flow<List<TuningRecord>> = context.historyDataStore.data.map { p ->
        val raw = p[Keys.RECORDS] ?: return@map emptyList()
        runCatching {
            json.decodeFromString(ListSerializer(TuningRecord.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    suspend fun add(record: TuningRecord) {
        context.historyDataStore.edit { p ->
            val current = p[Keys.RECORDS]?.let {
                runCatching { json.decodeFromString(ListSerializer(TuningRecord.serializer()), it) }
                    .getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (listOf(record) + current).take(MAX_RECORDS)
            p[Keys.RECORDS] = json.encodeToString(ListSerializer(TuningRecord.serializer()), updated)
        }
    }

    suspend fun clear() {
        context.historyDataStore.edit { it.remove(Keys.RECORDS) }
    }

    companion object {
        private const val MAX_RECORDS = 500
    }
}

/** Saved metronome presets. */
class MetronomeStore(private val context: Context) {

    private object Keys {
        val PRESETS = stringPreferencesKey("presets_json")
    }

    val presets: Flow<List<MetronomePreset>> = context.presetDataStore.data.map { p ->
        val raw = p[Keys.PRESETS] ?: return@map emptyList()
        runCatching {
            json.decodeFromString(ListSerializer(MetronomePreset.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    suspend fun save(preset: MetronomePreset) {
        context.presetDataStore.edit { p ->
            val current = p[Keys.PRESETS]?.let {
                runCatching { json.decodeFromString(ListSerializer(MetronomePreset.serializer()), it) }
                    .getOrDefault(emptyList())
            } ?: emptyList()
            val updated = listOf(preset) + current.filterNot { it.id == preset.id }
            p[Keys.PRESETS] = json.encodeToString(ListSerializer(MetronomePreset.serializer()), updated.take(30))
        }
    }

    suspend fun delete(id: String) {
        context.presetDataStore.edit { p ->
            val current = p[Keys.PRESETS]?.let {
                runCatching { json.decodeFromString(ListSerializer(MetronomePreset.serializer()), it) }
                    .getOrDefault(emptyList())
            } ?: emptyList()
            p[Keys.PRESETS] = json.encodeToString(
                ListSerializer(MetronomePreset.serializer()),
                current.filterNot { it.id == id }
            )
        }
    }
}
