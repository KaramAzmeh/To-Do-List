package com.udacity.todolist.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.todolist.R
import com.udacity.todolist.locationreminders.data.ReminderDataSource
import com.udacity.todolist.locationreminders.data.dto.ReminderDTO
import com.udacity.todolist.locationreminders.data.dto.Result
import com.udacity.todolist.locationreminders.data.local.RemindersLocalRepository
import com.udacity.todolist.locationreminders.reminderslist.ReminderDataItem
import com.udacity.todolist.locationreminders.savereminder.SaveReminderFragment
import com.udacity.todolist.utils.GeofencingConstants
import com.udacity.todolist.utils.errorMessage
import com.udacity.todolist.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeoTrnsJobIntSvc"

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {

        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(applicationContext, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, applicationContext.getString(R.string.geofence_entered))
                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort Mission!")
                        return
                    }
                }

                Log.v(TAG, "fence id is: $fenceId")

                val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
                CoroutineScope(coroutineContext).launch(SupervisorJob()) {


                    Log.i(TAG, "Searching fenceId in process")

                    //get the reminder with the request id
                    val result = remindersLocalRepository.getReminder(fenceId)

                    if (result is Result.Success<ReminderDTO>) {
                        Log.i(TAG, "fence id was found, sending notification")
                        sendNotification(geofencingEvent.triggeringGeofences)
                    } else {
                        Log.e(TAG, "Unknown Geofence: No notification to send")
                    }


                    Log.i(TAG, "onHandleWork is complete")

                }
            }
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = triggeringGeofences.first().requestId

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}