package ru.aleksandr.dccppthrottle.store

import org.json.JSONArray

interface JsonStoreInterface {
    var hasUnsavedData: Boolean
    fun toJson() : JSONArray
    fun fromJson(jsonArray: JSONArray, sortOrder: String? = null)
}