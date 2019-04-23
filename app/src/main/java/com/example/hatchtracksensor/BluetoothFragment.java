package com.example.hatchtracksensor;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.UUID;

public class BluetoothFragment extends Fragment {

    private Button mButtonScan = null;

    private static final String SERVICE_UUID_STR = "e7f9840b-d767-4169-a3d0-a83b083669df";
    private static final String CHARACTERISTIC_UUID_STR = "8bdc835c-10fe-407f-afb0-b21926f068a7";
    private static final long SCAN_PERIOD_MS = 10000;
    private static final int REQUEST_ENABLE_BT = 1;
    private  static final int REQUEST_COARSE_LOCATION = 2;
    private static final String TAG = "MREUTMAN";

    private ProgressBar mSpinner;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner mBluetoothLeScanner = null;
    private BluetoothDevice mBluetoothDevice = null;
    private Handler mHandler = null;
    private  boolean mIsScanning = false;
    private String mPeepUUID = "";

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    gatt.requestMtu(200);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                }
            }

            public void onMtuChanged (BluetoothGatt gatt, int mtu, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.discoverServices();
                } else {
                    gatt.disconnect();
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "onServicesDiscovered received: " + status);
                    BluetoothGattService service = gatt.getService(
                            UUID.fromString(SERVICE_UUID_STR));
                    if (null != service) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                                UUID.fromString(CHARACTERISTIC_UUID_STR));
                        if (null != characteristic) {
                            gatt.readCharacteristic(characteristic);
                        } else {
                            gatt.disconnect();
                        }
                    } else {
                        gatt.disconnect();
                    }
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                Log.i(TAG, "onCharacteristicRead received: " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //String res = characteristic.getStringValue(0);
                    String res = characteristic.getStringValue(0);
                    try {
                        JSONObject jsonObject = new JSONObject(res);
                        mPeepUUID = jsonObject.getString("uuid");
                        //characteristic.setValue("blah blah blah");
                        //gatt.writeCharacteristic(characteristic)
                    }
                    catch (Exception e) {
                        gatt.disconnect();
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "SUCCESS!!");
                }
            }
    };


    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            Log.i("MREUTMAN", "Device Name: " + name + " rssi: " + result.getRssi() + "\n");

            if ((null != name) && (name.contains("ESP32"))) {
                mBluetoothDevice = device;
            }
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mLeScanCallback);

                    if (null != mBluetoothDevice) {
                        mButtonScan.setText("Configure");
                        Context context = getContext();
                        mBluetoothDevice.connectGatt(context, true, mGattCallback);
                    }
                }
            }, SCAN_PERIOD_MS);

            mIsScanning = true;
            mSpinner.setVisibility(View.VISIBLE);
            mButtonScan.setEnabled(false);
            mBluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mIsScanning = false;
            mSpinner.setVisibility(View.GONE);
            mButtonScan.setEnabled(true);
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        Activity activity = getActivity();
        Context context = getContext();

        mSpinner = activity.findViewById(R.id.progressBarScan);
        mSpinner.setVisibility(View.GONE);

        mButtonScan = activity.findViewById(R.id.buttonScan);
        mButtonScan.setEnabled(false);
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        PackageManager packageManager = getActivity().getPackageManager();

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mHandler = new Handler();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            int perm = activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (PackageManager.PERMISSION_GRANTED != perm) {
                activity.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION);
            }
            else {
                mButtonScan.setEnabled(true);
            }

            mButtonScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((null == mPeepUUID) || (mPeepUUID.isEmpty())) {
                        scanLeDevice(true);
                    } else {

                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mButtonScan.setEnabled(true);
                } else {
                    mButtonScan.setEnabled(false);
                }
                break;
        }
    }


}
