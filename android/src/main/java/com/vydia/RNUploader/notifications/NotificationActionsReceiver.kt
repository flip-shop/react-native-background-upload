package com.vydia.RNUploader.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d
import com.vydia.RNUploader.notifications.data.ACTION_UPLOAD_CANCEL
import com.vydia.RNUploader.notifications.data.PARAM_UPLOAD_ID

private const val TAG = "NotificationActReceiver"
class NotificationActionsReceiver(
  private val cancelActionReceived: (uploadId: String) -> Unit
): BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    d(TAG, "onReceive")
    if(intent == null || context == null) {
      return
    }
    d(TAG, "onReceive intent = $intent")
    if(intent.action == ACTION_UPLOAD_CANCEL) {
      intent.getStringExtra(PARAM_UPLOAD_ID)?.let {
        cancelActionReceived(it)
      }
    }
  }
}
