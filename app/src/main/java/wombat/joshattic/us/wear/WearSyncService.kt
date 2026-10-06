package wombat.joshattic.us.wear

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.storage.AuthPreferences

class WearSyncService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val PATH_AUTH = "/wombat/auth"
        const val KEY_TOKEN = "token"
        const val KEY_USERNAME = "username"

        const val PATH_SETTINGS = "/wombat/settings"
        const val KEY_SHOW_IMAGES = "show_images"
        const val KEY_SHOW_PFP = "show_pfp"
        const val KEY_FEED_TYPE = "feed_type"

        fun pushSession(dataClient: DataClient, token: String?, username: String?) {
            val request = PutDataMapRequest.create(PATH_AUTH).apply {
                dataMap.putString(KEY_TOKEN, token ?: "")
                dataMap.putString(KEY_USERNAME, username ?: "")
                dataMap.putLong("ts", System.currentTimeMillis())
            }
            dataClient.putDataItem(request.asPutDataRequest().setUrgent())
        }

        fun pushSettings(dataClient: DataClient, showImages: Boolean, showPfp: Boolean, feedType: String) {
            val request = PutDataMapRequest.create(PATH_SETTINGS).apply {
                dataMap.putBoolean(KEY_SHOW_IMAGES, showImages)
                dataMap.putBoolean(KEY_SHOW_PFP, showPfp)
                dataMap.putString(KEY_FEED_TYPE, feedType)
                dataMap.putLong("ts", System.currentTimeMillis())
            }
            dataClient.putDataItem(request.asPutDataRequest().setUrgent())
        }
    }
}
