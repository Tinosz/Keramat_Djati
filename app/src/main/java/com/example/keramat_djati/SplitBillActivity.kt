package com.example.keramat_djati

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class SplitBillActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var processButton: Button
    private var capturedImageBitmap: Bitmap? = null
    private lateinit var startCamera: ActivityResultLauncher<Intent>

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill)

        imageView = findViewById(R.id.image_view)
        captureButton = findViewById(R.id.button_capture)
        processButton = findViewById(R.id.button_process_image)

        setupActivityResultLauncher()

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera()
            }
        }

        processButton.setOnClickListener {
            capturedImageBitmap?.let { bitmap ->
                processImage(bitmap)
            }
        }
    }

    private fun setupActivityResultLauncher() {
        startCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                capturedImageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageView.setImageBitmap(capturedImageBitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCamera.launch(cameraIntent)
    }

    private fun processImage(image: Bitmap) {
        val inputImage = InputImage.fromBitmap(image, 0)
        val options = TextRecognizerOptions.Builder().build()
        val recognizer = TextRecognition.getClient(options)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", "Recognized text: ${visionText.text}")
                val blocks = convertVisionTextToTextBlocks(visionText)

                // Preprocess OCR blocks before passing to item formatting
                val processedItems = TextProcessingUtils.processReceipt(blocks)

                // Send receiptItems to SplitBillDisplayActivity
                val intent = Intent(this, SplitBillDisplayActivity::class.java).apply {
                    putParcelableArrayListExtra("receiptItems", ArrayList(processedItems))  // Pass the list of items
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error recognizing text: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertVisionTextToTextBlocks(visionText: com.google.mlkit.vision.text.Text): List<TextBlock> {
        val textBlocks = mutableListOf<TextBlock>()
        for (textBlock in visionText.textBlocks) {
            val frame = textBlock.boundingBox
            frame?.let {
                textBlocks.add(
                    TextBlock(
                        text = textBlock.text,
                        x = frame.left,
                        y = frame.top,
                        width = frame.width(),
                        height = frame.height()
                    )
                )
            }
        }
        return textBlocks
    }
}
