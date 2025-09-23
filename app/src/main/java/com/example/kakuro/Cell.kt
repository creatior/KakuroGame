enum class CellType { WHITE, BLACK }

data class Cell(
    val type: CellType,
    val clueRight: Int? = null,
    val clueDown: Int? = null
)