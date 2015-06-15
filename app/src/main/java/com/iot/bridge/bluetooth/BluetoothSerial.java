package com.iot.bridge.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothSerial{
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

/*    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                }
                catch (IOException ex) {
                    showMessage("SEND FAILED");
                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }*/

    public void findBT(String deviceName) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            System.out.println("No bluetooth adapter available");
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
        System.out.println("Bluetooth Device Found");
    }

    public void openBT() throws IOException {
        UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"); //Standard //SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        //beginListenForData();
        System.out.println("Bluetooth Opened");
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
            for (int i = 0; i < bytesAvailable; i++) {
                byte b = packetBytes[i];
                if (b == delimiter) {
                    byte[] encodedBytes = new byte[readBufferPosition];
                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                    data = new String(encodedBytes, "US-ASCII");
                    readBufferPosition = 0;

                } else {
                    readBuffer[readBufferPosition++] = b;
                }
            }
        }
System.out.println(data);
            return data;
    }


    void sendData(String msg) throws IOException {
        mmOutputStream.write(msg.getBytes());
        System.out.println("Data Sent");
    }

   public void closeBT() throws IOException {
        //stopWorker = true;
       // mmOutputStream.close();
       // mmInputStream.close();
        mmSocket.close();
        System.out.println("Bluetooth Closed");
    }

    /*private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG)/160);
        msg.show();
    }*/
}