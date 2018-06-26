package com.vizurd.mfindo.handlers

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.util.*

class GeocodeWorkerThread(threadName: String) : HandlerThread(threadName) {

    private var handler: Handler? = null
    private var pendingTasks = PriorityQueue<Runnable>()

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = Handler(looper)
        pendingTasks.forEach { postTask(it) }
    }

    fun postTask(task: Runnable) {
        if (handler == null)
            pendingTasks.add(task)
        else handler?.post(task)

    }

}