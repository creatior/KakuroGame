package com.example.kakuro

import Cell
import LeaderBoardDbHelper
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.gridlayout.widget.GridLayout
import com.example.kakuro.Solver.isPuzzleSolved
import com.google.android.material.navigation.NavigationView
import com.example.kakuro.R

class MainActivity : AppCompatActivity() {
    private var selectedCell: TextView? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var kakuroField: Array<Array<Cell>>
    private var selectedModelCell: Cell? = null

    private lateinit var tvTimer: TextView
    private val handler = android.os.Handler()
    private lateinit var timerRunnable: Runnable
    private var startTime: Long = 0

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
                R.id.nav_settings -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        tvTimer = findViewById(R.id.tvTimer)
        tvTimer.text = getString(R.string.start_timer)
        tvTimer.visibility = View.GONE

        val startButton = findViewById<Button>(R.id.btnStartGame)
        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)

        startButton.setOnClickListener {
            showNewGameDialog()
        }
    }


    private fun setupNumberPad() {
        val numberPad = findViewById<LinearLayout>(R.id.numberPad)
        numberPad.visibility = View.VISIBLE

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
                val num = index + 1
                selectedCell?.text = num.toString()
                selectedModelCell?.value = num

                if (isPuzzleSolved(kakuroField)) {
                    stopTimer()
                    showWinDialog()
                }
            }

        }
    }

    private fun showNewGameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_game, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerSize)

        val sizes = listOf(9).map { "${it}x${it}" }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizes)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.new_game))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.start_game_button)) { dialog, _ ->
                val selectedSize = spinner.selectedItem.toString().substringBefore("x").toInt()

                val selectedDifficulty = when (dialogView.findViewById<RadioGroup>(R.id.rgDifficulty).checkedRadioButtonId) {
                    R.id.rbEasy -> Difficulty.EASY
                    R.id.rbMedium -> Difficulty.MEDIUM
                    R.id.rbHard -> Difficulty.HARD
                    R.id.rbVeryHard -> Difficulty.VERY_HARD
                    else -> Difficulty.EASY
                }

                val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
                val startButton = findViewById<Button>(R.id.btnStartGame)

                startButton.visibility = View.GONE
                gridLayout.visibility = View.VISIBLE

                kakuroField = LevelLoader.loadLevel(this, selectedDifficulty, selectedSize)
                generateKakuroGrid(gridLayout, kakuroField)
                setupNumberPad()
                tvTimer.visibility = View.VISIBLE
                startTimer()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_new_game), null)
            .show()
    }

    private fun showWinDialog() {
        val totalSeconds = getElapsedSeconds()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val username = getUsername()

        val dbHelper = LeaderBoardDbHelper(this)
        dbHelper.insertWin(username, totalSeconds)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.win_message))
            .setMessage(getString(R.string.win_time,minutes, seconds))
            .setPositiveButton(getString(R.string.ok_label), null)
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

                    if (cell.type == CellType.BLACK && (cell.right != null || cell.down != null)) {
                        val text = TextView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                            setTextColor(Color.WHITE)
                            textSize = 12f
                            gravity = android.view.Gravity.TOP or android.view.Gravity.END
                            text = buildString {
                                cell.right?.let { append("→$it") }
                                if (cell.down != null && cell.right != null) append("\n")
                                cell.down?.let { append("↓$it") }
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
                            selectedModelCell = cell
                        }
                    }
                }

                gridLayout.addView(cellView)
            }
        }
    }

    private fun getUsername(): String {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("username", "unknown") ?: "unknown"
    }

    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime()
        tvTimer.visibility = View.VISIBLE

        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                val totalSeconds = (elapsedMillis / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                tvTimer.text = getString(R.string.timer_value, minutes, seconds)
                handler.postDelayed(this, 200)
            }
        }
        handler.post(timerRunnable)
    }

    private fun getElapsedSeconds(): Int {
        val elapsedMillis = SystemClock.elapsedRealtime() - startTime
        return (elapsedMillis / 1000).toInt()
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }
}
