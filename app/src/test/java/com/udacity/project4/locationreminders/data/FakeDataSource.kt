package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.*


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {


    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error(
            "Reminders not found"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let { reminders ->
            return Result.Success(reminders.first { it.id == id })
        }
        return Result.Error ( "Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    suspend fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            reminders@this.saveReminder(reminder)
        }
    }

}