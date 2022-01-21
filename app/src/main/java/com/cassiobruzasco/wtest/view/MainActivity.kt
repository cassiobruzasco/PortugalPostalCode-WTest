package com.cassiobruzasco.wtest.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.cassiobruzasco.wtest.R
import com.cassiobruzasco.wtest.di.viewModels
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        setContentView(R.layout.activity_main)
        initKoin()
    }

    private fun initKoin() {
        val modules = mutableListOf(
            viewModels,
        )

        startKoin {
            androidContext(applicationContext)
            modules(modules)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopKoin()
    }
}