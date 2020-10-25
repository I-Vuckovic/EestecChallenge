package com.example.skynetapp

import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.*
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import ai.fritz.core.Fritz
import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.camera.core.*
//import com.example.skynetapp.api.APIInterface
//import com.example.skynetapp.api.UploadRequestBody
//import com.example.skynetapp.api.uploadResponse
//import com.mvp.handyopinion.UploadUtility
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.wait
//import retrofit2.Call
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
//import retrofit2.Callback
//import retrofit2.Response
import android.app.Activity
import android.app.ProgressDialog
import android.icu.util.TimeUnit
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide


// class MainActivity : AppCompatActivity() {
    

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)

//         ed1 = findViewById<View>(R.id.editText) as EditText
//         b1 = findViewById<View>(R.id.button) as Button


@SuppressLint("WrongViewCast")
class MainActivity : AppCompatActivity()/*, UploadRequestBody.UploadCallback*/ {
    var t1: TextToSpeech? = null
   // setContentView(R.id)
   // var pgsBar = findViewById<ProgressBar>(R.id.pBar)

    var dialog: ProgressDialog? = null
    var serverURL: String = "http://89.39.144.160:5000/submit"
    var serverUploadDirectoryPath: String = "http://89.39.144.160:5000/submit"
    val client = OkHttpClient.Builder().connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var pgsBar = findViewById<View>(R.id.pBar) as? ProgressBar
        //pgsBar!!.onVisibilityAggregated(true)

        showGif()

        Fritz.configure(this, "84e387eb963943cb9a6163e5a4642d9f")

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewFinder.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        //TTS
        t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
                    if (status != TextToSpeech.ERROR) {
                        t1!!.language = Locale.UK
                    }
                })

    }

//    override fun onStart() {
//        super.onStart()
//        readText("Press anywhere to recognize an object")
//    }

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

    private fun takePhoto() { //pgsBar:ProgressBar
        readText("Please wait while the object gets recognized")
        //pgsBar!!.onVisibilityAggregated(true)
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
                    uploadFile(photoFile, photoFile.name)
                }
            })

        //readText("I can read")
    }

    fun showGif() {
        val imageView: ImageView = findViewById(R.id.imageView)
        Glide.with(this).load("https://media4.giphy.com/media/3oEjI6SIIHBdRxXI40/giphy.gif").into(imageView)
    }

    public fun readText(msg:String){
        //Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
//    private fun uploadImage(file: File) {
//        val body = UploadRequestBody(file, "image", this)
//        APIInterface().uploadImage(
//            MultipartBody.Part.createFormData(
//                "image",
//                file.name,
//                body
//            )
//        ).enqueue(object : Callback<uploadResponse> {
//            override fun onFailure(call: Call<uploadResponse>, t: Throwable) {
//            }
//            override fun onResponse(
//                call: Call<uploadResponse>,
//                response: Response<uploadResponse>
//            ) {
//                response.body()?.let {
//                    Log.i("RISPONSE", it.toString())
//                }
//            }
//        })
//
//    }

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



    fun uploadFile(sourceFile: File, uploadedFileName: String? = null) {
        Thread {
            val mimeType = getMimeType(sourceFile);
            if (mimeType == null) {
                Log.e("file error", "Not able to get mime type")
                return@Thread
            }
            val fileName: String = if (uploadedFileName == null)  sourceFile.name else uploadedFileName
            try {
                val requestBody: RequestBody =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName,sourceFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                        .build()

                val request: Request = Request.Builder().url(serverURL).post(requestBody).build()

                val response: Response = client.newCall(request).execute()

                response.body?.string()?.let { readText(it+". Press anywhere to recognize another object") }

                if (response.isSuccessful) {
                    Log.d("File upload","success, path: $serverUploadDirectoryPath$fileName")
                    //readText("Press anywhere to recognize an object")
                } else {
                    Log.e("File upload", "failed")
                }
                response.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("File upload", "failed")
            }
        }.start()


    }

    // url = file path or whatever suitable URL you want.
    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}
