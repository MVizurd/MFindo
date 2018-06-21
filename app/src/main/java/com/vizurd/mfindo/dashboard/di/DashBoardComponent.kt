package com.vizurd.mfindo.dashboard.di

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.vizurd.mfindo.core.di.BaseComponent
import com.vizurd.mfindo.dashboard.DashBoardFragment
import dagger.Component
import dagger.Module
import dagger.Provides

@DashBoardScope
@Component(dependencies = [BaseComponent::class], modules = [DashBoardModule::class])
interface DashBoardComponent {
    fun inject(dashBoardFragment: DashBoardFragment)
}

@DashBoardScope
@Module
class DashBoardModule {

    @DashBoardScope
    @Provides
    fun getFusedLocationProviderClient(context: Context) = LocationServices.getFusedLocationProviderClient(context)


}