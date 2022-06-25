package com.udacity.todolist

import android.app.Application
import android.util.Log
import com.udacity.todolist.locationreminders.data.FakeDataSource
import com.udacity.todolist.locationreminders.data.ReminderDataSource
import com.udacity.todolist.locationreminders.data.local.LocalDB
import com.udacity.todolist.locationreminders.data.local.RemindersLocalRepository
import com.udacity.todolist.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.todolist.locationreminders.savereminder.SaveReminderViewModel
import org.junit.Assert.*

import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/*
class MyAppTest: Application() {


    override fun onCreate() {
        super.onCreate()

        Log.i("MyAppTest", "MyApp Called")
        */
/**
         * use Koin Library as a service locator
         *//*

        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as FakeDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as FakeDataSource
                )
            }
//            single { RemindersLocalRepository(get()) as FakeDataSource }
            single { LocalDB.createRemindersDao(this@MyAppTest) }
        }

        startKoin {
            androidContext(this@MyAppTest)
            modules(listOf(myModule))
        }

    }

}*/
