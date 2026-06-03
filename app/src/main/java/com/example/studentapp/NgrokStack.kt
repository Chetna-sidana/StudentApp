package com.example.studentapp

import com.android.volley.toolbox.HurlStack
import java.net.HttpURLConnection
import java.net.URL

class NgrokStack : HurlStack() {
    override fun createConnection(url: URL): HttpURLConnection {
        val connection = super.createConnection(url)
        connection.setRequestProperty("ngrok-skip-browser-warning", "true")
        connection.setRequestProperty("User-Agent", "SkillApp/1.0")
        return connection
    }
}
