package medidas_1.lux

import android.app.Service
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

object LightSensorManager :
    HandlerThread("LightSensorManager"), SensorEventListener {
    private val TAG: String = "LightSensorManager"
    private var sensorManager: SensorManager? = null
    private var handler: Handler? = null
    private var sensor: Sensor? = null
    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    init {
        sensorManager = MyApplication.getApplicationContext().getSystemService(Service.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorThread = HandlerThread(TAG, Thread.NORM_PRIORITY)
        sensorThread!!.start()
        sensorHandler = Handler(sensorThread!!.getLooper())
        sensorManager!!.registerListener(
            this,
            sensor, SensorManager.SENSOR_DELAY_FASTEST,
            sensorHandler
        )
    }

    fun setHandler(handler: Handler){
        this.handler = handler
    }

    // Se usa una recta para tranformar los lux en brillo.
    private fun luxToBrightness(lux: Float): Int {
        var brightness = (255.0 / 500.0 * lux).toInt()
        if (brightness > 255) { brightness = 255}

        return brightness;
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "" + accuracy)
    }

    // Al recibir un nuevo dato de luz, convertirlo en brillo y enviar ambos a la actividad principal.
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.values.isNotEmpty()) {
            var lux = event.values[0]
            var brightness = this.luxToBrightness(lux);
            var msg = Pair(lux, brightness)
            this.handler?.obtainMessage(0, msg)?.apply { sendToTarget() }
        }
    }

    }



