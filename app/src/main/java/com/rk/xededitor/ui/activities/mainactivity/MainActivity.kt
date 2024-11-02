package com.rk.xededitor.ui.activities.mainactivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.ui.theme.KarbonTheme

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KarbonTheme {
            
            }
        }
    }
}