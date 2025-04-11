package com.clienteblebmp280.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clienteblebmp280.data.ble.BLTClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempPressureViewModel @Inject constructor(private val client: BLTClient) : ViewModel() {

    // Exponer los StateFlow de temperatura y presión desde BLTClient
    val temperatureFlow: StateFlow<Float?> = client.temperatureFlow
    val pressureFlow: StateFlow<Float?> = client.pressureFlow

    // StateFlow para manejar errores
    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> get() = _errorFlow

    // Iniciar el escaneo al crear el ViewModel
    init {
        startScan()
    }

    private fun startScan() {
        viewModelScope.launch {
            try {
                client.startScan()  // Aquí ya estamos usando correctamente la propiedad 'client'
            } catch (e: Exception) {
                // Manejar cualquier error y exponerlo a la UI
                _errorFlow.value = "Error al iniciar el escaneo: ${e.message}"
            }
        }
    }

    // Mét\do para reiniciar el escaneo o reconectar
    fun restartScan() {
        viewModelScope.launch {
            _errorFlow.value = null  // Limpiar el estado de error
            startScan()
        }
    }
}
