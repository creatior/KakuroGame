package com.example.kakuro.activities

import Cell
import com.example.kakuro.db.LeaderBoardDbHelper
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.kakuro.R
import com.example.kakuro.fragments.GameBoardFragment
import com.example.kakuro.models.Difficulty
import com.example.kakuro.utils.LevelLoader
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var kakuroField: Array<Array<Cell>>

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var tvTimer: TextView
    private val handler = Handler()
    private lateinit var timerRunnable: Runnable
    private var startTime: Long = 0

    private var gameBoardFragment: GameBoardFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Toolbar + Drawer ---
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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
        tvTimer.visibility = View.GONE

        val startButton = findViewById<Button>(R.id.btnStartGame)
        startButton.setOnClickListener {
            showNewGameDialog()
        }
    }

    // -----------------------------
    //   NEW GAME DIALOG
    // -----------------------------
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

                val selectedDifficulty = when (
                    dialogView.findViewById<RadioGroup>(R.id.rgDifficulty).checkedRadioButtonId
                ) {
                    R.id.rbEasy -> Difficulty.EASY
                    R.id.rbMedium -> Difficulty.MEDIUM
                    R.id.rbHard -> Difficulty.HARD
                    R.id.rbVeryHard -> Difficulty.VERY_HARD
                    else -> Difficulty.EASY
                }

                kakuroField = LevelLoader.loadLevel(this, selectedDifficulty, selectedSize)

                launchGameBoardFragment()

                setupNumberPad()
                startTimer()
                tvTimer.visibility = View.VISIBLE

                findViewById<Button>(R.id.btnStartGame).visibility = View.GONE

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_new_game), null)
            .show()
    }

    // -----------------------------
    //   FRAGMENT LAUNCH
    // -----------------------------
    private fun launchGameBoardFragment() {
        gameBoardFragment = GameBoardFragment.newInstance(kakuroField)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, gameBoardFragment!!)
            .commitNow()
    }

    // -----------------------------
    //   NUMBER PAD
    // -----------------------------
    private fun setupNumberPad() {
        val pad = findViewById<LinearLayout>(R.id.numberPad)
        pad.visibility = View.VISIBLE

        val numberButtons = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9
        )

        gameBoardFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? GameBoardFragment
        numberButtons.forEachIndexed { index, id ->
            val btn = findViewById<FrameLayout>(id)
            btn.setOnClickListener {
                val num = index + 1
                gameBoardFragment?.placeNumber(num)  // передаем число во фрагмент
            }
        }
    }

    // -----------------------------
    //   WIN DIALOG
    // -----------------------------
    fun onPuzzleSolved() {
        stopTimer()
        showWinDialog()
    }

    private fun showWinDialog() {
        val totalSeconds = getElapsedSeconds()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val db = LeaderBoardDbHelper(this)
        db.insertWin(getUsername(), totalSeconds)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.win_message))
            .setMessage(getString(R.string.win_time, minutes, seconds))
            .setPositiveButton(getString(R.string.ok_label), null)
            .show()
    }

    private fun getUsername(): String {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("username", "unknown") ?: "unknown"
    }

    // -----------------------------
    //   TIMER
    // -----------------------------
    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime()

        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                val sec = (elapsed / 1000).toInt()
                tvTimer.text = getString(R.string.timer_value, sec / 60, sec % 60)
                handler.postDelayed(this, 200)
            }
        }

        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    private fun getElapsedSeconds(): Int {
        return ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
    }
}
