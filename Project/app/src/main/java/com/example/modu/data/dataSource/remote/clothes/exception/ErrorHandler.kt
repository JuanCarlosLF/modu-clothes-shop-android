package com.example.modu.data.dataSource.remote.clothes.exception

interface ErrorHandler {
    fun handle(error: Exception) : Exception
}