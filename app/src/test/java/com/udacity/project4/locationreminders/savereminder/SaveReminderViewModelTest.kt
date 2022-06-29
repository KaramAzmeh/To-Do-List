package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeLocalRepository
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class SaveReminderViewModelTest : KoinTest {


    private val saveReminderViewModel by inject<SaveReminderViewModel>()
    private val dataSource by inject<FakeLocalRepository>()
    private lateinit var appContext : Application

    val myModule = module {
        // define your module for test here
        viewModel {
            SaveReminderViewModel(
                appContext,
                get() as FakeLocalRepository
            )
        }

        single{ FakeLocalRepository() }

    }

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun startKoinForTest() {
        stopKoin()// stop the original app koin, which is launched when the application starts (in "MyApp")
        appContext = ApplicationProvider.getApplicationContext()

        startKoin {
            androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }
    }

    @After
    fun stopKoinAfterTest() = stopKoin()

    @Test
    fun saveReminder_noTitle_returnMessage() {

//        Given a reminder with incomplete information
        val reminder = ReminderDataItem(
            title = null,
            description = "Description1",
            location = "Location1",
            latitude = 24.2221,
            longitude = 44.5557
            )


//        When Reminders are saved
        mainCoroutineRule.runBlockingTest {
            saveReminderViewModel.validateAndSaveReminder(reminder)
        }

//         Then failed message is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun saveReminder_noLocation_returnMessage() {

//        Given a reminder with incomplete information
        val reminder = ReminderDataItem(
            title = "Title1",
            description = "Description1",
            location = null,
            latitude = null,
            longitude = null
        )


//        When Reminders are saved
        mainCoroutineRule.runBlockingTest {
            saveReminderViewModel.validateAndSaveReminder(reminder)
        }

//         Then failed message is shown
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location)
        )
    }

    @Test
    fun saveReminder_reminderAddedToDataSource() {

//        Given a reminder with incomplete information
        val reminder = ReminderDataItem(
            title = "Title1",
            description = "Description1",
            location = "Location1",
            latitude = 24.2221,
            longitude = 44.5557
        )

//        When Reminders are saved
        mainCoroutineRule.runBlockingTest {
            saveReminderViewModel.validateAndSaveReminder(reminder)

        }

//         Then failed message is shown
        MatcherAssert.assertThat(
            dataSource.reminders?.first{ it.id == reminder.id }, not(nullValue())
        )
    }

}