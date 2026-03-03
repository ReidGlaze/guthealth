package com.twintipsolutions.guthealth

import android.app.Application
import com.google.firebase.FirebaseApp

class GutHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
