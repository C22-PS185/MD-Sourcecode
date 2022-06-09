package com.myteam.terapiin

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import com.myteam.terapiin.databinding.ActivityCameraPageBinding
import kotlinx.android.synthetic.main.activity_camera_page.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.json.JSONArray

class CameraPage: AppCompatActivity() {
    val nameRand = getRandomString(10)
    var execute = 0
    private lateinit var binding: ActivityCameraPageBinding
    private lateinit var cameraExecutor: ExecutorService


    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this@CameraPage, "Tidak mendapatkan Ijin.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        binding.switchCamera.setOnClickListener { switchCamera() }
        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        if (intent.getIntExtra("openmouth",0) == 1){
            Toast.makeText(this@CameraPage, "Terapi Buka Mulut Dipilih", Toast.LENGTH_SHORT).show()
            binding.poseImage.setImageResource(R.drawable.openmouth)
        } else if (intent.getIntExtra("puffcheek",0) == 2){
            Toast.makeText(this@CameraPage, "Terapi Pipi Kembung Dipilih", Toast.LENGTH_SHORT).show()
            binding.poseImage.setImageResource(R.drawable.puffcheck)
        } else if (intent.getIntExtra("showteeth",0) == 3){
            Toast.makeText(this@CameraPage, "Terapi Tunjuk Gigi Dipilih", Toast.LENGTH_SHORT).show()
            binding.poseImage.setImageResource(R.drawable.showteeth)
        } else if (intent.getIntExtra("smile",0) == 4){
            Toast.makeText(this@CameraPage, "Terapi Senyum Dipilih", Toast.LENGTH_SHORT).show()
            binding.poseImage.setImageResource(R.drawable.smileface)
        } else if (intent.getIntExtra("sneer",0) == 5) {
            Toast.makeText(this@CameraPage, "Terapi Sneer Dipilih", Toast.LENGTH_SHORT).show()
            binding.poseImage.setImageResource(R.drawable.sneer)
        } else {
            Toast.makeText(this@CameraPage, "Error Prediction", Toast.LENGTH_SHORT).show()
        }
        execute = execute + 1
        switchCamera.visibility = View.VISIBLE
        capture_button.visibility = View.VISIBLE
        btnUpload.visibility = View.INVISIBLE
        btnCancelUpload.visibility = View.INVISIBLE
        btnDoneUpload.visibility = View.INVISIBLE
        binding.imagePreview.setImageURI(null)
        if (execute<=1){
            val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
            builder.setTitle("Terapi.in Memberitahu")
            builder.setMessage("Ekspresikan Seperti Gambar Diatas...")
            builder.setIcon(R.drawable.logo)

            builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                dialog.dismiss()
            })
            val alertDialog:AlertDialog=builder.create()
            alertDialog.show()
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraPage,
                    "Gagal memunculkan kamera.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchCamera() {
        cameraSelector =
            if (cameraSelector.equals(CameraSelector.DEFAULT_BACK_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
    }

    fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraPage,
                        "Potret Gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                @SuppressLint("ResourceAsColor")
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    poseImage.setImageResource(0)
                    switchCamera.visibility = View.INVISIBLE
                    capture_button.visibility = View.INVISIBLE
                    binding.imagePreview.setImageURI(Uri.fromFile(photoFile))
                    binding.viewFinder.removeAllViews()
                    btnUpload.visibility = View.VISIBLE
                    btnCancelUpload.visibility = View.VISIBLE
                    btnUpload.setOnClickListener{
                        linearPG.bringToFront()
                        linearPG.visibility = View.VISIBLE
                        uploadFirebase()
                    }
                    btnCancelUpload.setOnClickListener{
                        startCamera()
                    }
                }

                fun uploadFirebase() {
                    val firebase = Firebase.storage("gs://api_face-1/")
                    val fireReference = firebase.reference
                    val  imageLoc = fireReference.child("androFrom/$nameRand")
                    val metaPhoto = StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build()

                    val refFile = imageLoc.putFile(Uri.fromFile(photoFile), metaPhoto)

                    refFile.addOnProgressListener{
                            fotoFile ->
                        val progress = (100.0 * fotoFile.bytesTransferred) / fotoFile.totalByteCount
                        Log.d("Proses", progress.toString())
                    }

                    refFile.addOnSuccessListener {
                            fotoFile ->
                        btnUpload.visibility = View.INVISIBLE
                        btnCancelUpload.visibility = View.INVISIBLE
                        Toast.makeText(this@CameraPage, "Upload Berhasil", Toast.LENGTH_SHORT).show()
                        linearPG.visibility = View.INVISIBLE
                        btnDoneUpload.visibility = View.VISIBLE
                        if (intent.getIntExtra("openmouth",0) == 1){
                            openMouthPredict()
                        } else if (intent.getIntExtra("puffcheek",0) == 2){
                            puffCheekPredict()
                        } else if (intent.getIntExtra("showteeth",0) == 3){
                            showTeethPredict()
                        } else if (intent.getIntExtra("smile",0) == 4){
                            smilePredict()
                        } else if (intent.getIntExtra("sneer",0) == 5) {
                            sneerPredict()
                        } else {
                            Toast.makeText(this@CameraPage, "Error Prediction", Toast.LENGTH_SHORT).show()
                        }
                    }

                    refFile.addOnFailureListener {
                            fotoFile ->
                        Toast.makeText(this@CameraPage, "Gagal Diupload", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun openMouthPredict() {
        val listPose = mutableListOf<String>()
        val listAkurasi = mutableListOf<Double>()
        val url = "http://34.101.178.227:8000/?file=$nameRand"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val data = response.toString()
                var jArray = JSONArray(data)
                for (i in 0..jArray.length()-1){
                    var jObject = jArray.getJSONObject(i)
                    var poseName = jObject.getString("poseName")
                    var value = jObject.getDouble("value")
                    listPose.add(poseName)
                    listAkurasi.add(value)
                }
                val openMouthAcc = listAkurasi[0]
                listAkurasi.sort()
                val higestAcc = listAkurasi[4]
                if (listAkurasi[4] > openMouthAcc){
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terap Buka Mulut Gagal! Akurasi Anda : $openMouthAcc | Akurasi Terbesar : $higestAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                } else {
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terapi Buka Mulut Berhasil! Akurasi : $openMouthAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                }
            }, Response.ErrorListener {
                Log.e("Errorku",it.toString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(20*1000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        queue.add(request)
    }

    private fun puffCheekPredict() {
        val listPose = mutableListOf<String>()
        val listAkurasi = mutableListOf<Double>()
        val url = "http://34.101.178.227:8000/?file=$nameRand"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val data = response.toString()
                var jArray = JSONArray(data)
                for (i in 0..jArray.length()-1){
                    var jObject = jArray.getJSONObject(i)
                    var poseName = jObject.getString("poseName")
                    var value = jObject.getDouble("value")
                    listPose.add(poseName)
                    listAkurasi.add(value)
                }
                val puffCheekAcc = listAkurasi[1]
                listAkurasi.sort()
                val higestAcc = listAkurasi[4]
                if (listAkurasi[4] > puffCheekAcc){
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terap Pipi Kembung Gagal! Akurasi Anda : $puffCheekAcc | Akurasi Terbesar : $higestAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                } else {
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terapi Pipi Kembung Berhasil! Akurasi : $puffCheekAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                }
            }, Response.ErrorListener {
                Log.e("Errorku",it.toString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(20*1000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        queue.add(request)
    }

    private fun showTeethPredict() {
        val listPose = mutableListOf<String>()
        val listAkurasi = mutableListOf<Double>()
        val url = "http://34.101.178.227:8000/?file=$nameRand"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val data = response.toString()
                var jArray = JSONArray(data)
                for (i in 0..jArray.length()-1){
                    var jObject = jArray.getJSONObject(i)
                    var poseName = jObject.getString("poseName")
                    var value = jObject.getDouble("value")
                    listPose.add(poseName)
                    listAkurasi.add(value)
                }
                val showTeethAcc = listAkurasi[2]
                listAkurasi.sort()
                val higestAcc = listAkurasi[4]
                if (listAkurasi[4] > showTeethAcc){
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terap Tunjuk Gigi Gagal! Akurasi Anda : $showTeethAcc | Akurasi Terbesar : $higestAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                } else {
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terapi Tunjuk Gigi Berhasil! Akurasi : $showTeethAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                }
            }, Response.ErrorListener {
                Log.e("Errorku",it.toString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(20*1000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        queue.add(request)
    }

    private fun smilePredict(){
        val listPose = mutableListOf<String>()
        val listAkurasi = mutableListOf<Double>()
        val url = "http://34.101.178.227:8000/?file=$nameRand"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val data = response.toString()
                var jArray = JSONArray(data)
                for (i in 0..jArray.length()-1){
                    var jObject = jArray.getJSONObject(i)
                    var poseName = jObject.getString("poseName")
                    var value = jObject.getDouble("value")
                    listPose.add(poseName)
                    listAkurasi.add(value)
                }
                val smileAcc = listAkurasi[3]
                listAkurasi.sort()
                val higestAcc = listAkurasi[4]
                if (listAkurasi[4] > smileAcc){
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terap Senyum Gagal! Akurasi Anda : $smileAcc | Akurasi Terbesar : $higestAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                } else {
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terapi Senyum Berhasil! Akurasi : $smileAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                }
            }, Response.ErrorListener {
                Log.e("Errorku",it.toString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(20*1000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        queue.add(request)
    }

    private fun sneerPredict() {
        val listPose = mutableListOf<String>()
        val listAkurasi = mutableListOf<Double>()
        val url = "http://34.101.178.227:8000/?file=$nameRand"
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val data = response.toString()
                var jArray = JSONArray(data)
                for (i in 0..jArray.length()-1){
                    var jObject = jArray.getJSONObject(i)
                    var poseName = jObject.getString("poseName")
                    var value = jObject.getDouble("value")
                    listPose.add(poseName)
                    listAkurasi.add(value)
                }
                val sneerAcc = listAkurasi[4]
                listAkurasi.sort()
                val higestAcc = listAkurasi[4]
                if (listAkurasi[4] > sneerAcc){
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terap Sneer Gagal! Akurasi Anda : $sneerAcc | Akurasi Terbesar : $higestAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                } else {
                    val builder : AlertDialog.Builder = AlertDialog.Builder(this@CameraPage)
                    builder.setTitle("Terapi.in Memberitahu Hasil Akurasimu")
                    builder.setMessage("Terapi Sneer Berhasil! Akurasi : $sneerAcc")
                    builder.setIcon(R.drawable.logo)

                    builder.setPositiveButton("Oke",DialogInterface.OnClickListener{ dialog,which ->
                        dialog.dismiss()
                        startActivity(Intent(this@CameraPage,TerapiMenu::class.java))
                    })
                    val alertDialog:AlertDialog=builder.create()
                    alertDialog.show()
                }
            }, Response.ErrorListener {
                Log.e("Errorku",it.toString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(20*1000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        queue.add(request)
    }

    private fun getRandomString(length : Int) : String
    {
        val allowedChars = ('A' .. 'Z') + ('a' .. 'z') + ('0' .. '9')
        return (1 .. length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}