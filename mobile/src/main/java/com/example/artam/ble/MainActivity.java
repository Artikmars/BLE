package com.example.artam.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final long SCAN_PERIOD = 3000;
    private Button mAdvertiseButton;
    private Button mDiscoverButton;


    TextView tvEnabledGPS;
    TextView tvStatusGPS;
    TextView tvLocationGPS;
    TextView tvEnabledNet;
    TextView tvStatusNet;
    TextView tvLocationNet;
    TextView tvScanResultLat;
    TextView tvScanResultLong;

    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();

    private AdvertiseData advData;

    private float longitude;
    private float latitude;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        mAdvertiseButton = (Button) findViewById(R.id.advertise_btn);
        mDiscoverButton = (Button) findViewById(R.id.discover_btn);
        tvScanResultLat = (TextView) findViewById(R.id.tvScanResultLat);
        tvScanResultLong = (TextView) findViewById(R.id.tvScanResultLong);

        mDiscoverButton.setOnClickListener(this);
        mAdvertiseButton.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        if (!isBluetoothEnabled()) {
            Toast.makeText(this, "Please turn Bluetooth on!", Toast.LENGTH_SHORT).show();
            mAdvertiseButton.setEnabled(false);
            mDiscoverButton.setEnabled(false);
        } else {
            if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                Toast.makeText(this, "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();
                Log.i("myLogs", "Multiple advertisement not supported ");
                mAdvertiseButton.setEnabled(false);
                mDiscoverButton.setEnabled(false);
            }


        }

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public void updateButtonsVisibility(View view) {
        if (isBluetoothEnabled()) {
            mAdvertiseButton.setEnabled(true);
            mDiscoverButton.setEnabled(true);

        } else {
            Toast.makeText(this, "Please turn Bluetooth on!", Toast.LENGTH_SHORT).show();
            mAdvertiseButton.setEnabled(false);
            mDiscoverButton.setEnabled(false);

        }
    }

    public void updateButtonsVisibility() {
        if (isBluetoothEnabled()) {
            mAdvertiseButton.setEnabled(true);
            mDiscoverButton.setEnabled(true);

        } else {
            Toast.makeText(this, "Please turn Bluetooth on!", Toast.LENGTH_SHORT).show();
            mAdvertiseButton.setEnabled(false);
            mDiscoverButton.setEnabled(false);

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.discover_btn) {
            discover();
        } else if (v.getId() == R.id.advertise_btn) {
            // advertise();
            setupAdvertising();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }


    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();

    }

    public void setupAdvertising() {

        if (isBluetoothEnabled()) {

            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false) //flags in scan record are not added
                    .setTimeout(0)
                    .build();

            advData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .setIncludeTxPowerLevel(false)
                    .addManufacturerData(0x4D4D, float2ByteArray(latitude))
                    .build();

            //  String s = String.valueOf(longitude);
            //  ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));
            //  Log.i("myLogs", Integer.toString(float2ByteArray(latitude, longitude)));
            // .addManufacturerData(1, float2ByteArray(longitude))
            // .addManufacturerData(0x4343, rawAdvData)
            // .addServiceUuid(pUuid)
            // .addServiceData(pUuid, "H".getBytes(Charset.forName("UTF-8")))
            // .addServiceData(pUuid, float2ByteArray(latitude))
            // .addServiceData(pUuid, "H".getBytes(Charset.forName("UTF-8")))
            // .addServiceData(pUuid, float2ByteArray(longitude))
            // .addServiceData(pUuid, s.getBytes(Charset.forName("UTF-8")))

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

            advertiser.startAdvertising(settings, advData, advertisingCallback);

        } else {
            updateButtonsVisibility();

        }
    }

  /*  public String byteArrayToString(byte[] in) {
        char out[] = new char[in.length * 2];
        for (int i = 0; i < in.length; i++) {
            out[i * 2] = "0123456789ABCDEF".charAt((in[i] >> 4) & 15);
            out[i * 2 + 1] = "0123456789ABCDEF".charAt(in[i] & 15);
        }
        return new String(out);
    }*/

    public static byte[] float2ByteArray(float latitude) {

        /*   byte[] lat = ByteBuffer.allocate(4).putFloat(latitude).array();
             byte[] lon = ByteBuffer.allocate(4).putFloat(longitude).array();
             return new byte[lat.length + lon.length];  */
        return ByteBuffer.allocate(4).putFloat(latitude).array();
    }


    private void discover() {
        if (isBluetoothEnabled()) {
            Log.i("myLogs", "discover() ");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);

            // ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));


           /* List<ScanFilter> scanFilters = Arrays.asList(new ScanFilter.Builder()
                    // .setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid))))
                    .setManufacturerData(0, float2ByteArray(latitude))
                    .setServiceData(pUuid, float2ByteArray(longitude))
                    .build());*/

            //  filters.add();
            // Log.i("myLogs", "filters were added");

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            Log.i("myLogs", "settings were built");

            //  mBluetoothLeScanner.startScan(scanFilters, settings, mScanCallback);
            mBluetoothLeScanner.startScan(null, settings, mScanCallback);

        } else {
            updateButtonsVisibility();

        }
        //Log.i("myLogs", "startScan");
        // mBluetoothLeScanner.startScan(mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {



        /*  latitude 50.9722 17219=[66, 75, -29, -112]
            longitude 11.3118=[65, 52, -3, 55]}*/

            Toast.makeText(getApplicationContext(), "onScanResult", Toast.LENGTH_SHORT).show();
           /* Log.i("myLogs", "Device Name: " + result.getDevice().getName() + " Plaintext: " + result.getScanRecord().getServiceData().get(0) +
                    " RSSI: " + result.getRssi() + " Device Name: " + result.getDevice().toString()
                    + "Describe Contents: " +
                    result + "Manufactured Data: " + result.getScanRecord().getManufacturerSpecificData());
            Log.i("myLogs", "ScanResult: " + result.toString());*/


