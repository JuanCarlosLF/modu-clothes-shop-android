package com.juancarloslf.modu.data.dataSource.remote.exception

interface ErrorHandler {
    fun handle(error: Exception) : Exception
}