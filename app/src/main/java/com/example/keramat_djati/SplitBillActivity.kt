package com.example.keramat_djati

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition

class SplitBillActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var processButton: Button
    private var capturedImageBitmap: Bitmap? = null

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
        const val REQUEST_IMAGE_CAPTURE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill)

        imageView = findViewById(R.id.image_view)
        captureButton = findViewById(R.id.button_capture)
        processButton = findViewById(R.id.button_process_image)

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera()
            }
        }

        processButton.setOnClickListener {
            capturedImageBitmap?.let {
                processImage(it)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    openCamera()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            capturedImageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(capturedImageBitmap)
        }
    }

    private fun processImage(image: Bitmap) {
        val image = InputImage.fromBitmap(image, 0)
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val intent = Intent(this, SplitBillDisplayActivity::class.java)
                intent.putExtra("recognizedText", visionText.text)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error recognizing text: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
