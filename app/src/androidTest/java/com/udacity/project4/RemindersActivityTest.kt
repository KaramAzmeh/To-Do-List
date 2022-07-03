package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.atPosition
import com.udacity.project4.util.longClickIn
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun clearData() {
        //clear the data after complete
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
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
    fun createOneReminder_verifyListContainsItem() {

        lateinit var activity: Activity
        // start up Reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        activityScenario.onActivity {
            activity = it
        }
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(longClickIn(450, 265))

        onView(withId(R.id.save_button))
            .perform(click())

        onView(withId(R.id.saveReminder)).perform(click())


        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(not(`is`(activity.window.decorView)))
        ).check(matches(isDisplayed()))

        // Verify it was added
        onView(withId(R.id.reminderssRecyclerView))
            .check(
                ViewAssertions.matches(
                    atPosition(
                        0,
                        ViewMatchers.hasDescendant(withText("Title"))
                    )
                )
            )

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()

    }


    @Test
    @MediumTest
    fun saveReminder_noLocation_returnSnackbar() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Description"), ViewActions.closeSoftKeyboard())

        onView(withId(R.id.saveReminder)).perform(click())


        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches((withText(R.string.err_select_location))))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()

    }

    @Test
    @MediumTest
    fun saveReminder_noTitle_returnSnackbar() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(longClickIn(450, 265))

        onView(withId(R.id.save_button))
            .perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches((withText(R.string.err_enter_title))))

        activityScenario.close()

    }

}
