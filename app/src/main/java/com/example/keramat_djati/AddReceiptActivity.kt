package com.example.keramat_djati

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddReceiptActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDate: EditText
    private lateinit var imageView: ImageView
    private lateinit var editTextDescription: EditText
    private lateinit var buttonSave: Button
    private var imageUri: Uri? = null

    private val storageReference = FirebaseStorage.getInstance().getReference()
    private val firestoreReference = FirebaseFirestore.getInstance()

    private val pickImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDate = findViewById(R.id.editTextDate)
        imageView = findViewById(R.id.imageView)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonSave = findViewById(R.id.buttonSave)

        imageView.setOnClickListener { checkPermissionAndPickImage() }
        buttonSave.setOnClickListener {
            if (imageUri != null) {
                uploadImageAndSaveReceipt()
            } else {
                saveReceipt() // Save without an image URL
            }
        }

        editTextDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            } else {
                pickImage()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            } else {
                pickImage()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageResultLauncher.launch(intent)
    }

    private fun uploadImageAndSaveReceipt() {
        imageUri?.let { uri ->
            val fileName = "images/${FirebaseAuth.getInstance().currentUser?.uid}/${System.currentTimeMillis()}"
            val fileRef = storageReference.child(fileName)
            fileRef.putFile(uri).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    Log.d("UploadSuccess", "Image uploaded successfully to Firebase Storage: $imageUrl")
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    saveReceipt(imageUrl)
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "No image selected to upload.", Toast.LENGTH_SHORT).show()
    }

    private fun saveReceipt(imageUrl: String = "") {
        val title = editTextTitle.text.toString()
        val date = editTextDate.text.toString()
        val description = editTextDescription.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val receipt = hashMapOf(
            "title" to title,
            "date" to date,
            "description" to description,
            "imageUrl" to imageUrl
        )

        userId?.let {
            firestoreReference.collection("accounts").document(it)
                .collection("savedreceipt").add(receipt)
                .addOnSuccessListener {
                    Toast.makeText(this, "Receipt saved successfully", Toast.LENGTH_LONG).show()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to save receipt: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            editTextDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                pickImage()
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
