package com.example.keramat_djati

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
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
    private lateinit var selectGalleryButton: Button
    private var capturedImageBitmap: Bitmap? = null
    private lateinit var startCamera: ActivityResultLauncher<Intent>
    private lateinit var startGallery: ActivityResultLauncher<Intent>

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
        const val REQUEST_STORAGE_PERMISSION = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill)

        imageView = findViewById(R.id.image_view)
        captureButton = findViewById(R.id.button_capture)
        processButton = findViewById(R.id.button_process_image)
        selectGalleryButton = findViewById(R.id.button_select_gallery)

        setupActivityResultLaunchers()

        captureButton.setOnClickListener {
            // Check if camera permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                openCamera()  // If already granted, open camera
            }
        }

        selectGalleryButton.setOnClickListener {
            // Check if storage permission is granted
            val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(storagePermission), REQUEST_STORAGE_PERMISSION)
            } else {
                openGallery()  // If already granted, open gallery
            }
        }

        processButton.setOnClickListener {
            capturedImageBitmap?.let { bitmap ->
                processImage(bitmap)
            }
        }
    }

    private fun setupActivityResultLaunchers() {
        startCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                capturedImageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageView.setImageBitmap(capturedImageBitmap)
            }
        }

        startGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    capturedImageBitmap = bitmap
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    // Requesting permission for camera and gallery
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startCamera.launch(cameraIntent)
    }

    @SuppressLint("IntentReset")
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startGallery.launch(galleryIntent)
    }

    private fun processImage(image: Bitmap) {
        val inputImage = InputImage.fromBitmap(image, 0)
        val options = TextRecognizerOptions.Builder().build()
        val recognizer = TextRecognition.getClient(options)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", "Recognized text: ${visionText.text}")
                val blocks = convertVisionTextToTextBlocks(visionText)

                // Format the grouped text blocks to receipt items
                val receiptItems = TextProcessingUtils.processReceipt(blocks)

                // Send receiptItems to SplitBillDisplayActivity
                val intent = Intent(this, SplitBillDisplayActivity::class.java).apply {
                    putParcelableArrayListExtra("receiptItems", ArrayList(receiptItems))  // Pass the list of items
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