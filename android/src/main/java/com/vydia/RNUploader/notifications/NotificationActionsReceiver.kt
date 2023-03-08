package com.vydia.RNUploader.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vydia.RNUploader.notifications.data.ACTION_UPLOAD_CANCEL
import com.vydia.RNUploader.notifications.data.PARAM_UPLOAD_ID
import com.vydia.RNUploader.worker.UploadWorkerManager
import com.vydia.RNUploader.worker.UploadWorkerManagerImpl
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "NotificationActReceiver"
class NotificationActionsReceiver: BroadcastReceiver() {

  private val uploadWorkerManager: UploadWorkerManager
    by inject(UploadWorkerManagerImpl::class.java)

  override fun onReceive(context: Context?, intent: Intent?) {

    if(intent == null || context == null) {
      return
    }

    if(intent.action == ACTION_UPLOAD_CANCEL) {
      intent.getStringExtra(PARAM_UPLOAD_ID)?.let {
        uploadWorkerManager.cancelWorker(it)
      }
    }
  }
}
