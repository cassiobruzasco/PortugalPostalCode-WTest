package com.cassiobruzasco.wtest.di

import com.cassiobruzasco.wtest.view.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {
    viewModel { HomeViewModel() }
}