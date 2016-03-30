package lsi.pryv.epfl.pryvironic.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;
import co.lujun.lmbluetoothsdk.base.State;
import lsi.pryv.epfl.pryvironic.R;
import lsi.pryv.epfl.pryvironic.utils.BluetoothPrinter;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothController mBluetoothController;

    private Button btnScan, btnOpen;
    private TextView tvBTState;
    private ListView lvDevices;
    private List<String> mList;
    private BaseAdapter mFoundAdapter;
    private String mMacAddress;

    private static final String TAG = "LMBluetoothSdk";

    private BluetoothListener mListener = new BluetoothListener() {
        @Override
        public void onActionStateChanged(int preState, int state) {
            tvBTState.setText("Bluetooth state: " + BluetoothPrinter.transBtStateAsString(state));
        }

        @Override
        public void onActionDiscoveryStateChanged(String discoveryState) {
            if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Toast.makeText(BluetoothActivity.this, "Scanning...", Toast.LENGTH_SHORT).show();
            } else if (discoveryState.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Toast.makeText(BluetoothActivity.this, "Scan finished!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActionScanModeChanged(int preScanMode, int scanMode) {
            Log.d(TAG, "preScanMode:" + preScanMode + ", scanMode:" + scanMode);
        }

        @Override
        public void onBluetoothServiceStateChanged(int state) {
            Log.d(TAG, "bluetooth service state:" + state);
            if (state == State.STATE_CONNECTED) {
                Intent intent = new Intent(BluetoothActivity.this, ChatActivity.class);
                startActivityForResult(intent, 4);
            }
        }

        @Override
        public void onActionDeviceFound(BluetoothDevice device, short rssi) {
            String label = device.getName() + "@" + device.getAddress();
            if(!mList.contains(label)) {
                mList.add(label);
                mFoundAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onReadData(final BluetoothDevice device, final byte[] data) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        getSupportActionBar().setTitle("Classic Bluetooth Discovery");
        init();
    }

    private void initBT(){
        mBluetoothController = BluetoothController.getInstance().build(this);
        mBluetoothController.setAppUuid(UUID.fromString("fa87c0d0-afac-12de-8a39-0450200c9a66"));
        mBluetoothController.setBluetoothListener(mListener);

        tvBTState.setText("Bluetooth state: "
                + BluetoothPrinter.transBtStateAsString(mBluetoothController.getBluetoothState()));
    }

    private void init(){
        mList = new ArrayList<String>();
        mFoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);
        
        btnScan = (Button) findViewById(R.id.btn_scan);
        btnOpen = (Button) findViewById(R.id.btn_open_bt);
        tvBTState = (TextView) findViewById(R.id.tv_bt_state);
        lvDevices = (ListView) findViewById(R.id.lv_devices);

        lvDevices.setAdapter(mFoundAdapter);
        initBT();

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // for Android 6.0+, request dangerous permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    int permission = ActivityCompat.checkSelfPermission(
                            BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                    }
                }

                mList.clear();
                mFoundAdapter.notifyDataSetChanged();
                if(!mBluetoothController.startScan()){
                    Toast.makeText(BluetoothActivity.this, "Start scan failed!",
                            Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BluetoothActivity.this, "Start scan success!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothController.isEnabled()) {
                    mBluetoothController.openBluetooth();
                } else {
                    Toast.makeText(BluetoothActivity.this, "Bluetooth has opened!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemStr = mList.get(position);
                mMacAddress = itemStr.substring(itemStr.length() - 17);
                Intent intent = new Intent(BluetoothActivity.this, ChatActivity.class);
                intent.putExtra("name", itemStr.substring(0, itemStr.length() - 18));
                intent.putExtra("mac", mMacAddress);
                startActivityForResult(intent, 4);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4){
            if (mBluetoothController != null){
                mBluetoothController.release();
            }
            mBluetoothController.build(this);
            mBluetoothController.setBluetoothListener(mListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothController.release();
    }
}

