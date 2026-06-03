package com.example.studentapp

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

object AppConfig {
    const val BASE_URL = "https://dean-excuse-petal.ngrok-free.dev"

    fun getQueue(context: Context): RequestQueue {
        return Volley.newRequestQueue(context, NgrokStack())
    }
}