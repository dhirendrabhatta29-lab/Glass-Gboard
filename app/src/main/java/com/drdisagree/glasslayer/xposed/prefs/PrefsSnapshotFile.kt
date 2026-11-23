package com.drdisagree.glasslayer.xposed.prefs

import android.util.AtomicFile
import java.io.File
import java.util.Optional
import org.json.JSONArray
import org.json.JSONObject

internal class PrefsSnapshotFile(dataDir: String) {

    private val file = AtomicFile(File(File(dataDir, FILES_DIR), FILE_NAME))

    fun exists(): Boolean = file.baseFile.exists()

    fun read(): Map<String, Optional<Any>>? {
        if (!exists()) return null
        val json = JSONObject(String(file.readFully(), Charsets.UTF_8))
        val result = HashMap<String, Optional<Any>>()
        val values = json.optJSONObject(VALUES) ?: JSONObject()
        values.keys().forEach { name ->
            decode(values.getJSONObject(name))?.let { result[name] = Optional.of(it) }
        }
        val removed = json.optJSONArray(REMOVED) ?: JSONArray()
        for (index in 0 until removed.length()) result[removed.getString(index)] = Optional.empty()
        return result
    }

    fun write(entries: Map<String, Optional<Any>>) {
        val values = JSONObject()
        val removed = JSONArray()
        entries.forEach { (name, value) ->
            val encoded = value.orElse(null)?.let(::encode)
            if (encoded == null) removed.put(name) else values.put(name, encoded)
        }
        val bytes = JSONObject().put(VALUES, values).put(REMOVED, removed).toString().toByteArray(Charsets.UTF_8)
        file.baseFile.parentFile?.mkdirs()
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (throwable: Throwable) {
            file.failWrite(stream)
            throw throwable
        }
    }

    private fun encode(value: Any): JSONObject? = when (value) {
        is Boolean -> typed(TYPE_BOOLEAN, value)
        is Float -> typed(TYPE_FLOAT, value.toDouble())
        is Int -> typed(TYPE_INT, value)
        is Long -> typed(TYPE_LONG, value)
        is String -> typed(TYPE_STRING, value)
        else -> null
    }

    private fun typed(type: String, value: Any): JSONObject = JSONObject().put(TYPE, type).put(VALUE, value)

    private fun decode(entry: JSONObject): Any? = when (entry.optString(TYPE)) {
        TYPE_BOOLEAN -> entry.getBoolean(VALUE)
        TYPE_FLOAT -> entry.getDouble(VALUE).toFloat()
        TYPE_INT -> entry.getInt(VALUE)
        TYPE_LONG -> entry.getLong(VALUE)
        TYPE_STRING -> entry.getString(VALUE)
        else -> null
    }
}

private const val FILES_DIR = "files"
private const val FILE_NAME = "glass_layer_prefs.json"
private const val VALUES = "values"
private const val REMOVED = "removed"
private const val TYPE = "type"
private const val VALUE = "value"
private const val TYPE_BOOLEAN = "boolean"
private const val TYPE_FLOAT = "float"
private const val TYPE_INT = "int"
private const val TYPE_LONG = "long"
private const val TYPE_STRING = "string"
