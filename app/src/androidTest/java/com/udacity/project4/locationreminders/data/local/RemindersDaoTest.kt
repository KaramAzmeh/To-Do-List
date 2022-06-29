package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description","location"
        , 23.3333, 53.2262)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values
        MatcherAssert.assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        MatcherAssert.assertThat(loaded.id, `is`(reminder.id))
        MatcherAssert.assertThat(loaded.title, `is`(reminder.title))
        MatcherAssert.assertThat(loaded.description, `is`(reminder.description))
        MatcherAssert.assertThat(loaded.location, `is`(reminder.location))
        MatcherAssert.assertThat(loaded.latitude, `is`(reminder.latitude))
        MatcherAssert.assertThat(loaded.longitude, `is`(reminder.longitude))
    }


    @Test
    fun deleteAllReminders() = runBlockingTest {
        // When inserting reminders
        val reminder1 = ReminderDTO("title1", "description1","location1"
            , 13.3333, 13.2262)
        val reminder2 = ReminderDTO("title2", "description2","location2"
            , 23.3333, 23.2262)
        val reminder3 = ReminderDTO("title3", "description3","location3"
            , 33.3333, 33.2262)

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // When the task is updated
        database.reminderDao().deleteAllReminders()

        // THEN - The loaded reminders are deleted
        MatcherAssert.assertThat(database.reminderDao().getReminders(), `is`(emptyList()))
    }
}