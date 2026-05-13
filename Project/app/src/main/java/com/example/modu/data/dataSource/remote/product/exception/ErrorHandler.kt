package com.example.modu.data.dataSource.remote.product.exception

interface ErrorHandler {
    fun handle(error: Exception) : Exception
}