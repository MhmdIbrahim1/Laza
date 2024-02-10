package com.example.laza.mvvm

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.bumptech.glide.load.HttpException
import kotlinx.coroutines.*
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

const val DEBUG_EXCEPTION = "THIS IS A DEBUG EXCEPTION!"
const val DEBUG_PRINT = "DEBUG PRINT"

class DebugException(message: String) : Exception("$DEBUG_EXCEPTION\n$message")


fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { it?.let { t -> action(t) } }
}

fun <T> LifecycleOwner.observeNullable(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { action(it) }
}

sealed class Resource<out T> {
    data class Success<out T>(val value: T) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorResponse: Any?, //ResponseBody
        val errorString: String,
    ) : Resource<Nothing>()

    data class Loading(val url: String? = null) : Resource<Nothing>()
}

fun logError(throwable: Throwable) {
    Log.d("ApiError", "-------------------------------------------------------------------")
    Log.d("ApiError", "safeApiCall: " + throwable.localizedMessage)
    Log.d("ApiError", "safeApiCall: " + throwable.message)
    throwable.printStackTrace()
    Log.d("ApiError", "-------------------------------------------------------------------")
}

fun <T> normalSafeApiCall(apiCall: () -> T): T? {
    return try {
        apiCall.invoke()
    } catch (throwable: Throwable) {
        logError(throwable)
        return null
    }
}

suspend fun <T> suspendSafeApiCall(apiCall: suspend () -> T): T? {
    return try {
        apiCall.invoke()
    } catch (throwable: Throwable) {
        logError(throwable)
        return null
    }
}
fun <T> safeFail(throwable: Throwable): Resource<T> {
    val stackTraceMsg = throwable.getStackTracePretty()
    return Resource.Failure(false, null, null, stackTraceMsg)
}


fun Throwable.getAllMessages(): String {
    return (this.localizedMessage ?: "") + (this.cause?.getAllMessages()?.let { "\n$it" } ?: "")
}

fun Throwable.getStackTracePretty(showMessage: Boolean = true): String {
    val prefix = if (showMessage) this.localizedMessage?.let { "\n$it" } ?: "" else ""
    return prefix + this.stackTrace.joinToString(
        separator = "\n"
    ) {
        "${it.fileName} ${it.lineNumber}"
    }
}



fun CoroutineScope.launchSafe(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val obj: suspend CoroutineScope.() -> Unit = {
        try {
            block()
        } catch (throwable: Throwable) {
            logError(throwable)
        }
    }

    return this.launch(context, start, obj)
}



