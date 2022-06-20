package com.example.maccproject

import org.json.JSONArray
import org.json.JSONObject

class LeaderboardEntry(elements: JSONArray?) {
    var array: MutableList<JSONObject> = mutableListOf()

    init {
        var i = 0
        if (elements != null) {
            while (!elements.isNull(i)){
                array.add(i,elements.getJSONObject(i))
                i++
            }
        }
    }
}