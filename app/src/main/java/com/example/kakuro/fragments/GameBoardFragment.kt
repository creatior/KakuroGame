package com.example.kakuro.fragments

import Cell
import CellType
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.kakuro.R
import com.example.kakuro.activities.MainActivity
import com.example.kakuro.utils.Solver
import com.example.kakuro.models.Difficulty

class GameBoardFragment : Fragment() {

    private var kakuroField: Array<Array<Cell>> = arrayOf()
    private var activeCellView: TextView? = null
    private var activeModelCell: Cell? = null

    companion object {
        private const val ARG_FIELD = "field"

        fun newInstance(field: Array<Array<Cell>>): GameBoardFragment {
            val fragment = GameBoardFragment()
            val args = Bundle()
            args.putSerializable(ARG_FIELD, field)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kakuroField = requireArguments().getSerializable(ARG_FIELD) as Array<Array<Cell>>
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_game_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateBoard(view)
    }

    // Метод для вставки числа из MainActivity
    fun placeNumber(number: Int) {
        activeCellView?.text = number.toString()
        activeModelCell?.value = number

        if (Solver.isPuzzleSolved(kakuroField)) {
            (activity as? MainActivity)?.onPuzzleSolved()
        }
    }

    private fun generateBoard(root: View) {
        val grid = root.findViewById<GridLayout>(R.id.gameGrid)
        grid.removeAllViews()
        grid.rowCount = kakuroField.size
        grid.columnCount = kakuroField[0].size

        for (i in kakuroField.indices) {
            for (j in kakuroField[0].indices) {
                val cell = kakuroField[i][j]

                val cellView = FrameLayout(requireContext()).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        columnSpec = GridLayout.spec(j, 1f)
                        rowSpec = GridLayout.spec(i, 1f)
                        setMargins(2, 2, 2, 2)
                    }
                    setBackgroundColor(
                        if (cell.type == CellType.BLACK) 0xFF000000.toInt()
                        else 0xFFFFFFFF.toInt()
                    )
                }

                if (cell.type == CellType.WHITE) {
                    val text = TextView(requireContext()).apply {
                        textSize = 20f
                        gravity = Gravity.CENTER
                    }
                    cellView.addView(text)

                    cellView.setOnClickListener {
                        activeCellView?.background = null
                        text.setBackgroundResource(R.drawable.selected_cell_border)
                        activeCellView = text
                        activeModelCell = cell
                    }

                } else if (cell.right != null || cell.down != null) {
                    val text = TextView(requireContext()).apply {
                        textSize = 12f
                        setTextColor(0xFFFFFFFF.toInt())
                        gravity = Gravity.TOP or Gravity.END
                        text = buildString {
                            cell.right?.let { append("→$it") }
                            if (cell.down != null && cell.right != null) append("\n")
                            cell.down?.let { append("↓$it") }
                        }
                    }
                    cellView.addView(text)
                }

                grid.addView(cellView)
            }
        }
    }
}
