package com.vizurd.mfindo.core.di

import com.vizurd.mfindo.base.BaseApplication
import com.vizurd.mfindo.dashboard.di.DaggerDashBoardComponent
import com.vizurd.mfindo.dashboard.di.DashBoardComponent
import javax.inject.Singleton

@Singleton
object DIHandler {

    private var dashBoardComponent: DashBoardComponent? = null

    fun getDashBoardComponent(): DashBoardComponent {
        if (dashBoardComponent == null)
            dashBoardComponent = DaggerDashBoardComponent.builder().baseComponent(BaseApplication.baseComponent).build()
        return dashBoardComponent as DashBoardComponent
    }

    fun destroyDashBoardComponent() {
        dashBoardComponent = null
    }

}