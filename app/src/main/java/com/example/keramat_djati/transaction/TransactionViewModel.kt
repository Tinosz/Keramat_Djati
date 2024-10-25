package com.example.keramat_djati.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionViewModel : ViewModel() {
    val walletName = MutableLiveData<String>()
    val walletId = MutableLiveData<String>()
    val categoryType = MutableLiveData<String>()
    val category = MutableLiveData<String>()
    val categoryId = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val amount = MutableLiveData<Long>()
    val date = MutableLiveData<String>()
    val note = MutableLiveData<String>()
}
