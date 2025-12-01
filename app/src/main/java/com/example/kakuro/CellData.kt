package com.example.kakuro

import CellType


data class CellData(
    val type: CellType,
    val right: Int? = null,
    val down: Int? = null
)