/**                     THE PART BELOW IS STILL UNDER CONSTRUCTION. The task is to divide an array to two sub arrays.
                        The problem is that ByteBuffer does not work with arrays length less then 8 bytes.
                        Bufferunderflowexception occurs.

 */
   /*         if (result.getScanRecord().getManufacturerSpecificData(0x4D4D) != null) {
                byte[] scanResultLatArray = Arrays.copyOfRange(result.getScanRecord().getManufacturerSpecificData(0x4D4D), 0, 3);
                Log.i("myLogs", "Array: " + scanResultLatArray);
                byte[] scanResultLongArray = Arrays.copyOfRange(result.getScanRecord().getManufacturerSpecificData(0x4D4D), 4, 7);
                Log.i("myLogs", "Array: " + scanResultLongArray);

                float scanResultLat = ByteBuffer.wrap(scanResultLatArray).getFloat();
                Log.i("myLogs", "Latitude String float: " + scanResultLat);
                String latitudeString = Float.toString(scanResultLat);
                Log.i("myLogs", "Latitude String: " + latitudeString);

                float scanResultLong = ByteBuffer.wrap(scanResultLongArray).getFloat();
                Log.i("myLogs", "Longitude String float: " + scanResultLong);
                String longitudeString = Float.toString(scanResultLong);
                Log.i("myLogs", "Longitude String: " + longitudeString);
                //get float for latitude from first part of byte array, get float for longitude from second part of array
                if (latitudeString != null) {
                    String formattedScanResultLatitude = String.format(getString(R.string.scan_result_latitude), scanResultLat);
                    tvScanResultLat.setText(formattedScanResultLatitude);
                }
                if (longitudeString != null) {
                    String formattedScanResultLongitude = String.format(getString(R.string.scan_result_longitude), scanResultLong);
                    tvScanResultLong.setText(formattedScanResultLongitude);
                }

            }
            */
           /* float scanResultLongitude = ByteBuffer.wrap(result.getScanRecord().getManufacturerSpecificData(0)).getFloat();
            String latitudeString = Float.toString(scanResultLatitude);
            Log.i("myLogs", "Latitude String: " + latitudeString);
            if (latitudeString != null) {
                String formattedScanResultLatitude = String.format(getString(R.string.scan_result_latitude), scanResultLatitude);
                tvScanResult.setText(formattedScanResultLatitude);
            }
*/
            float scanResultLatitude = ByteBuffer.wrap(result.getScanRecord().getManufacturerSpecificData(0x4D4D)).getFloat();
            String latitudeString = Float.toString(scanResultLatitude);

            if (latitudeString != null) {
                Log.i("myLogs", "Latitude String: " + latitudeString);
                String formattedScanResultLatitude = String.format(getString(R.string.scan_result_latitude), scanResultLatitude);
                tvScanResultLat.setText(formattedScanResultLatitude);
            }


            //Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
            super.onScanResult(callbackType, result);
            /*if (result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()))
                return;*/

/*            StringBuilder builder = new StringBuilder(result.getDevice().getName());
            Log.i("myLogs", "builder was created ");

            try {
                // builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                builder.append("\n").append(result.toString());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }*/

            //   a = result.toString();
            //   mText.setText(a);
            //  Toast.makeText(getApplicationContext(),a,Toast.LENGTH_LONG).show();

            //   Log.i("myLogs", "builder.append ");

            //  mText.setText(builder.toString());
            //  Log.i("myLogs", "text was made ");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results)
                onScanResult(0, sr);

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("myLogs", "Discovery onScanFailed: " + errorCode);
            Toast.makeText(getApplicationContext(), "onScanFailed " + errorCode, Toast.LENGTH_SHORT).show();
            super.onScanFailed(errorCode);
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
              /*location.getLatitude();
        location.getLongitude();*/

            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle bundle) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                String formattedStatus = String.format(getString(R.string.status), status);
                tvStatusGPS.setText(formattedStatus);
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                String formattedStatus = String.format(getString(R.string.status), status);
                tvStatusNet.setText(formattedStatus);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.
                    PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String s) {
            checkEnabled();

        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    private String formatLocation(Location location) {

        if (location != null) {

            latitude = (float) location.getLatitude();
            longitude = (float) location.getLongitude();

            return String.format(getString(R.string.coordinates),
                    location.getLatitude(), location.getLongitude(), new Date(
                            location.getTime()));
        } else return "";

    }

    private void checkEnabled() {

        boolean gpsEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String formattedGpsEnabled = String.format(getString(R.string.enabled), gpsEnabled);
        String formattedNetworkEnabled = String.format(getString(R.string.enabled), networkEnabled);

        tvEnabledGPS.setText(formattedGpsEnabled);
        tvEnabledNet.setText(formattedNetworkEnabled);
    }

}











