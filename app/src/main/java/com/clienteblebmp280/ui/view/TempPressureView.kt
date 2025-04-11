package com.clienteblebmp280.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clienteblebmp280.ui.viewmodel.TempPressureViewModel


@Composable
fun TempPressureScreen() {
    // Aquí viewModel() automáticamente obtiene el TempPressureViewModel gracias a Hilt
    val viewModel: TempPressureViewModel = viewModel()

    // Recoger los valores de temperatura y presión desde el ViewModel
    val temperature by viewModel.temperatureFlow.collectAsState()
    val pressure by viewModel.pressureFlow.collectAsState()
    val error by viewModel.errorFlow.collectAsState()
    // Contenido de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            // Mostrar el error si ocurrió
            Text(
                text = "Error: $error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text(
            text = "Temperatura: ${temperature?.let { "$it °C" } ?: "Cargando..."}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Presión: ${pressure?.let { "$it hPa" } ?: "Cargando..."}",
            style = MaterialTheme.typography.headlineMedium
        )

        // Botón para reiniciar el escaneo en caso de error o reconexión
        Button(
            onClick = { viewModel.restartScan() }, // Corregido el uso de viewModel
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Reiniciar escaneo")
        }
    }
}
