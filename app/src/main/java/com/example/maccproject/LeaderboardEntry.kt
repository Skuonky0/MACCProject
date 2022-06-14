package com.example.maccproject

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class LeaderboardEntry(elements: JSONArray?) {
    var size: Int = 0
    var array: MutableList<JSONObject> = mutableListOf()

    init {
        var i = 0
        if (elements != null) {
            while (!elements.isNull(i)){
                array.add(i,elements.getJSONObject(i))
                i++
            }
            size = i
        }
    }
}