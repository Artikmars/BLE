package com.example.artam.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final long SCAN_PERIOD = 3000;
    BluetoothAdapter adapter;
    private TextView mText;
    private Button mAdvertiseButton;
    private Button mDiscoverButton;
    private String uid;
    private List<ScanFilter> filters;

    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();
            mAdvertiseButton.setEnabled(false);
            mDiscoverButton.setEnabled(false);
        }
        setContentView(R.layout.activity_main);

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();


        //uid = AdvertisingIdClient.Info.getId();

        // BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);


       /* if (btManager != null) {
            btAdapter = btManager.getAdapter();
        }
*/

     /*   if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(intent, 1);
        } else {
            Toast.makeText(getApplicationContext(),"BLE on", Toast.LENGTH_SHORT).show();
            startScanning();
            //do BLE stuff
        }*/


        //  btAdapter.startLeScan(leScanCallback);
        //     btAdapter.stopLeScan(leScanCallback);

        // BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(context, false, btleGattCallback);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.discover_btn) {
            discover();
        } else if (v.getId() == R.id.advertise_btn) {
            advertise();
        }
    }

    private void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(false) //flags in scan record are not added
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));


       /* AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, "H".getBytes(Charset.forName("UTF-8")))
                .build();*/
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, "H".getBytes(Charset.forName("UTF-8")))
                .build();


        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Toast.makeText(getApplicationContext(), "onStartSuccess", Toast.LENGTH_SHORT).show();
                Log.i("myLogs", "onStartSuccess ");
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.i("myLogs", "Advertising onStartFailure: " + errorCode);
                Toast.makeText(getApplicationContext(), "onStartFailure " + errorCode, Toast.LENGTH_SHORT).show();
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, data, advertisingCallback);
    }

    private void discover() {
        Log.i("myLogs", "discover() ");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, SCAN_PERIOD);

        // filters = new ArrayList<>();
        List<ScanFilter> filters = Arrays.asList(new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid))))
                .build());
        //filters.add(filter);
        Log.i("myLogs", "filters were added");

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        Log.i("myLogs", "settings were built");


        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        //Log.i("myLogs", "startScan");
        // mBluetoothLeScanner.startScan(mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Toast.makeText(getApplicationContext(), "onScanResult", Toast.LENGTH_SHORT).show();
            Log.i("myLogs", "Device Name: " + result.getDevice().getName() + " Plaintext: " + result.getScanRecord().getServiceData().get(0) +
                    " RSSI: " + result.getRssi() + " Device Name: " + result.getDevice().toString()
                    + " UUID: " + result.getScanRecord().getServiceUuids().get(0) + " Manufactured Data: " +
                    result.getScanRecord().getManufacturerSpecificData().toString());
            Log.i("myLogs", "ScanResult: " + result.toString());

            //Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
            super.onScanResult(callbackType, result);
            if (result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()))
                return;
//mac adress, gps longitude and lattitude, manifactured data, put to wiki advert.modes, listening
           // in background, put avertiseflags, phonenumber, broadcaster (disable for Meizu) and observer(enable for both)
            //2 broadcasts
            StringBuilder builder = new StringBuilder(result.getDevice().getName());
            Log.i("myLogs", "builder was created ");

            try {
                // builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                builder.append("\n").append(result.toString());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //   a = result.toString();
            //   mText.setText(a);
            //  Toast.makeText(getApplicationContext(),a,Toast.LENGTH_LONG).show();

            //   Log.i("myLogs", "builder.append ");

            //  mText.setText(builder.toString());
            //  Log.i("myLogs", "text was made ");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("myLogs", "Discovery onScanFailed: " + errorCode);
            Toast.makeText(getApplicationContext(), "onScanFailed " + errorCode, Toast.LENGTH_SHORT).show();
            super.onScanFailed(errorCode);
        }
    };


 /*   private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            // your implementation here
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a 			BluetoothGatt.discoverServices() call
        }*/



   /* public void startScanning(){

        BluetoothLeScanner scanner = btAdapter.getBluetoothLeScanner();
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        List<ScanFilter> scanFilters = Arrays.asList(
                new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString("some uuid"))
                        .build());

        scanner.startScan(scanFilters, scanSettings, new MyScanCallback());
    }
*/
   /* private class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            Toast.makeText(getApplicationContext(),"onScanResult", Toast.LENGTH_SHORT).show();
            BluetoothDevice device = btAdapter.getRemoteDevice(result.getDevice().getAddress());
            BluetoothGatt gatt = device.connectGatt(this, false, new myGattCallBack());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Toast.makeText(getApplicationContext(),"onBatchScanResults", Toast.LENGTH_SHORT).show();
        }
*/
     /*   @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getApplicationContext(),"onScanFailed", Toast.LENGTH_SHORT).show();
        }*/
}





