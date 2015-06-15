package com.iot.bridge.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Aswin Prasanth on 5/10/2015.
 */
public class MqttBroker {
    private volatile IMqttAsyncClient mqttClient;
    private String deviceId;
    Context mContext;
    IMqttToken token;
    public MqttBroker(){
       // mContext=context;
        deviceId = MqttAsyncClient.generateClientId();
        Log.e("","Device ID:" + deviceId);
       // setClientID();
    }
    private void setClientID(){
        WifiManager wifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        deviceId = wInfo.getMacAddress();
        Log.e("", "Message arrived from deviceId "+deviceId );
        if(deviceId == null){
            deviceId = MqttAsyncClient.generateClientId();
        }
    }

    public void doConnect(String mqttUrl){
        MqttConnectOptions options = new MqttConnectOptions();
        Log.e("", "do connect");
        options.setCleanSession(true);
        options.setUserName("aswinprasanth.s@gmail.com");
        options.setPassword("2505052c".toCharArray());
        try {
            Log.e("","do connect");
            mqttClient = new MqttAsyncClient(mqttUrl, deviceId, new MemoryPersistence());
            Log.e("","do connect");
            token = mqttClient.connect(options);
            token.waitForCompletion(3500);
            Log.e("", "do connect");
            mqttClient.setCallback(new MqttEventCallback());
            Log.e("", "do connect");
            token.waitForCompletion(5000);
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

public String subscribeMqtt()
{
    try {
        token = mqttClient.subscribe("/aswinprasanth.s@gmail.com/testtopic", 0);
    }
    catch (MqttException e) {

    }
    return token.toString();
}
    public void publishMqtt(byte[] b)
    {

        Log.e("", "Message arrived from topic publishMqtt "+b.toString() );
        try {
            token = mqttClient.publish("/aswinprasanth.s@gmail.com/testtopic",b,1,Boolean.TRUE);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disConnect(){

        if (isOnline() && mqttClient != null && mqttClient.isConnected()) {
            try {
                token = mqttClient.disconnect();
                token.waitForCompletion(1000);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    private class MqttEventCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable arg0) {
            Log.e("", "Message arrived from topic connectionLost" );
            arg0.printStackTrace();

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.e("", "Message arrived from topic deliveryComplete" );

        }

        @Override
        @SuppressLint("NewApi")
        public void messageArrived(String topic, final MqttMessage msg) throws Exception {
            Log.e("", "Message arrived from topic" + topic);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getApplicationContext().getSystemService(mContext.getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }



}
