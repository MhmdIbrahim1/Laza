package com.example.laza.utils

import com.example.laza.data.Address

interface AllAddressesUtil {
    fun deleteItem(position:Int)
    fun editItem(address: Address, position: Int)
}