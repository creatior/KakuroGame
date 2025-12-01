package com.example.kakuro

enum class Difficulty(val fileName: String) {
    EASY("levels_easy.json"),
    MEDIUM("levels_medium.json"),
    HARD("levels_hard.json"),
    VERY_HARD("levels_very_hard.json")
}