package com.udacity.todolist.locationreminders

import androidx.test.core.app.ApplicationProvider
import com.udacity.todolist.locationreminders.data.FakeDataSource
import com.udacity.todolist.locationreminders.reminderslist.RemindersListViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

/*
val viewModelsModule =
    module {
        viewModel { RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            get() as FakeDataSource
        ) }
    }
val dataSourcesModule =
    module {
        single{ get() as FakeDataSource }
    }*/
