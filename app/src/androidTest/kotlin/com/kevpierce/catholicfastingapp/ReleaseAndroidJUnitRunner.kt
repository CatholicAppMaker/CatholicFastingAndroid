package com.kevpierce.catholicfastingapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class ReleaseAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String?,
        context: Context,
    ): Application = super.newApplication(cl, TestCatholicFastingApplication::class.java.name, context)
}
