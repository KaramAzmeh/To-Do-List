package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.utils.errorMessage
import com.udacity.project4.utils.sendNotification
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

        val geofenceTriggers : MutableList<Geofence> = mutableListOf()

        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(applicationContext, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, applicationContext.getString(R.string.geofence_entered))

                if (geofencingEvent.triggeringGeofences.isEmpty()){
                        Log.e(TAG, "No Geofence Trigger Found! Abort Mission!")
                        return
                    }


                    val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
                    CoroutineScope(coroutineContext).launch(SupervisorJob()) {

                        for (geofence in geofencingEvent.triggeringGeofences){

                            val fenceId = geofence.requestId

                            Log.v(TAG, "fence id is: $fenceId")

                        Log.i(TAG, "Searching fenceId in process")

                        //get the reminder with the request id
                        val result = remindersLocalRepository.getReminder(fenceId)

                        if (result is Result.Success<ReminderDTO>) {
                            Log.i(TAG, "fence id was found, sending notification")
                            geofenceTriggers.add(geofence)
                        } else {
                            Log.e(TAG, "Unknown Geofence: No notification to send")
                        }
                    }

                        sendNotification(geofenceTriggers)
                        Log.i(TAG, "onHandleWork is complete")

                }

            }
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()

        Log.i(TAG,triggeringGeofences.toString())
        for (fence in triggeringGeofences) {
            val requestId = fence.requestId
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
}