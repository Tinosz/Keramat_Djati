package com.example.keramat_djati

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.DateFormat
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts


class AddReceiptActivity : AppCompatActivity() {

    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextDate: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSave: Button
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    private val storageReference = FirebaseStorage.getInstance().getReference()
    private val firestoreReference = FirebaseFirestore.getInstance()

    private val pickImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleImageResult(result.data?.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDate = findViewById(R.id.editTextDate)
        editTextDescription = findViewById(R.id.editTextDescription)
        imageView = findViewById(R.id.imageView)
        buttonSave = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBar)

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

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonSave.isEnabled = !show  // Disable the save button while loading
    }

    private fun checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            } else {
                pickImage()
            }
        } else {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageResultLauncher.launch(intent)
    }

    private fun handleImageResult(uri: Uri?) {
        imageUri = uri
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.baseline_error_24)
            .error(R.drawable.baseline_error_24)
            .into(imageView)
    }

    private fun uploadImageAndSaveReceipt() {
        showLoading(true)
        imageUri?.let { uri ->
            val fileName = "images/${FirebaseAuth.getInstance().currentUser?.uid}/${System.currentTimeMillis()}"
            val fileRef = storageReference.child(fileName)
            fileRef.putFile(uri).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    saveReceipt(imageUrl)
                }.addOnFailureListener { exception ->
                    showError(exception.message)
                    showLoading(false)
                }
            }.addOnFailureListener { exception ->
                showError(exception.message)
                showLoading(false)
            }
        } ?: run {
            Toast.makeText(this, "No image selected to upload.", Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
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
                    finish()  // Close this activity and go back
                }.addOnFailureListener { exception ->
                    showError(exception.message)
                    showLoading(false)
                }
        }
    }

    private fun showError(message: String?) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            editTextDate.setText(DateFormat.getDateInstance().format(Date(selection)))
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(this, "Permission denied to read your External storage, cannot pick images", Toast.LENGTH_LONG).show()
            }
        }
    }
}
