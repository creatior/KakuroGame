package com.example.kakuro

import Cell

object Solver {

    fun isPuzzleSolved(field: Array<Array<Cell>>): Boolean {

        val rows = field.size
        val cols = field[0].size

        // Проверка горизонтальных групп
        for (i in 0 until rows) {
            for (j in 0 until cols) {

                val cell = field[i][j]

                if (cell.type == CellType.BLACK && cell.right != null) {

                    val target = cell.right
                    val nums = mutableListOf<Int>()

                    var x = j + 1
                    while (x < cols && field[i][x].type == CellType.WHITE) {
                        val v = field[i][x].value
                        if (v == null) return false       // есть пустая клетка → головоломка НЕ решена
                        nums.add(v)
                        x++
                    }

                    val set = nums.toSet()
                    // проверка уникальности
                    if (nums.size != set.size) return false

                    // проверка суммы
                    if (nums.sum() != target) return false
                }
            }
        }

        // Проверка вертикальных групп
        for (i in 0 until rows) {
            for (j in 0 until cols) {

                val cell = field[i][j]

                if (cell.type == CellType.BLACK && cell.down != null) {

                    val target = cell.down
                    val nums = mutableListOf<Int>()

                    var y = i + 1
                    while (y < rows && field[y][j].type == CellType.WHITE) {
                        val v = field[y][j].value
                        if (v == null) return false
                        nums.add(v)
                        y++
                    }

                    if (nums.size != nums.toSet().size) return false
                    if (nums.sum() != target) return false
                }
            }
        }

        return true
    }
}
