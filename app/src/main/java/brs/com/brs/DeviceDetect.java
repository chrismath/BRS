package brs.com.brs;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import java.util.Queue;

/**
 * Created by jake on 1/17/15.
 */
public class DeviceDetect extends Activity {
    private static final String TAG = "DeviceDetect";

    //USB VARS
    private UsbManager mUsbManager;
    private UsbSerialPort mPort;
    private UsbDeviceConnection mConnection;
    private TextView infoView;
    private int packetSize =10;

/*Byte signals*/
    private final byte sig_start = (byte) 0xFF;
    private final byte sig_error = (byte) 0xEF;
    private final byte sig_kill  = (byte) 0xEE;
    private final IOException no_write;
    private final IOException no_read;

    {
        no_write = new IOException("nw");
        no_read = new IOException("nr");
    }


    /*-----------MAIN  FN'S--------------*/
    /*
      getPorts()
            -gets manager for driver
            -driver gets list of ports
    */
    protected List<UsbSerialPort> getPorts(){
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> mDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        List<UsbSerialPort> ports = null;

        if(mDrivers.isEmpty()){
            return ports;
        }else{
            sucess_message("Found driver");
        }
        ports = mDrivers.get(0).getPorts();
        return ports;


    }


    /*
    *  connectToDevice()
    *        -opens connection to port
    */
    protected void connectToDevice(UsbSerialPort port){
        UsbDeviceConnection mConnection = mUsbManager.openDevice(port.getDriver().getDevice());
        try{
            port.open(mConnection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        }catch (IOException e){
            failure_message("Couldn't connect through port");
        }



    }


/* writePort()
*       Parameters: byte signal
*       Returns: Void
*       Protocol:
*            -Converts Sig to Byte array
*            -Wites for 0.5 sec
*       Errors
*             -IO exeception
 */
    protected void writePort(byte sig) throws IOException{
        byte[] sig_out = new byte[1];
        sig_out[0] = sig;
        try{
            mPort.write(sig_out,200);
        }catch (IOException e4){
            failure_message("Couldn't write");
            throw no_write;
        }

    }



/* readPort()
*      Parameters: N/A
*      Returns: String (for now)
*      Protocol:
*              -Buffers 32 bytes
*              -Reads for 0.2 secs
*              -Return HEX string
*      Errors:
*              -IO exception
*/
    protected byte[] readPort() throws IOException{
        byte buffer_in[] = new byte[32];
        try {
            int numBytesRead = mPort.read(buffer_in, 200);
            if(numBytesRead >0){
               //String message = "Bytes:" + HexDump.dumpHexString((buffer_in)) + "\n";
               return buffer_in;
            }
        } catch (IOException e6) {
                failure_message("Couldn't Read/Write");
                try {
                    mPort.close();
                } catch (IOException e7) {
                    //ignore
                }
                throw no_read;

        }
         return buffer_in;
    }




/*decode()
 *      Parameters: Byte Buffer
 *      Returns: List of longs
 *      Protocol:
 *           -Read through buffer find 0xFF
 *           -Parse, convert, and insert data 
 */
     protected String decode(byte[] buffer_in ){
               String output ="";
               Integer i =0;
               for( ;i<buffer_in.length ; ++i){
                   if(buffer_in[i] == sig_start){
                      break; // find start signal
                   }
                }   
                ++i;
                int j = 0;
                while( j < 2 && i < buffer_in.length && buffer_in[i] != sig_kill) {
                       long lo = (long)(buffer_in[i]);
                       long hi = (long) (buffer_in[i+1] );
                            hi = hi << 4;
                       long temp = lo +hi;
                       output = HexDump.toHexString(buffer_in[i]) + HexDump.toHexString(buffer_in[i+1]) + " = ";

                       output =  output + " " + temp + "  ";
                      i+=2;
                      ++j;
                }

                return output + "\n";

     }



/*  
*   Runnable and Handler for communication with arduino
*/
    final Handler readHandle = new Handler(){
        @Override
        public void handleMessage(Message msg){
            // on a message of sig kill send to arduino
            // and stop running read write
             try {
                 if(mPort != null) {
                     writePort((byte) msg.arg1);
                 }
             }catch(IOException no_write){
                    //do nothing
             }
            readHandle.removeCallbacks(readRun);

            }

    };

    final Runnable readRun = new Runnable() {
        @Override
        public void run() {
            try {
                writePort(sig_start);
                infoView.append(decode(readPort()));
                //Should auto scroll
               /*
                int scrollAmount = infoView.getLayout().getLineTop(infoView.getLineCount()) - infoView.getHeight();
                if (scrollAmount > 0) {
                    infoView.scrollTo(0, scrollAmount);
                } else {
                    infoView.scrollTo(0, 0);
                }
*/
                readHandle.postDelayed(this, 0);                     // calls same thread in .2 secs
            }catch(IOException e){
                failure_message(e.getMessage());
            }
        }

    };






/*--------- TEST INTERFACE ---------*/



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detect);
        infoView =(TextView)findViewById(R.id.infoMessage);
        infoView.setText("Information:\n S1:\tS2:\tS3:\tS4:\tS5:\tS6:\t \n");
        infoView.setTextSize(20);
        infoView.setMovementMethod(new ScrollingMovementMethod()); //enables scrolling for interface


        /*Setup Connection
         *   -Find Drivers
         *   -Connect with driver
         *   -Connect to a port
         */

        if(getPorts() != null){
            mPort = getPorts().get(0);
        }else{
            failure_message("Couldn't get a port");
            return;
        }
        connectToDevice(mPort);




    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.




    }
    @Override
    protected void onResume() {
        super.onResume();
        //pass to next activity
        //parcelable for mport
        Intent i = new Intent(this,StartDetect.class);


        //If no port connect to port
        if (mPort == null) {
            if (getPorts() != null) {
                mPort = getPorts().get(0);
                connectToDevice(mPort);

            } else {
                failure_message("Couldn't get a port");
                return;
            }
        }else{
            readHandle.postDelayed(readRun,0);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        Message msg = readHandle.obtainMessage();
        msg.arg1 = sig_kill;
        readHandle.handleMessage(msg);

    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        //readHandle.handleMessage(sig_kill);
        //readHandle.removeCallbacks(readRun);

        Message msg = readHandle.obtainMessage();
        msg.arg1 = sig_kill;
        readHandle.handleMessage(msg);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Message msg = readHandle.obtainMessage();
        msg.arg1 = sig_kill;
        readHandle.handleMessage(msg);

        // The activity is about to be destroyed.
    }

/*-----------UTIL FN'S-------------*/

    public void failure_message(String warn){
        TextView failureView = (TextView)findViewById(R.id.failureMessage);
        failureView.setText("Failure" + warn);
        failureView.setTextSize(40);
    }

    public void sucess_message(String warn){
        TextView successView = (TextView)findViewById(R.id.sucessMessage);
        successView.append("Sucess" + warn);
        successView.setTextSize(40);
    }




}
