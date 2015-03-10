package brs.com.brs;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.TextView;

import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Calendar;


public class MainActivity extends Activity {
    boolean isConnected=false;

    int proximitySetting;
    int alertSetting;
    int themeSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceDetect.intializeDebug();
        DeviceDetect.debug("BRS Session Started:");
        DeviceDetect.debug(Integer.toString(Calendar.getInstance().get(Calendar.SECOND)));
        DeviceDetect.debug("MainActivity onCreate()");
        DeviceDetect.intializeSerial(this);
        try{
            DeviceDetect.connectToDevice();
        }catch(Exception e){
            createToast("Not Connected");
        }


        LinearLayout layout1 = (LinearLayout) findViewById(R.id.main_layout);
        //Preferences for saved data
        @SuppressWarnings("deprecation")
        final SharedPreferences myPrefs = this.getSharedPreferences(
                "myPrefs", MODE_WORLD_READABLE);


        proximitySetting = myPrefs.getInt("proximity", 1);



        alertSetting = myPrefs.getInt("alert", 1);


        themeSetting = myPrefs.getInt("theme", 0);

        //set current theme
        if(themeSetting==1){
            layout1.setBackgroundResource(R.drawable.background_main_light_2);
        }
    }

    @Override
    protected void onDestroy(){
        try {
            DeviceDetect.disconnectDevice();
        }catch (Exception no_connect){
            createToast("Internal error");
        }
    }


    @Override
    protected void onResume(){
        super.onResume();

        DeviceDetect.intializeSerial(this);
        try{
            DeviceDetect.connectToDevice();
        }catch(Exception e){
            createToast("Not Connected");
        }


        //Preferences for saved data
        @SuppressWarnings("deprecation")
        final SharedPreferences myPrefs = this.getSharedPreferences(
                "myPrefs", MODE_WORLD_READABLE);
        LinearLayout layout1 = (LinearLayout) findViewById(R.id.main_layout);
        themeSetting = myPrefs.getInt("theme", 0);
        if(themeSetting==1){
            layout1.setBackgroundResource(R.drawable.background_main_light_2);
        }
        else{
            layout1.setBackgroundResource(R.drawable.background_main_dark_2);
        }
    }



   public void graphicsdebug(View view){
       Intent intent = new Intent(this, GraphicsDebug.class);
       startActivity(intent);
   }


    public void settings(View view){
         Intent intent = new Intent(this, Settings.class);
         startActivity(intent);

    }

    public void start(View view){
        if(DeviceDetect.isConnected()){
            Intent intent = new Intent(this,StartDetect.class);
            startActivity(intent);
        }else{
            createToast("Not Connected");
        }

    }

    @Override
    public void onBackPressed() {
           //fixes button error
    }


    //method to create a toast message
    public void createToast(String message){
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
