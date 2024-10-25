package com.example.keramat_djati

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.ImageAnnotatorSettings
import com.google.cloud.vision.v1.Image
import com.google.auth.oauth2.GoogleCredentials
import com.google.protobuf.ByteString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class VisionApiManager(context: Context) {

    private val client: ImageAnnotatorClient

    init {
        // Load the credentials from the JSON file directly
        val credentialsPath = copyCredentialsToInternalStorage(context)
        val credentialsStream = FileInputStream(credentialsPath)
        val googleCredentials = GoogleCredentials.fromStream(credentialsStream)

        // Use the credentials to configure the Vision API client
        val settings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider { googleCredentials }
            .build()

        client = ImageAnnotatorClient.create(settings)
    }

    private fun copyCredentialsToInternalStorage(context: Context): String {
        val fileName = "kramat-djati-b2c10-f893c0773c47.json"  // Your JSON file name
        val inputStream: InputStream = context.assets.open(fileName)
        val outputFile = File(context.filesDir, fileName)

        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.absolutePath
    }

    fun prepareImage(bitmap: Bitmap): Image {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val imageBytes = ByteString.copyFrom(stream.toByteArray())
        return Image.newBuilder().setContent(imageBytes).build()
    }

    fun detectText(image: Bitmap, callback: (List<String>?, Exception?) -> Unit) {
        try {
            val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
            val request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(prepareImage(image))
                .build()

            val response = client.batchAnnotateImages(listOf(request))
            val texts = response.responsesList.flatMap { it.textAnnotationsList.map { it.description } }
            if (texts.isEmpty()) {
                callback(null, RuntimeException("No text detected"))
            } else {
                callback(texts, null)
            }
        } catch (e: Exception) {
            Log.e("VisionApiManager", "Error during text detection: ${e.localizedMessage}")
            callback(null, e)
        }
    }



    fun close() {
        client.close()
    }
}
