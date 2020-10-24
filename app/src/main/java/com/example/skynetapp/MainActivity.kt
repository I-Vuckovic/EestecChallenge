package com.example.skynetapp

import android.content.DialogInterface
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuItem
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import ai.fritz.core.Fritz
import ai.fritz.vision.FritzVision
import ai.fritz.vision.FritzVisionImage
import ai.fritz.vision.ImageRotation
import ai.fritz.vision.imagelabeling.ImageLabelManagedModelFast
import ai.fritz.vision.PredictorStatusListener
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor
import android.net.Uri
import android.widget.TextView
import androidx.camera.core.*
import java.io.File
import java.text.SimpleDateFormat


// class MainActivity : AppCompatActivity() {
    

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)

//         ed1 = findViewById<View>(R.id.editText) as EditText
//         b1 = findViewById<View>(R.id.button) as Button


class MainActivity : AppCompatActivity() {
    var t1: TextToSpeech? = null
    var ed1: EditText? = null
    var b1: Button? = null

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Fritz.configure(this, "84e387eb963943cb9a6163e5a4642d9f");

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        camera_capture_button.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        //TTS
        t1 = TextToSpeech(applicationContext, OnInitListener { status ->
                    if (status != TextToSpeech.ERROR) {
                        t1!!.language = Locale.UK
                    }
                })

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

        imageCapture = ImageCapture.Builder()
            .build()
//        ==============================================================================================CLIENT PREDICTOR CODE===================================
//        val imageAnalyzer = ImageAnalysis.Builder()
//            .build()
//            .also {
//                it.setAnalyzer(cameraExecutor, ImageProcessor ())
//            }
//        ==============================================================================================CLIENT PREDICTOR CODE===================================
    }

    override fun onPause() {
        if (t1 != null) {
            t1!!.stop()
            t1!!.shutdown()
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })

        readText("I can read")
    }

    private fun readText(msg:String){
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        t1!!.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

//        ==============================================================================================CLIENT PREDICTOR CODE===================================
//    inner class ImageProcessor : ImageAnalysis.Analyzer {
//        var predictor: FritzVisionLabelPredictor? = null
//        val TAG = javaClass.simpleName
//
//        @SuppressLint("UnsafeExperimentalUsageError")
//        override fun analyze(image: ImageProxy) {
//
//            //Handle all the ML logic here
//            val mediaImage = image.image
//
//            val imageRotation = ImageRotation.getFromValue(image.imageInfo.rotationDegrees)
//
//            val visionImage = FritzVisionImage.fromMediaImage(mediaImage, imageRotation)
//
//            val managedModel = ImageLabelManagedModelFast()
//
//            FritzVision.ImageLabeling.loadPredictor(
//                managedModel,
//                object : PredictorStatusListener<FritzVisionLabelPredictor> {
//                    override fun onPredictorReady(p0: FritzVisionLabelPredictor?) {
//                        Log.d(TAG, "Image Labeling predictor is ready")
//                        predictor = p0
//                    }
//                })
//
//            val labelResult = predictor?.predict(visionImage)
//
//                labelResult?.resultString?.let {
//                    val sname = it.split(":")
//                    Log.e(TAG, it)
//                    Log.e(TAG, sname[0])
//                    println(sname[0])
//                    tv_name.text = sname[0]
//                } ?: kotlin.run {
//                    tv_name.visibility = TextView.INVISIBLE
//                }
//        }
//    }
//        ==============================================================================================CLIENT PREDICTOR CODE===================================

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
