package com.example.keramat_djati

import android.Manifest
import android.os.Environment
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.*

class ReceiptDetailActivity : AppCompatActivity() {

    private lateinit var editTextTitle: TextInputEditText
    private lateinit var editTextDate: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSaveChanges: Button
    private lateinit var buttonDownloadPdf: Button
    private lateinit var progressBar: ProgressBar
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_detail)

        editTextTitle = findViewById(R.id.editTextTitleDetail)
        editTextDate = findViewById(R.id.editTextDateDetail)
        editTextDescription = findViewById(R.id.editTextDescriptionDetail)
        imageView = findViewById(R.id.imageViewDetail)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)
        buttonDownloadPdf = findViewById(R.id.buttonDownloadPdf)
        progressBar = findViewById(R.id.progressBarDetail)

        intent.extras?.let {
            editTextTitle.setText(it.getString("title"))
            editTextDate.setText(it.getString("date"))
            editTextDescription.setText(it.getString("description"))
            Glide.with(this).load(it.getString("imageUrl")).into(imageView)
            documentId = it.getString("documentId")
        }

        editTextDate.setOnClickListener { showDatePicker() }
        buttonSaveChanges.setOnClickListener { confirmBeforeUpdate() }
        buttonDownloadPdf.setOnClickListener { checkPermissionAndDownloadPdf() }
    }

    private fun checkPermissionAndDownloadPdf() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            downloadImageAndConvertToPdf()
        }
    }

    private fun downloadImageAndConvertToPdf() {
        val imageUrl = intent.getStringExtra("imageUrl")
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    createPdfFromBitmap(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun createPdfFromBitmap(bitmap: Bitmap) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        // Check if external storage is available
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfFile = File(downloadsFolder, "DownloadedImage.pdf")

            try {
                pdfDocument.writeTo(FileOutputStream(pdfFile))
                Toast.makeText(this, "PDF saved to ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to save PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                pdfDocument.close()
            }
        } else {
            Toast.makeText(this, "External storage is not available", Toast.LENGTH_LONG).show()
        }
    }


    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener { selection ->
            editTextDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(selection)))
        }
    }

    private fun confirmBeforeUpdate() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to save these changes?")
            .setPositiveButton("Yes") { _, _ -> updateReceipt() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateReceipt() {
        showLoading(true)
        val title = editTextTitle.text.toString()
        val date = editTextDate.text.toString()
        val description = editTextDescription.text.toString()

        val receiptUpdates: Map<String, Any> = hashMapOf(
            "title" to title,
            "date" to date,
            "description" to description
        )

        documentId?.let { docId ->
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                FirebaseFirestore.getInstance()
                    .collection("accounts")
                    .document(userId)
                    .collection("savedreceipt")
                    .document(docId)
                    .update(receiptUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Receipt updated successfully", Toast.LENGTH_SHORT).show()
                        finish()  // Optionally, finish the activity if update is successful
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update receipt: ${e.message}", Toast.LENGTH_LONG).show()
                        showLoading(false)
                    }
            } ?: run {
                Toast.makeText(this, "Authentication failed, user ID is null.", Toast.LENGTH_LONG).show()
                showLoading(false)
            }
        } ?: run {
            Toast.makeText(this, "Document ID is null, cannot perform update.", Toast.LENGTH_LONG).show()
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) ImageView.VISIBLE else ImageView.GONE
        buttonSaveChanges.isEnabled = !isLoading
        buttonDownloadPdf.isEnabled = !isLoading
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImageAndConvertToPdf()
        } else {
            Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show()
        }
    }
}
