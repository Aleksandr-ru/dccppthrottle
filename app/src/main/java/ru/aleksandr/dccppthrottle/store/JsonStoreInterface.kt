/*
 * Copyright (c) 2023. Aleksandr.ru
 * @link http://aleksandr.ru
 *
 * If you're using this code, please keep above information.
 */

package ru.aleksandr.dccppthrottle.store

import org.json.JSONArray

interface JsonStoreInterface {
    var hasUnsavedData: Boolean
    fun toJson() : JSONArray
    fun fromJson(jsonArray: JSONArray, sortOrder: String? = null)
}