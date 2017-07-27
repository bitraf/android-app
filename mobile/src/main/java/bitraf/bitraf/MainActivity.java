package bitraf.bitraf;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import bitraf.bitraf.api.DoorRequest;
import bitraf.bitraf.api.DoorRequestIntentService;
import bitraf.bitraf.geofence.RegisterBitrafGeoFenceIntentService;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_FINE_LOCATION_PERMISSION_CODE = 1;
    private BroadcastReceiver broRec = createUnlockedBroReceiver();
    private Switch geofenceSwitch;
    private Button unlockFrontnLabButton;
    private Button unlock3FloorButton;
    private Button unlock4FloorButton;
    private Button clearCredentialsButton;
    private EditText username;
    private EditText passwd;
    private ProgressBar progress;
    private TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        fillInputFieldsWithStoredCredentials();
        setupUnlockButtons();
        setupGeofenceSwitch();
        setupClearCredentialsButton();
        setupActivateLink();

    }

    private void setupActivateLink(){
        welcome.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupClearCredentialsButton() {
        clearCredentialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Storage.clearCredentials();
                fillInputFieldsWithStoredCredentials();
                showLoginUI();

            }
        });
    }

    private void setupGeofenceSwitch() {
        geofenceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (needToAskForPermission()) {
                        askForPermission();
                    }else{
                        Storage.setIfGeofenceEnabled(true);
                        enableGeofence();
                    }
                } else {
                    disableGeofence();
                }
            }

        });
        geofenceSwitch.setChecked(Storage.getIfGeofenceEnabled());
    }

    private void setupUnlockButtons() {
        unlockFrontnLabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                Storage.storeUsername(getUsername());
                Storage.storePassword(getPasswd());
                requestUnlocking(DoorRequestIntentService.ACTION_UNLOCK_FRONTNLAB);
            }
        });
        unlock3FloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                Storage.storeUsername(getUsername());
                Storage.storePassword(getPasswd());
                requestUnlocking(DoorRequestIntentService.ACTION_UNLOCK_F3);
            }
        });
        unlock4FloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                Storage.storeUsername(getUsername());
                Storage.storePassword(getPasswd());
                requestUnlocking(DoorRequestIntentService.ACTION_UNLOCK_F4);
            }
        });
    }

    private void requestUnlocking(final String unlockAction) {
        Intent unlock = new Intent(getApplicationContext(), DoorRequestIntentService.class);
        unlock.setAction(unlockAction);
        unlock.putExtra(DoorRequestIntentService.USERNAME_EXTRA, getUsername());
        unlock.putExtra(DoorRequestIntentService.PASWD_EXTRA, getPasswd());
        startService(unlock);
    }

    private void fillInputFieldsWithStoredCredentials() {
        username.setText(Storage.retreiveUsername());
        passwd.setText(Storage.retreivePassword());
    }

    private void askForPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION_CODE);
    }

    private boolean needToAskForPermission() {
        return Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }



    private void findViews() {
        geofenceSwitch = (Switch) findViewById(R.id.geofenceSwitch);
        unlockFrontnLabButton = (Button) findViewById(R.id.openDoorButtonFront);
        unlock3FloorButton = (Button) findViewById(R.id.openDoorButtonThirdFloor);
        unlock4FloorButton = (Button) findViewById(R.id.openDoorButtonFourthFloor);
        clearCredentialsButton = (Button) findViewById(R.id.clearCredentialsButton);
        username = (EditText) findViewById(R.id.username);
        passwd = (EditText) findViewById(R.id.passwd);
        progress = (ProgressBar) findViewById(R.id.progress);
        welcome = (TextView) findViewById(R.id.welcome);
    }

    @NonNull
    private BroadcastReceiver createUnlockedBroReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(DoorRequestIntentService.RESPONSE_UNLOCK_SUCCESS.equals(intent.getAction())){
                    Storage.storeHasSuccessfullCredentials();
                    showLoginUI();
                    Toast.makeText(getApplicationContext(),"Unlocked door JA",Toast.LENGTH_SHORT).show();
                }
                else if(DoorRequestIntentService.RESPONSE_UNLOCK_FAIL.equals(intent.getAction())){
                    showLoginUI();
                    Toast.makeText(getApplicationContext(),"Unlocked door FEIL",Toast.LENGTH_SHORT).show();
                }
                else if(DoorRequestIntentService.RESPONSE_UNLOCK_ERROR.equals(intent.getAction())){
                    showLoginUI();
                    Toast.makeText(getApplicationContext(),"Unlocked door ERROR",Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void disableGeofence() {
        Storage.setIfGeofenceEnabled(false);
        Intent registerGeoFence = new Intent(getApplicationContext(),RegisterBitrafGeoFenceIntentService.class);
        registerGeoFence.setAction(RegisterBitrafGeoFenceIntentService.REMOVE_GEOFENCE_ACTION);
        startService(registerGeoFence);
    }

    private void enableGeofence() {
        Storage.setIfGeofenceEnabled(true);
        Intent registerGeoFence = new Intent(getApplicationContext(),RegisterBitrafGeoFenceIntentService.class);
        registerGeoFence.setAction(RegisterBitrafGeoFenceIntentService.REGISTER_GEOFENCE_ACTION);
        startService(registerGeoFence);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        if(requestCode == REQUEST_FINE_LOCATION_PERMISSION_CODE){
            if ( Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[0]) && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED){
                enableGeofence();
            }
        }
    }

    private void showProgress() {
        unlockFrontnLabButton.setVisibility(View.GONE);
        unlock3FloorButton.setVisibility(View.GONE);
        unlock4FloorButton.setVisibility(View.GONE);
        username.setVisibility(View.GONE);
        passwd.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }


    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broRec,new IntentFilter(DoorRequestIntentService.RESPONSE_UNLOCK_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(broRec,new IntentFilter(DoorRequestIntentService.RESPONSE_UNLOCK_FAIL));
        LocalBroadcastManager.getInstance(this).registerReceiver(broRec,new IntentFilter(DoorRequestIntentService.RESPONSE_UNLOCK_ERROR));
        showLoginUI();

    }

    private void showLoginUI() {
        if(Storage.hasSuccessfullCredentials()){
            welcome.setVisibility(View.GONE);
        }
        unlockFrontnLabButton.setVisibility(View.VISIBLE);
        unlock3FloorButton.setVisibility(View.VISIBLE);
        unlock4FloorButton.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        passwd.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }

    public void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broRec);
        super.onPause();
    }


    public String getUsername() {
        return username.getText().toString();
    }

    public String getPasswd() {
        return passwd.getText().toString();
    }


}
