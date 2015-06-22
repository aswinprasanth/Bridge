package com.iot.bridge.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothSerial{
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket,mmSocketfallback;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    private boolean useFallback = false;
    public void findBT(String deviceName) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Log.e("","No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                Log.e("device.getName()",device.getName());
                if(device.getName().equals(deviceName)) {
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.e("","Bluetooth Device Found");
    }

    public void openBT() throws IOException {
        UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"); //Standard //SerialPortService ID
        mBluetoothAdapter.cancelDiscovery();
        /*
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        Log.e("", "Bluetooth Device Connecting");
        try
        {
            mmSocket.connect();
        }
        catch(Exception e)
        {
            Log.e("",e.getMessage());
        }*/
        try {
                // Instantiate a BluetoothSocket for the remote device and connect it.
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
                } catch (Exception e1) {
                  Log.e("", "There was an error while establishing Bluetooth connection. Falling back..");
                  Class<?> clazz = mmSocket.getRemoteDevice().getClass();
                  Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                  try {
                        Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                        Object[] params = new Object[]{Integer.valueOf(1)};
                      mmSocketfallback = (BluetoothSocket) m.invoke(mmSocket.getRemoteDevice(), params);
                      mmSocketfallback.connect();
                      mmOutputStream = mmSocketfallback.getOutputStream();
                      mmInputStream = mmSocketfallback.getInputStream();
                      } catch (Exception e2) {
                      Log.e("", "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                        //stopService();
                        return;
                      }
        }

        //mmOutputStream = mmSocketfallback.getOutputStream();
        //mmInputStream = mmSocketfallback.getInputStream();
        ///beginListenForData();
        Log.e("","Bluetooth Opened");
    }

    public String beginListenForData() throws IOException{
        final byte delimiter = 10; //This is the ASCII code for a newline character
        stopWorker = false;
        String data = null;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        int bytesAvailable = mmInputStream.available();
         if(bytesAvailable > 0) {
            byte[] packetBytes = new byte[bytesAvailable];
            mmInputStream.read(packetBytes);
             data = new String(packetBytes, "US-ASCII");
             Log.e("", "Ardunio data:" + data);
        }
            return data;
    }


  public  void sendData(String msg) throws IOException {
        mmOutputStream.write(msg.getBytes());
        Log.e("","Data Sent");
    }

   public void closeBT() throws IOException {
        //stopWorker = true;
       mmOutputStream.close();
       mmInputStream.close();
        mmSocket.close();
       Log.e("","Bluetooth Closed");
    }
}