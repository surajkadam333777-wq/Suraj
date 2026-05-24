package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.FashionAtelierScreen
import com.example.ui.FashionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: FashionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full-edge content drawing matching status/nav insets
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                FashionAtelierScreen(viewModel = viewModel)
            }
        }
    }
}
