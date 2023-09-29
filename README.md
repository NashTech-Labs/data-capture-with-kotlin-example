# DataCapture Android Application

The DataCapture Android application is designed for integration with Zebra DataWedge, enabling barcode scanning and data capture capabilities. This README provides an overview of the main components and functionalities of the application.

## Overview

The DataCapture Android application is built to interact with Zebra DataWedge, a powerful tool for controlling barcode scanning and data capture on Zebra devices. It allows you to create profiles, configure barcode scanning, and receive notifications about scanner status changes.
![image](https://github.com/ibrahimnadra/DataCaptureWithKotlin/assets/124992299/aea9d1fc-6284-44fc-840e-45cb13b12b60)


## Main Components

### MainActivity

The `MainActivity` class is the main activity of the application. It serves as the user interface for managing DataWedge profiles, scanner settings, and barcode scanning.

### Companion Object

The `companion object` within the `MainActivity` class contains constants and action names related to DataWedge integration. These constants are used for communication with the DataWedge API.

### BroadcastReceiver

The `myBroadcastReceiver` inner class is a `BroadcastReceiver` responsible for handling DataWedge responses and notifications. It listens for DataWedge actions, processes barcode scan data, and handles scanner status changes.

### Methods

- `createProfile()`: Creates a DataWedge profile and configures it for barcode scanning.
- `cloneProfile()`: Clones an existing DataWedge profile.
- `ToggleSoftScanTrigger(view: View?)`: Toggles the soft scan trigger for barcode scanning.
- `setDecoder(decoder: CheckBox)`: Sets the decoder value based on the state of a CheckBox.
- `registerReceivers()`: Registers the BroadcastReceiver to listen for DataWedge responses and notifications.
- `unRegisterScannerStatus()`: Unregisters the scanner status notification.
- `displayScanResult(initiatingIntent: Intent, howDataReceived: String)`: Displays scanned data and label type.
- `sendDataWedgeIntentWithExtra(action: String, extraKey: String, extras: Bundle)`: Sends a DataWedge intent with extra information.
- `sendDataWedgeIntentWithExtra(action: String, extraKey: String, extraValue: String)`: Sends a DataWedge intent with a single extra value.

### Lifecycle Methods

- `onCreate(savedInstanceState: Bundle?)`: Initializes the main activity and sets up DataWedge integration.
- `onResume()`: Registers necessary receivers when the activity is resumed.
- `onPause()`: Unregisters receivers and scanner status notification when the activity is paused.
- `onDestroy()`: Performs cleanup and resource release when the activity is destroyed.
- `onStart()`: Initializes the activity when it is starting.
- `onStop()`: Performs cleanup when the activity is no longer visible.

## Getting Started with Zebra device

1. Ensure that your Zebra device supports DataWedge and that DataWedge is installed and configured.

2. Install the DataCapture1 Android application on your Zebra device.

3. Launch the application and this application will automatically create and configure DataWedge profiles.

4. Use the application to scan barcodes.


## Getting Started without a Zebra device

The application and its integration with Zebra DataWedge are specifically designed for Zebra devices, which have built-in barcode scanning capabilities and the DataWedge framework. DataWedge works by sending barcode data to the application as either an Intent or by emulating key events. So, we can use the following adb shell commands to simulate the data that DataWedge will send on a real device when barcodes are scanned : 
adb shell am broadcast -a com.zebra.datacapture1.ACTION --es com.symbol.datawedge.api.DATA_STRING "123456789" --es com.symbol.datawedge.api.LABEL_TYPE "Code128"

## Resources

- [Zebra TechDocs](http://techdocs.zebra.com/datawedge/latest/guide/samples/barcode1/)
- [Zebra DataWedge Documentation](http://techdocs.zebra.com/datawedge/latest/guide/)
- [Zebra Developer Portal](https://developer.zebra.com/)


