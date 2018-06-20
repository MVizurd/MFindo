package com.vizurd.mfindo.base

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

@SuppressLint("Registered")
class BaseActivity : AppCompatActivity() {


    fun addFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean) {
        val tag = fragment.javaClass.simpleName
        val fragmentTransaction = supportFragmentManager.beginTransaction()
                .add(containerId, fragment, tag)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack("add$tag")
        }
        fragmentTransaction.commit()
    }
}