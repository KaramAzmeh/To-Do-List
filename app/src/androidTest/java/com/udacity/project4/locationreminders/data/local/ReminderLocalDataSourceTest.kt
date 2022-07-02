package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class ReminderLocalDataSourceTest {


    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase
    // Set the main coroutines dispatcher for unit testing.

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each reminder synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    @SmallTest
    fun saveReminder_retrievesReminder() = runBlockingTest {
        // GIVEN - a new task saved in the database
        val newReminder = ReminderDTO("title", "description","location"
            , 23.3333, 53.2262)
        localDataSource.saveReminder(newReminder)

        // WHEN  - Reminder retrieved by ID

        val result = localDataSource.getReminder(newReminder.id)

        val succeeded = result is Result.Success


        // THEN - Same Reminder is returned
        assertThat(succeeded, CoreMatchers.`is`(true))
        result as Result.Success
        Assert.assertThat(result.data.title, CoreMatchers.`is`("title"))
        Assert.assertThat(result.data.description, CoreMatchers.`is`("description"))
        Assert.assertThat(result.data.location, CoreMatchers.`is`("location"))
        Assert.assertThat(result.data.latitude, CoreMatchers.`is`(23.3333))
        Assert.assertThat(result.data.longitude, CoreMatchers.`is`(53.2262))
    }

    @Test
    @SmallTest
    fun retrieveNonExistingReminder_throwsError() = runBlockingTest {
        // GIVEN - an empty database

        // WHEN  - Non-existing id is requested

        val result = localDataSource.getReminder(UUID.randomUUID().toString())

        val error = result is Result.Error

        // THEN - Error Reminder not found is returned
        assertThat(error, CoreMatchers.`is`(true))
        result as Result.Error
        Assert.assertThat(result.message, CoreMatchers.`is`("Reminder not found!"))
    }
}