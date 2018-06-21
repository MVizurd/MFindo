package com.vizurd.mfindo.dashboard.models

import android.arch.lifecycle.ViewModel
import com.vizurd.mfindo.core.di.DIHandler

class DashBoardViewModel : ViewModel() {


    override fun onCleared() {
        super.onCleared()
        DIHandler.destroyDashBoardComponent()
    }
}