package com.example.kakuro

import android.content.Intent
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val leaders = listOf(
        Leader("User1", "01:25"),
        Leader("User2", "01:40"),
        Leader("User3", "02:00"),
        Leader("User4", "02:10"),
        Leader("User5", "02:30")
    )

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
