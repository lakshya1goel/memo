package com.example.memo.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.memo.ui.theme.MemoTheme

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Memory Game",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    MemoTheme() {
        GameScreen()
    }
}