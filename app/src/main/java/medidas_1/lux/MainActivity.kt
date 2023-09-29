package medidas_1.lux

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import medidas_1.lux.R


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mSensorManager : SensorManager
    private var mLux : Sensor ?= null

    private val canWriteSettings: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)

    private var enabled = true

    companion object {
        private const val REQUEST_CODE_WRITE_SETTINGS_PERMISSION = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Se piden los permisos de la aplicación
        if (this.canWriteSettings) {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } else {
            startManageWriteSettingsPermission()
        }

        // Se inicializa el sensor de luz
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLux = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (mLux != null) {
            // Lux sensor found!
            // Register the sensor listener
            mSensorManager.registerListener(this, mLux, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            val toast = Toast.makeText(this, "No light sensor (wtf?!). Terminating...", Toast.LENGTH_LONG)
            toast.show()
            finish()
            return
        }




        buttonEnable.setOnClickListener {
            if (this.enabled == true) {
                buttonEnable.text = "Enable"
                this.enabled = false
            }

            else {
                buttonEnable.text = "Disable"
                this.enabled = true
            }
        }
    }

    // Resto de estados de la App no se usan
    override fun onResume() {
        super.onResume()
        Log.i("customLOG", "Volviendo a arrancar!")
        mSensorManager.registerListener(this, mLux, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause() {
        super.onPause()
        Log.i("customLOG", "Entrando en pausa!")
        mSensorManager.unregisterListener(this)

    }

    // Función para leer el brillo actual de la pantalla. En desuso
    private fun readBrightness() : Int {
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
    }

    // Pedir permisos para escribir configuraciones del sistema
    private val writeSettingsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result when the user grants the permission.
                // You can perform your desired action here.
            } else {
                // Handle the result when the user denies the permission.
                // You can display a message or take appropriate action.
            }
        }

    private fun startManageWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${this.packageName}")
            ).let {
                writeSettingsPermissionLauncher.launch(it)
            }
        }
    }


    private fun changeBrightness(brightness : Int) {
        if (this.enabled == true) {

            val layoutpars: ViewGroup.LayoutParams
            layoutpars = this.getWindow().getAttributes()
            layoutpars.screenBrightness = brightness.toFloat() / 25
            getWindow().setAttributes(layoutpars)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            if (p0.sensor.type == Sensor.TYPE_LIGHT) {
                Log.i("SensorChanged", "Transition detected.")
                labelLight.text = p0.values[0].toString()
                labelBrightness.text = p0.values[0].toString()
                changeBrightness(p0.values[0].toInt())
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }


}