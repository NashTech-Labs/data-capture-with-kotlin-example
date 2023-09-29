package com.zebra.datacapture1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

/**
 * This is the main activity class for the DataCapture Android application.
 */
class MainActivity : AppCompatActivity() {

    // Private variables
    private val bRequestSendResult = false
    private val TAG = "DataCapture1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register the BroadcastReceiver to listen for DataWedge responses
        createProfile()
        registerReceivers()
        cloneProfile()

        // Register for status change notification
        // Use REGISTER_FOR_NOTIFICATION: http://techdocs.zebra.com/datawedge/latest/guide/api/registerfornotification/
        val b = Bundle()
        b.putString(EXTRA_KEY_APPLICATION_NAME, packageName)
        b.putString(
            EXTRA_KEY_NOTIFICATION_TYPE,
            "SCANNER_STATUS"
        ) // register for changes in scanner status
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b)
        registerReceivers()

        // Get DataWedge version
        // Use GET_VERSION_INFO: http://techdocs.zebra.com/datawedge/latest/guide/api/getversioninfo/
        sendDataWedgeIntentWithExtra(
            ACTION_DATAWEDGE,
            EXTRA_GET_VERSION_INFO,
            EXTRA_EMPTY
        ) // must be called after registering BroadcastReceiver
    }

    /**
     * Create a profile from the UI onClick() event.
     */
    fun createProfile() {
        val profileName = EXTRA_PROFILENAME
        val profileTextView = findViewById(R.id.profileName) as TextView
        profileTextView.text = profileName


        // Send DataWedge intent with extra to create profile
        // Use CREATE_PROFILE: http://techdocs.zebra.com/datawedge/latest/guide/api/createprofile/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, profileName)

        // Configure created profile to apply to this app
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString(
            "CONFIG_MODE",
            "CREATE_IF_NOT_EXIST"
        ) // Create profile if it does not exist

        // Configure barcode input plugin
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true") //  This is the default
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)

        // Associate profile with this app
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        profileConfig.remove("PLUGIN_CONFIG")
        val appNameTextView = findViewById(R.id.appName) as TextView
        appNameTextView.text = packageName

        // Apply configs
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)

        // Configure intent output for captured data to be sent to this app
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", "com.zebra.datacapture1.ACTION")
        intentProps.putString("intent_delivery", "2")
        val modeTextView = findViewById(R.id.mode) as TextView
        modeTextView.text = "Broadcast"
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig)
        Toast.makeText(
            applicationContext,
            "Created profile.  Check DataWedge app UI.",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Method to clone the existing datawedge profile
     */
    private fun cloneProfile() {
        val i = Intent()
        i.action = "com.symbol.datawedge.api.ACTION"
        val values = arrayOf("Source profile", "Destination Profile")
        i.putExtra("com.symbol.datawedge.api.CLONE_PROFILE", values)
        sendBroadcast(i)
    }

    // Toggle soft scan trigger from UI onClick() event
    // Use SOFT_SCAN_TRIGGER: http://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/
    fun ToggleSoftScanTrigger(view: View?) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING")
    }

    /**
     * Registers the BroadcastReceiver to listen for DataWedge responses and notifications.
     */
    private fun registerReceivers() {
        Log.d(TAG, "registerReceivers()")
        val filter = IntentFilter()
        filter.addAction(ACTION_RESULT_NOTIFICATION) // for notification result
        filter.addAction(ACTION_RESULT) // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction(resources.getString(R.string.activity_intent_filter_action))
        filter.addAction(resources.getString(R.string.activity_action_from_service))
        registerReceiver(myBroadcastReceiver, filter)
    }

    /**
     * Unregisters the scanner status notification.
     */
    fun unRegisterScannerStatus() {
        Log.d(TAG, "unRegisterScannerStatus()")
        val b = Bundle()
        b.putString(EXTRA_KEY_APPLICATION_NAME, packageName)
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS)
        val i = Intent()
        i.action = ContactsContract.Intents.Insert.ACTION
        i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b)
        this.sendBroadcast(i)
    }

    /**
     * Sets the decoder value based on the state of the provided CheckBox.
     *
     * @param decoder The CheckBox representing the decoder state.
     * @return A String representing the decoder value, either "true" if the CheckBox is checked
     *         or "false" if it is unchecked.
     */
    fun setDecoder(decoder: CheckBox): String {
        val checkValue = decoder.isChecked
        var value = "false"
        return if (checkValue) {
            value = "true"
            value
        } else value
    }

    /**
     * A BroadcastReceiver responsible for handling DataWedge responses and notifications.
     */
    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        /**
         * This method is called when a broadcast is received.
         *
         * @param context The context in which the receiver is running.
         * @param intent The Intent containing the broadcasted data.
         */
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val b = intent.extras
            Log.d(TAG, "DataWedge Action:$action")

            // Get DataWedge version info
            if (intent.hasExtra(EXTRA_RESULT_GET_VERSION_INFO)) {
                val versionInfo = intent.getBundleExtra(EXTRA_RESULT_GET_VERSION_INFO)
                val DWVersion = versionInfo.getString("DATAWEDGE")
                val txtDWVersion = findViewById(R.id.txtGetDWVersion) as TextView
                txtDWVersion.text = DWVersion
                Log.i(TAG, "DataWedge Version: $DWVersion")
            }
            if (action == resources.getString(R.string.activity_intent_filter_action)) {
                //  Received a barcode scan
                try {
                    displayScanResult(intent, "via Broadcast")
                } catch (e: Exception) {
                    //  Catch error if the UI does not exist when we receive the broadcast...
                }
            } else if (action == ACTION_RESULT) {
                    cloneProfile()
                val clonedProfileInfo = intent.getStringExtra("com.symbol.datawedge.api.RESULT_GET_CLONE_PROFILE")
                val profileTextView = findViewById(R.id.profileName) as TextView
                profileTextView.text = clonedProfileInfo
                // Register to receive the result code
                if (intent.hasExtra(EXTRA_RESULT) && intent.hasExtra(EXTRA_COMMAND)) {
                    val command = intent.getStringExtra(EXTRA_COMMAND)
                    val result = intent.getStringExtra(EXTRA_RESULT)
                    var info = ""
                    if (intent.hasExtra(EXTRA_RESULT_INFO)) {
                        val result_info = intent.getBundleExtra(EXTRA_RESULT_INFO)
                        val keys = result_info.keySet()
                        for (key in keys) {
                            val `object` = result_info[key]
                            if (`object` is String) {
                                info += "$key: $`object`\n"
                            } else if (`object` is Array<*> && `object`.isArrayOf<String>()) {
                                for (code in `object`) {
                                    info += "$key: $code\n"
                                }
                            }
                        }
                        Log.d(TAG, "Command: $command Result: $result Result Info: $info".trimIndent())
                        Toast.makeText(
                            applicationContext,
                            "Error Resulted. Command:$command\nResult: $result\nResult Info: $info",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else if (action == ACTION_RESULT_NOTIFICATION) {
                if (intent.hasExtra(EXTRA_RESULT_NOTIFICATION)) {
                    val extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION)
                    val notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE)
                    if (notificationType != null) {
                        when (notificationType) {
                            EXTRA_KEY_VALUE_SCANNER_STATUS -> {
                                // Change in scanner status occurred
                                val displayScannerStatusText = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS) +
                                        ", profile: " + extras.getString(
                                    EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME
                                )
                                //Toast.makeText(getApplicationContext(), displayScannerStatusText, Toast.LENGTH_SHORT).show();
                                val lblScannerStatus =
                                    findViewById(R.id.lblScannerStatus) as TextView
                                lblScannerStatus.text = displayScannerStatusText
                                Log.i(TAG, "Scanner status: $displayScannerStatusText")
                            }

                            EXTRA_KEY_VALUE_PROFILE_SWITCH -> {}
                            EXTRA_KEY_VALUE_CONFIGURATION_UPDATE -> {}
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays the scanned data and label type based on the information provided in the initiating Intent.
     *
     * @param initiatingIntent The Intent containing the scanned data and label type.
     * @param howDataReceived  A description of how the data was received, e.g., "via Broadcast."
     */
    private fun displayScanResult(initiatingIntent: Intent, howDataReceived: String) {
        if (initiatingIntent.hasExtra("com.symbol.datawedge.api.DATA_STRING") &&
            initiatingIntent.hasExtra("com.symbol.datawedge.api.LABEL_TYPE")
        ) {
            // Extract the data and label type
            val decodedData =
                initiatingIntent.getStringExtra("com.symbol.datawedge.api.DATA_STRING")
            val decodedLabelType =
                initiatingIntent.getStringExtra("com.symbol.datawedge.api.LABEL_TYPE")
            val lblScanData = findViewById(R.id.lblScanData) as TextView
            val lblScanLabelType = findViewById(R.id.lblScanDecoder) as TextView
            lblScanData.text = decodedData
            lblScanLabelType.text = decodedLabelType
        } else {
            // Handle the case when the expected extras are not present
            Log.e(TAG, "Intent is missing expected extras (DATA_STRING and/or LABEL_TYPE)")
        }
    }

    /**
     * Sends a DataWedge intent with the specified action and extra information.
     *
     * @param action    The action associated with the intent.
     * @param extraKey  The key of the extra data to be included in the intent.
     * @param extras    The bundle containing extra data to be included in the intent.
     */
    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extras: Bundle) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extras)

        // Include the SEND_RESULT extra if requested
        if (bRequestSendResult) dwIntent.putExtra(EXTRA_SEND_RESULT, "true")

        // Send the broadcast intent
        this.sendBroadcast(dwIntent)
    }

    /**
     * Sends a DataWedge intent with the specified action and a single extra value.
     *
     * @param action     The action associated with the intent.
     * @param extraKey   The key of the extra data to be included in the intent.
     * @param extraValue The value of the extra data to be included in the intent.
     */
    private fun sendDataWedgeIntentWithExtra(action: String, extraKey: String, extraValue: String) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extraValue)

        // Include the SEND_RESULT extra if requested
        if (bRequestSendResult) dwIntent.putExtra(EXTRA_SEND_RESULT, "true")

        // Send the broadcast intent
        this.sendBroadcast(dwIntent)
    }

    /**
     * Called when the activity is resumed from the paused state.
     * Registers the necessary receivers to listen for DataWedge responses and notifications.
     */
    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    /**
     * Called when the activity is paused, typically when another activity is in the foreground.
     * Unregisters the BroadcastReceiver for DataWedge responses and notifications, and
     * unregisters the scanner status notification.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(myBroadcastReceiver)
        unRegisterScannerStatus()
    }

    /**
     * Called when the activity is being destroyed.
     * Performs cleanup and releases resources.
     */
    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Called when the activity is starting.
     * Performs any necessary initialization or setup when the activity is about to become visible.
     */
    override fun onStart() {
        super.onStart()
    }

    /**
     * Called when the activity is no longer visible to the user.
     * Performs any cleanup or actions required when the activity is stopped.
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * This companion object contains constants related to DataWedge integration.
     * It defines action names, extra keys, and other constants used in the DataWedge integration.
     * These constants are used for communication with the DataWedge API.
     */
    companion object {

        // DataWedge Sample supporting DataWedge APIs up to DW 7.0
        private const val EXTRA_PROFILENAME = "DWDataCapture1"

        // DataWedge Extras
        private const val EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO"
        private const val EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"
        private const val EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME"
        private const val EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE"
        private const val EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER"
        private const val EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION"
        private const val EXTRA_REGISTER_NOTIFICATION =
            "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION"
        private const val EXTRA_UNREGISTER_NOTIFICATION =
            "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION"
        private const val EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
        private const val EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
        private const val EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS"
        private const val EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH"
        private const val EXTRA_KEY_VALUE_CONFIGURATION_UPDATE = "CONFIGURATION_UPDATE"
        private const val EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS"
        private const val EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME"
        private const val EXTRA_SEND_RESULT = "SEND_RESULT"
        private const val EXTRA_EMPTY = ""
        private const val EXTRA_RESULT_GET_VERSION_INFO =
            "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO"
        private const val EXTRA_RESULT = "RESULT"
        private const val EXTRA_RESULT_INFO = "RESULT_INFO"
        private const val EXTRA_COMMAND = "COMMAND"

        // DataWedge Actions
        private const val ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION"
        private const val ACTION_RESULT_NOTIFICATION =
            "com.symbol.datawedge.api.NOTIFICATION_ACTION"
        private const val ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION"
    }

}