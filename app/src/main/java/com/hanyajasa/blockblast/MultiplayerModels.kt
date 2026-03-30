package com.hanyajasa.blockblast

import org.json.JSONArray
import org.json.JSONObject

sealed class GameEvent {
    data class ScoreUpdate(val playerName: String, val score: Int) : GameEvent()
    data class OpponentGridUpdate(val grid: List<List<Long>>) : GameEvent()

    fun toJson(): String {
        val json = JSONObject()
        when (this) {
            is ScoreUpdate -> {
                json.put("type", "SCORE_UPDATE")
                json.put("playerName", playerName)
                json.put("score", score)
            }
            is OpponentGridUpdate -> {
                json.put("type", "GRID_UPDATE")
                val gridArray = JSONArray()
                grid.forEach { row ->
                    val rowArray = JSONArray()
                    row.forEach { cell -> rowArray.put(cell) }
                    gridArray.put(rowArray)
                }
                json.put("grid", gridArray)
            }
        }
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): GameEvent? {
            return try {
                val json = JSONObject(jsonStr)
                when (json.getString("type")) {
                    "SCORE_UPDATE" -> ScoreUpdate(
                        json.getString("playerName"),
                        json.getInt("score")
                    )
                    "GRID_UPDATE" -> {
                        val gridArray = json.getJSONArray("grid")
                        val grid = List(gridArray.length()) { r ->
                            val rowArray = gridArray.getJSONArray(r)
                            List(rowArray.length()) { c -> rowArray.getLong(c) }
                        }
                        OpponentGridUpdate(grid)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
