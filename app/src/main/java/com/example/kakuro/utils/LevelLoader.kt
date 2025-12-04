package com.example.kakuro.utils

import Cell
import android.content.Context
import com.example.kakuro.models.Difficulty
import com.example.kakuro.models.LevelData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LevelLoader {
    private val gson = Gson()

    fun loadLevel(
        context: Context,
        difficulty: Difficulty,
        size: Int,
        levelIndex: Int = 0
    ): Array<Array<Cell>> {

        val json = context.assets.open(difficulty.fileName)
            .bufferedReader().use { it.readText() }

        val type = object : TypeToken<List<LevelData>>() {}.type
        val levels: List<LevelData> = gson.fromJson(json, type)

        // фильтруем по размеру
        val filtered = levels.filter { it.size == size }

        if (filtered.isEmpty()) {
            throw IllegalArgumentException("Error")
        }

        val level = filtered[levelIndex % filtered.size]

        return convertLevel(level)
    }

    private fun convertLevel(level: LevelData): Array<Array<Cell>> {
        val size = level.size
        val result = Array(size) { Array(size) { Cell(CellType.EMPTY) } }

        for (i in 0 until size) {
            for (j in 0 until size) {
                val cell = level.grid[i][j]
                result[i][j] = Cell(
                    type = cell.type,
                    right = cell.right,
                    down = cell.down,
                    value = 0,
                    color = 0
                )
            }
        }

        return result
    }
}