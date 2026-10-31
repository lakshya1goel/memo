package com.example.memo

import android.os.Bundle
import android.media.AudioManager
import com.example.memo.audio.GameAudio
import com.example.memo.game.MemoryGame
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.memo.ui.GameScreen
import com.example.memo.ui.theme.MemoTheme

class MainActivity : ComponentActivity() {
    private lateinit var gameAudio: GameAudio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        gameAudio = GameAudio(this)
        enableEdgeToEdge()
        setContent {
            MemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        modifier = Modifier.padding(innerPadding),
                        onCardSelected = { result ->
                            if (result != MemoryGame.SelectionResult.IGNORED) {
                                gameAudio.playTap()
                            }
                            when (result) {
                                MemoryGame.SelectionResult.MATCH -> gameAudio.playMatch()
                                MemoryGame.SelectionResult.MISMATCH -> gameAudio.playMiss()
                                else -> Unit
                            }
                        },
                        onRestart = { gameAudio.playTap() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameAudio.resume()
    }

    override fun onPause() {
        gameAudio.pause()
        super.onPause()
    }

    override fun onDestroy() {
        gameAudio.release()
        super.onDestroy()
    }
}
