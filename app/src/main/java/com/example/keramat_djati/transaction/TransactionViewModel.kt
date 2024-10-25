package com.example.keramat_djati.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewModel : ViewModel() {
    val walletName = MutableLiveData<String>()
    val walletId = MutableLiveData<String>()
    val categoryType = MutableLiveData<String>()
    val category = MutableLiveData<String>()
    val categoryId = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val amount = MutableLiveData<Long>()
    val date = MutableLiveData<String>()
    val time = MutableLiveData<String>()
    val note = MutableLiveData<String>()
    val originalCategoryType = MutableLiveData<String>()  // Original type for comparison
    val transactionId = MutableLiveData<String?>()

    fun setCurrentDateTime() {
        val currentDateTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        date.value = dateFormat.format(currentDateTime)
        time.value = timeFormat.format(currentDateTime)
    }
}
