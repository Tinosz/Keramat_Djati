package com.example.keramat_djati

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateWalletViewModel : ViewModel() {
    val walletName = MutableLiveData<String>()
    val walletAmount = MutableLiveData<Long>()
}
