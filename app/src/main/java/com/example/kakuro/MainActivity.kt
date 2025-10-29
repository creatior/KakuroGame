package com.example.kakuro

import Cell
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private var selectedCell: TextView? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

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

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = getColor(android.R.color.black)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_new_game -> {
                    drawerLayout.closeDrawers()
                    showNewGameDialog()
                    true
                }
                R.id.nav_how_to_play -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, HowToPlayActivity::class.java))
                    true
                }
                R.id.nav_leaderboard -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
        generateKakuroGrid(gridLayout, kakuroField)
        setupNumberPad()
    }


    private fun setupNumberPad() {
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

    private fun showNewGameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_game, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerSize)

        val sizes = (10..20).map { "${it}x${it}" }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizes)

        AlertDialog.Builder(this)
            .setTitle("Новая игра")
            .setView(dialogView)
            .setPositiveButton("Начать") { dialog, _ ->
                val selectedSize = spinner.selectedItem.toString()
                val selectedDifficulty = when (dialogView.findViewById<RadioGroup>(R.id.rgDifficulty).checkedRadioButtonId) {
                    R.id.rbEasy -> "Легкий"
                    R.id.rbMedium -> "Средний"
                    R.id.rbHard -> "Сложный"
                    R.id.rbVeryHard -> "Очень сложный"
                    else -> "Не выбрано"
                }

                Toast.makeText(this, "Выбрано: $selectedDifficulty, $selectedSize", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
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
                            selectedCell?.background = null
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
