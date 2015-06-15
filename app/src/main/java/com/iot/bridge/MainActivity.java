package com.iot.bridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;


public class MainActivity extends Activity  {
    private Switch mySwitch;
    Intent serviceIntent;
    SensorService myService;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ListView mConversationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(MainActivity.this, SensorService.class);

        mConversationView = (ListView) findViewById(R.id.in);
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);



        mySwitch = (Switch) findViewById(R.id.but_sensor);

        //set the switch to ON
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {


                if(isChecked){
                    startService(serviceIntent); //Starting the service
                    bindService(serviceIntent, mConnection,            Context.BIND_AUTO_CREATE); //Binding to the service!
                }else{
                    unbindService(mConnection);
                    stopService(serviceIntent);
                    mConversationArrayAdapter.add("onServiceDisconnected called");
                }

            }
        });


    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            mConversationArrayAdapter.add("onServiceconnected called");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mConversationArrayAdapter.add("onServiceDisconnected called");
        }
    };

}
