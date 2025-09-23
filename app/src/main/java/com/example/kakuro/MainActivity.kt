package com.example.kakuro

import Cell
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {
    private var selectedCell: TextView? = null

    private val kakuroField: Array<Array<Cell>> = arrayOf(
        arrayOf(Cell(CellType.BLACK), Cell(CellType.BLACK, clueDown = 16), Cell(CellType.BLACK, clueDown = 24), Cell(CellType.BLACK, clueDown = 17), Cell(CellType.BLACK)),
        arrayOf(Cell(CellType.BLACK, clueRight = 23), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE)),
        arrayOf(Cell(CellType.BLACK, clueRight = 30), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE)),
        arrayOf(Cell(CellType.BLACK, clueRight = 27), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE), Cell(CellType.WHITE)),
        arrayOf(Cell(CellType.BLACK), Cell(CellType.BLACK, clueRight = 12), Cell(CellType.BLACK, clueDown = 10), Cell(CellType.BLACK, clueRight = 7), Cell(CellType.BLACK))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
        generateKakuroGrid(gridLayout, kakuroField)

        // Подключаем numberPad
        val numberButtons = listOf(
            findViewById<FrameLayout>(R.id.btn1),
            findViewById<FrameLayout>(R.id.btn2),
            findViewById<FrameLayout>(R.id.btn3),
            findViewById<FrameLayout>(R.id.btn4),
            findViewById<FrameLayout>(R.id.btn5),
            findViewById<FrameLayout>(R.id.btn6),
            findViewById<FrameLayout>(R.id.btn7),
            findViewById<FrameLayout>(R.id.btn8),
            findViewById<FrameLayout>(R.id.btn9)
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedCell?.text = (index + 1).toString()
            }
        }
    }

    private fun generateKakuroGrid(gridLayout: GridLayout, field: Array<Array<Cell>>) {
        gridLayout.removeAllViews()
        val rows = field.size
        val cols = field[0].size
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val cell = field[i][j]

                val cellView = FrameLayout(gridLayout.context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        columnSpec = GridLayout.spec(j, 1f)
                        rowSpec = GridLayout.spec(i, 1f)
                        setMargins(2, 2, 2, 2)
                    }

                    setBackgroundColor(if (cell.type == CellType.BLACK) Color.BLACK else Color.WHITE)
                    isClickable = cell.type == CellType.WHITE
                    isFocusable = cell.type == CellType.WHITE

                    if (cell.type == CellType.BLACK && (cell.clueRight != null || cell.clueDown != null)) {
                        val text = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            setTextColor(Color.WHITE)
                            textSize = 12f
                            gravity = android.view.Gravity.TOP or android.view.Gravity.END
                            text = buildString {
                                cell.clueDown?.let { append("↓$it") }
                                if (cell.clueDown != null && cell.clueRight != null) append("\n")
                                cell.clueRight?.let { append("→$it") }
                            }
                        }
                        addView(text)
                    } else if (cell.type == CellType.WHITE) {
                        val text = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            gravity = android.view.Gravity.CENTER
                            textSize = 20f
                            setTextColor(Color.BLACK)
                        }
                        addView(text)

                        setOnClickListener {
                            // снимаем выделение с предыдущей клетки
                            selectedCell?.background = null
                            // выделяем текущую
                            text.setBackgroundResource(R.drawable.selected_cell_border)
                            selectedCell = text
                        }
                    }
                }

                gridLayout.addView(cellView)
            }
        }
    }

}