import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kakuro.Leader

class LeaderBoardDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "game.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "wins"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_TIME = "time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_TIME INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertWin(username: String, time: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_TIME, time)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getTopLeaders(limit: Int = 10): List<Leader> {
        val leaders = mutableListOf<Leader>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_USERNAME, COLUMN_TIME),
            null,
            null,
            null,
            null,
            "$COLUMN_TIME ASC",
            limit.toString()
        )

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val timeSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                val minutes = timeSeconds / 60
                val seconds = timeSeconds % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                leaders.add(Leader(name, timeFormatted))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return leaders
    }

}
