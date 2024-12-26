package com.example.keramat_djati

data class Receipt(
    var title: String = "",
    var date: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var documentId: String? = null  // Include a field for the Firestore document ID
)
