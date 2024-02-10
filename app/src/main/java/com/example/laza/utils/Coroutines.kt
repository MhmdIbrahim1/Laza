package com.example.laza.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.laza.R

import com.example.laza.mvvm.launchSafe
import com.example.laza.mvvm.logError
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.Collections.synchronizedList

object Coroutines {
    fun <T> T.main(work: suspend ((T) -> Unit)): Job {
        val value = this
        return CoroutineScope(Dispatchers.Main).launchSafe {
            work(value)
        }
    }

    fun <T> T.ioSafe(work: suspend (CoroutineScope.(T) -> Unit)): Job {
        val value = this

        return CoroutineScope(Dispatchers.IO).launchSafe {
            work(value)
        }
    }

    suspend fun <T, V> V.ioWorkSafe(work: suspend (CoroutineScope.(V) -> T)): T? {
        val value = this
        return withContext(Dispatchers.IO) {
            try {
                work(value)
            } catch (e: Exception) {
                logError(e)
                null
            }
        }
    }

    suspend fun <T, V> V.ioWork(work: suspend (CoroutineScope.(V) -> T)): T {
        val value = this
        return withContext(Dispatchers.IO) {
            work(value)
        }
    }

    suspend fun <T, V> V.mainWork(work: suspend (CoroutineScope.(V) -> T)): T {
        val value = this
        return withContext(Dispatchers.Main) {
            work(value)
        }
    }

    fun runOnMainThread(work: (() -> Unit)) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            work()
        }
    }

    /**
     * Safe to add and remove how you want
     * If you want to iterate over the list then you need to do:
     * synchronized(allProviders) { code here }
     **/
    fun <T> threadSafeListOf(vararg items: T): MutableList<T> {
        return synchronizedList(items.toMutableList())
    }
}

object CommonActivity{

    val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    val Float.toPx: Float get() = (this * Resources.getSystem().displayMetrics.density)

    private const val TAG = "COMPACT"
    private var currentToast: Toast? = null

    private var _activity: WeakReference<Activity>? = null
    var activity
        get() = _activity?.get()
        private set(value) {
            _activity = WeakReference(value)
        }
    @MainThread
    fun showToast(act: Activity?, message: String?, duration: Int? = null) {
        if (act == null || message == null) {
            Log.w(TAG, "invalid showToast act = $act message = $message")
            return
        }

        Log.i(TAG, "showToast = $message")

        try {
            currentToast?.cancel()
        } catch (e: Exception) {
            logError(e)
        }

        try {
            val inflater = LayoutInflater.from(act)
            val layout: View = inflater.inflate(
                R.layout.toast,
                act.findViewById(R.id.toast_layout_root) // Use findViewById directly on the activity
            )

            val text = layout.findViewById<TextView>(R.id.textt)
            text.text = message.trim()

            val toast = Toast(act)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 5.toPx)
            toast.duration = duration ?: Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()
            currentToast = toast
        } catch (e: Exception) {
            logError(e)
        }
    }
    fun showToast(@StringRes message: Int, duration: Int? = null) {
        val act = activity ?: return
        act.runOnUiThread {
            showToast(act, act.getString(message), duration)
        }
    }

    fun showToast(message: String?, duration: Int? = null) {
        val act = activity ?: return
        act.runOnUiThread {
            showToast(act, message, duration)
        }
    }

    fun showToast(message: UiText?, duration: Int? = null) {
        val act = activity ?: return
        if (message == null) return
        act.runOnUiThread {
            showToast(act, message.asString(act), duration)
        }
    }


    @MainThread
    fun showToast(act: Activity?, text: UiText, duration: Int) {
        if (act == null) return
        text.asStringNull(act)?.let {
            showToast(act, it, duration)
        }
    }

    /** duration is Toast.LENGTH_SHORT if null*/
    @MainThread
    fun showToast(act: Activity?, @StringRes message: Int, duration: Int? = null) {
        if (act == null) return
        showToast(act, act.getString(message), duration)
    }


}

sealed class UiText {
    companion object {
        const val TAG = "UiText"
    }

    data class DynamicString(val value: String) : UiText() {
        override fun toString(): String = value
    }

    class StringResource(
        @StringRes val resId: Int,
        val args: List<Any>
    ) : UiText() {
        override fun toString(): String =
            "resId = $resId\nargs = ${args.toList().map { "(${it::class} = $it)" }}"
    }

    fun asStringNull(context: Context?): String? {
        try {
            return asString(context ?: return null)
        } catch (e: Exception) {
            Log.e(TAG, "Got invalid data from $this")
            logError(e)
            return null
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val str = context.getString(resId)
                if (args.isEmpty()) {
                    str
                } else {
                    str.format(*args.map {
                        when (it) {
                            is UiText -> it.asString(context)
                            else -> it
                        }
                    }.toTypedArray())
                }
            }
        }
    }
}
