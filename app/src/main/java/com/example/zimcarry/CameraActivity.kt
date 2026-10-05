package com.example.zimcarry

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.carryon.Decision
import com.example.carryon.Rules
import com.example.carryon.TfliteHelper
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var txtLabel: TextView
    private lateinit var txtConf: TextView
    private lateinit var txtDecision: TextView
    private lateinit var txtReason: TextView
    private lateinit var resultCard: MaterialCardView
    private lateinit var btnSnap: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var progress: ProgressBar

    private lateinit var tflite: TfliteHelper
    private var isProcessing = false

    private val camPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        txtLabel = findViewById(R.id.txtLabel)
        txtConf = findViewById(R.id.txtConf)
        txtDecision = findViewById(R.id.txtDecision)
        txtReason = findViewById(R.id.txtReason)
        resultCard = findViewById(R.id.resultCard)
        btnSnap = findViewById(R.id.btnSnap)
        btnBack = findViewById(R.id.btnBack)
        progress = findViewById(R.id.progress)

        tflite = TfliteHelper(this)

        btnBack.setOnClickListener { finish() }
        btnSnap.setOnClickListener { captureAndInfer() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            camPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureAndInfer() {
        if (isProcessing) return

        val bitmap: Bitmap? = previewView.bitmap
        if (bitmap == null) {
            Toast.makeText(this, "카메라가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        progress.visibility = View.VISIBLE
        btnSnap.isEnabled = false

        lifecycleScope.launch(Dispatchers.Default) {

            val (label, conf) = tflite.run(bitmap)
            val decision = Rules.decide(label)

            withContext(Dispatchers.Main) {

                txtLabel.text = "예측: $label"
                txtConf.text = "신뢰도: ${(conf * 100).toInt()}%"
                txtReason.text = "이유: ${decision.reason}"

                txtDecision.text = when (decision.tag) {
                    Decision.Tag.OK -> "결과: 기내 OK"
                    Decision.Tag.CHECKED -> "결과: 위탁 권장"
                    Decision.Tag.PROHIBITED -> "결과: 금지"
                }

                progress.visibility = View.GONE
                btnSnap.isEnabled = true
                isProcessing = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tflite.isInitialized) tflite.close()
    }
}
