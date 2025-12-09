package com.example.kakuro.activities

import com.example.kakuro.db.LeaderBoardDbHelper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.kakuro.R
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.DisposableEffect
import kotlin.math.sqrt

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = { TopBarWithBack { finish() } } // стрелка назад
                ) { padding ->
                    LeaderboardScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBack(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.leaderboard)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back),
                    contentDescription = stringResource(R.string.return_label)
                )
            }
        }
    )
}

data class Leader(val name: String, val time: String)

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val dbHelper = remember { LeaderBoardDbHelper(context) }
    var leaders by remember { mutableStateOf<List<Leader>>(emptyList()) }

    LaunchedEffect(Unit) {
        leaders = dbHelper.getTopLeaders()
    }

    val sensorManager = remember {
        context.getSystemService(SensorManager::class.java)
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var lastAccel by remember { mutableStateOf(0f) }
    var currentAccel by remember { mutableStateOf(0f) }
    var shake by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                lastAccel = currentAccel
                currentAccel = sqrt((x * x + y * y + z * z))

                val delta = currentAccel - lastAccel
                shake = shake * 0.9f + delta

                if (shake > 12f) {
                    dbHelper.clearLeaders()
                    leaders = emptyList()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(leaders) { index, leader ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${index + 1}. ${leader.name}", fontSize = 18.sp)
                Text(leader.time, fontSize = 18.sp)
            }
        }
    }
}

