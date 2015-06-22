package com.iot.bridge;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.iot.bridge.bluetooth.BluetoothSerial;
import com.iot.bridge.bluetooth.MqttBroker;

import java.io.IOException;

/**
 * Created by Aswin Prasanth on 5/9/2015.
 */
public class SensorService extends Service {

BluetoothSerial bluetoothSerial;
    MqttBroker mqttBroker;
    boolean stopWorker=false;
    Thread workerThread;

    public SensorService() {
        this.bluetoothSerial= new BluetoothSerial();
        this.mqttBroker=new MqttBroker();
  }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
   @Override
    public void onCreate() {
        Toast.makeText(this, "The Sensor Service was Created", Toast.LENGTH_LONG).show();
        bluetoothSerial.findBT("HC-05");
        mqttBroker.doConnect("tcp://mqtt.dioty.co:1883");


    }

//   @Override
// public void onStart(Intent intent, int startId) {
        // For time consuming an long tasks you can launch a new thread here...

 //       Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();

//    }
//
//    @Override
//    public void onDestroy() {
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
//
//    }
//}

//    NotificationManager notificationManager;
//    NotificationCompat.Builder mBuilder;
//    Callbacks activity;
    private long startTime = 0;
    private long millis = 0;
    private final IBinder mBinder = new LocalBinder();
//    Handler handler = new Handler();
//    Runnable serviceRunnable = new Runnable() {
//        @Override
//        public void run() {
//            millis = System.currentTimeMillis() - startTime;
//            activity.updateClient(millis); //Update Activity (client) by the implementd callback
//            handler.postDelayed(this, 1000);
//        }
//    };




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Do what you need in onStartCommand when service has been started
        String test = "this is a test message";
        byte[] b= test.getBytes();
        mqttBroker.publishMqtt(b);
        Log.e("",test);
        try {
            bluetoothSerial.openBT();
            //bluetoothSerial.sendData("test");
            workerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            String data=bluetoothSerial.beginListenForData();
                            if(data != null)
                            {
                                byte[] b= data.getBytes();
                                mqttBroker.publishMqtt(b);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            stopWorker=true;
            bluetoothSerial.closeBT();
            //mqttBroker.disConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public SensorService getServiceInstance() {
            return SensorService.this;
        }
    }

//    //Here Activity register to the service as Callbacks client
//    public void registerClient(Activity activity) {
//        this.activity = (Callbacks) activity;
//    }
//
//    public void startCounter() {
//        startTime = System.currentTimeMillis();
//        handler.postDelayed(serviceRunnable, 0);
//        Toast.makeText(getApplicationContext(), "Counter started", Toast.LENGTH_SHORT).show();
//    }
//
//    public void stopCounter() {
//        handler.removeCallbacks(serviceRunnable);
//    }
//
//
//    //callbacks interface for communication with service clients!
//    public interface Callbacks {
//        public void updateClient(long data);
//    }
}