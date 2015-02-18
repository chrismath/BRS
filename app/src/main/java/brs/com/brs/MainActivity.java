package brs.com.brs;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Connection portion
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        DeviceDetect.intializeSerial(usbManager);
        DeviceDetect.setPort();
        try{
            DeviceDetect.connectToDevice();
        }catch(Exception e){
            //do something if no_connect
        }


    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void searchDevice(View view){
        Intent intent = new Intent(this,DeviceDetect.class);
        startActivity(intent);

    }
    public void start(View view){
        Intent intent = new Intent(this,StartDetect.class);
        startActivity(intent);
    }
    public void settings(View view){
        Intent intent = new Intent(this,Settings.class);
        startActivity(intent);
    }
}
