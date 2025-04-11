package com.clienteblebmp280.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothCentralManagerCallback
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.BluetoothPeripheralCallback
import com.welie.blessed.GattStatus
import com.welie.blessed.HciStatus
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BLTClient @Inject constructor(@ApplicationContext private val context: Context){
    // Crear un HandlerThread para ejecución en segundo plano
    private val bluetoothHandler = Handler(Looper.getMainLooper())
    // Flow para actualizar la UI automáticamente
    private val _temperatureFlow = MutableStateFlow<Float?>(null)
    val temperatureFlow = _temperatureFlow.asStateFlow()

    private val _pressureFlow = MutableStateFlow<Float?>(null)
    val pressureFlow = _pressureFlow.asStateFlow()

    // Callback para manejar eventos del escaneo
    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback = object : BluetoothCentralManagerCallback() {
        override fun onDiscovered(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            Timber.i("Found peripheral '${peripheral.name}' with RSSI ${scanResult.rssi}")
            bluetoothManager.stopScan()
            // Conectar al periférico
            connectToPeripheral(peripheral)
        }

        override fun onConnected(peripheral: BluetoothPeripheral) {
            Timber.i("Connected to '${peripheral.name}'")
        }

        override fun onDisconnected(peripheral: BluetoothPeripheral, status: HciStatus) {
            Timber.i("Disconnected '${peripheral.name}'")
            bluetoothHandler.postDelayed(
                { bluetoothManager.autoConnect(peripheral, peripheralCallback) },
                15000
            )
        }

        override fun onConnectionFailed(peripheral: BluetoothPeripheral, status: HciStatus) {
            Timber.e("Failed to connect to '${peripheral.name}'")
        }

        override fun onBluetoothAdapterStateChanged(state: Int) {
            Timber.i("Bluetooth adapter changed state to $state")
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth está encendido, iniciar el escaneo nuevamente
                bluetoothManager.startPairingPopupHack()
                startScan()
            }
        }
    }

    // Inicialización del BluetoothCentralManager en un hilo en segundo plano
    private val bluetoothManager :BluetoothCentralManager = BluetoothCentralManager(context, bluetoothCentralManagerCallback, bluetoothHandler)

    // Nomblre dispositivo UUID caracteristicas
    private val SERVICEUUID :UUID = UUID.fromString("91bad492-b950-4226-aa2b-4ede9fa42f59")
    private val TEMPUUID:UUID = UUID.fromString("cba1d466-344c-4be3-ab3f-189f80dd7518")
    private val PRESSUUID:UUID = UUID.fromString("ca73b3ba-39f6-4ab3-91ae-186dc9577d99")

    // Iniciar escaneo de dispositivos en segundo plano
    fun startScan() {
        Timber.i("Starting BLE scan for peripherals with service $SERVICEUUID")
        bluetoothHandler.post {
            bluetoothManager.scanForPeripheralsWithServices(setOf(SERVICEUUID))
        }
    }

    // Conectar a un periférico en segundo plano
    private fun connectToPeripheral(peripheral: BluetoothPeripheral) {
        bluetoothHandler.post {
            bluetoothManager.connect(peripheral, peripheralCallback)
        }
    }

    // Callback para manejar eventos del periférico (conectado, datos recibidos, etc.)
    private val peripheralCallback = object : BluetoothPeripheralCallback() {
        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            Timber.i("Discovered services for peripheral ${peripheral.name}")
            peripheral.services.forEach { service ->
                Timber.i("Service UUID: ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid == TEMPUUID || characteristic.uuid == PRESSUUID) {
                        Timber.i("Characteristic UUID: ${characteristic.uuid}  ")
                        peripheral.startNotify(
                            serviceUUID = service.uuid,
                            characteristicUUID = characteristic.uuid
                        )
                    }
                }
            }
            super.onServicesDiscovered(peripheral)
        }

        override fun onCharacteristicUpdate(
            peripheral: BluetoothPeripheral,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic,
            status: GattStatus
        ) {
            if (status == GattStatus.SUCCESS) {
                when (characteristic.uuid) {
                    TEMPUUID -> {
                        val temperature = parseTemperature(value)
                        _temperatureFlow.value = temperature
                        Timber.i("Temperatura recibida: $temperature°C")
                    }
                    PRESSUUID -> {
                        val pressure = parsePressure(value)
                        _pressureFlow.value = pressure
                        Timber.i("Presión recibida: $pressure hPa")
                    }
                }
            }
        }

    }

    // Convierte el valor recibido de BLE a un número flotante
    private fun parseTemperature(value: ByteArray): Float {
        return try {
            val tempString = value.decodeToString().trim() // Convertir ByteArray a String y limpiar espacios
            tempString.toFloat() // Convertir la cadena a Float
        } catch (e: NumberFormatException) {
            Timber.e(e, "Error al parsear la temperatura")
            -1.0f // Valor por defecto en caso de error
        }
    }

    private fun parsePressure(value: ByteArray): Float {
        return try {
            val pressureString = value.decodeToString().trim() // Convertir ByteArray a String y limpiar espacios
            pressureString.toFloat() // Convertir la cadena a Float
        } catch (e: NumberFormatException) {
            Timber.e(e, "Error al parsear la presión")
            -1.0f // Valor por defecto en caso de error
        }
    }

}