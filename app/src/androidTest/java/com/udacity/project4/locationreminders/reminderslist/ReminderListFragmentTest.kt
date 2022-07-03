package com.udacity.project4.locationreminders.reminderslist

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.atPosition
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
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
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {


    private val repository by inject<FakeAndroidTestRepository>()
    private lateinit var appContext : Application


    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    val myModule = module {
        // define your module for test here
        viewModel {
            RemindersListViewModel(
                appContext,
                get() as FakeAndroidTestRepository
            )
        }
        single{ FakeAndroidTestRepository() }
    }

    @Before
    fun setupKoin() {
        stopKoin()// stop the original app koin, which is launched when the application starts (in "MyApp")
        appContext = ApplicationProvider.getApplicationContext()

        startKoin {
            androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }
    }

    @After
    fun endKoin() = stopKoin()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        dataBindingIdlingResource.monitorFragment(scenario)


        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // THEN - Verify that we navigate to the add screen
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun addReminders_remindersShowOnList() {
        // GIVEN - On the home screen
        val reminder1 = ReminderDTO("Title1","Description1","Location1",35.5532, 120.3352)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",37.5532, 122.3352)

        runBlockingTest {
            repository.addReminders(reminder1, reminder2)
        }


        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // WHEN - Reminder is added

        // THEN - Verify items are added to the list
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(atPosition(0,
                hasDescendant(withText("Title1")))))

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(atPosition(1,
                hasDescendant(withText("Title2")))))
    }
    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */

    @Test
    fun noData_showErrorMessage(){

        val reminder1 = ReminderDTO("Title1","Description1","Location1",35.5532, 120.3352)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",37.5532, 122.3352)

        runBlockingTest {
            repository.addReminders(reminder1, reminder2)
        }

        repository.setReturnError(true)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches((withText("Testing Result Error"))))

    }


}
