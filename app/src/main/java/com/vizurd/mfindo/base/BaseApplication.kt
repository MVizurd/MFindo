package com.vizurd.mfindo.base

import android.app.Application
import com.vizurd.mfindo.core.Constants
import com.vizurd.mfindo.core.di.AppModule
import com.vizurd.mfindo.core.di.BaseComponent
import com.vizurd.mfindo.core.di.DaggerBaseComponent
import com.vizurd.mfindo.core.di.NetModule

class BaseApplication : Application() {

    companion object {
        lateinit var baseComponent: BaseComponent
    }

    override fun onCreate() {
        super.onCreate()
        initDI()
    }

    private fun initDI() {
        baseComponent = DaggerBaseComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule(Constants.BASE_URL))
                .build()
    }

}