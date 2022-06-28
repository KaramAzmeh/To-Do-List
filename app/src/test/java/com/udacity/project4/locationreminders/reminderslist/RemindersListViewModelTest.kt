package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.*
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
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
import com.udacity.project4.locationreminders.data.dto.Result

import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : KoinTest {


    private val remindersListViewModel by inject<RemindersListViewModel>()
    private val dataSource by inject<FakeDataSource>()
    private lateinit var appContext : Application

/*    val viewModelsModule =
        module {
            viewModel { RemindersListViewModel(
                appContext,
                dataSource
            ) }
        }
    val dataSourcesModule =
        module {
            single{ FakeDataSource() }
        }*/

    val myModule = module {
        // define your module for test here
        viewModel {
            RemindersListViewModel(
                appContext,
                get() as FakeDataSource
            )
        }

        single{ FakeDataSource() }

    }

    // Subject under test
//    private val remindersListViewModel: RemindersListViewModel by viewModel()

    // Use a fake data source to be injected into the viewmodel

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
/*
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(myModule)
    }*/

    @Before fun startKoinForTest() {
        stopKoin()// stop the original app koin, which is launched when the application starts (in "MyApp")
        appContext = ApplicationProvider.getApplicationContext()

        startKoin {
            androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }


/*     // We initialise the tasks to 3, with one active and two completed
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 10.0110, 20.5000)
        val reminder2 = ReminderDTO("Title2", "Description2", "location2", 40.0110, 30.5000)
        val reminder3 = ReminderDTO("Title3", "Description3","location3", 15.0110, 27.5000)

        mainCoroutineRule.runBlockingTest {
            dataSource.addReminders(reminder1, reminder2, reminder3)
        }*/


    }
    @After
    fun stopKoinAfterTest() = stopKoin()



    @Test
    fun loadTasks_loading() {

        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Load the task in the viewmodel
        remindersListViewModel.loadReminders()

        // Then progress indicator is shown
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }


    @Test
    fun loadReminders_emptyList_returnEmpty() {

//        Given an empty data list


//        When load reminders
        remindersListViewModel.loadReminders()

//        Then show no data is true
        assertThat(
            remindersListViewModel.showNoData.value, `is`(true)
        )
    }

    @Test
    fun loadReminders_validList_returnNotEmpty() {

        val reminder1 = ReminderDTO("Title1","Description1","Location1",35.5532, 120.3352)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",37.5532, 122.3352)
        val reminder3 = ReminderDTO("Title3","Description3","Location3",39.5532, 125.3352)

        mainCoroutineRule.runBlockingTest {
            dataSource.addReminders(reminder1, reminder2, reminder3)
        }

        remindersListViewModel.loadReminders()

        // Then progress indicator is hidden
        assertThat(
            remindersListViewModel.showNoData.value, `is`(false)
        )
    }

    @Test
    fun loadReminders_validList_reminderListEqualsDataSource() {

        val reminder1 = ReminderDTO("Title1","Description1","Location1",35.5532, 120.3352)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",37.5532, 122.3352)
        val reminder3 = ReminderDTO("Title3","Description3","Location3",39.5532, 125.3352)

        mainCoroutineRule.runBlockingTest {
            dataSource.addReminders(reminder1, reminder2, reminder3)
        }

        remindersListViewModel.loadReminders()

        // Then progress indicator is hidden
        assertThat(
            remindersListViewModel.remindersList.value, `is`(dataSource.reminders?.map {
                ReminderDataItem(
                it.title,
                it.description,
                it.location,
                it.latitude,
                it.longitude,
                it.id) })
        )
    }



    @Test
    fun loadReminders_validList_returnCorrectReminder() {

        val reminder1 = ReminderDTO("Title1","Description1","Location1",
            35.5532, 120.3352)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",
            37.5532, 122.3352)
        val reminder3 = ReminderDTO("Title3","Description3","Location3",
            39.5532, 125.3352)

        mainCoroutineRule.runBlockingTest {
            dataSource.addReminders(reminder1, reminder2, reminder3)
        }

        remindersListViewModel.loadReminders()

        // Then progress indicator is hidden
        assertThat(
            remindersListViewModel.remindersList.value?.first { it.id == reminder1.id }?.title
            , `is`(reminder1.title)
        )
    }
}