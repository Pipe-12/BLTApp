package com.clienteblebmp280

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clienteblebmp280.ui.theme.ClienteBLEBMP280Theme
import com.clienteblebmp280.ui.view.TempPressureScreen

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            ClienteBLEBMP280Theme {
                TempPressureScreen()  // Hilt se encarga de inyectar el ViewModel automáticamente
            }
        }
    }
}
