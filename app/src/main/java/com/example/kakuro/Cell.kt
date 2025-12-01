enum class CellType { EMPTY, WHITE, BLACK }

data class Cell(
    val type: CellType,
    val right: Int? = null,
    val down: Int? = null,
    var color: Int? = 0,
    var value: Int? = 0,
)