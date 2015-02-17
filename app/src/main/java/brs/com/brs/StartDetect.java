package brs.com.brs;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by lesterpi on 2/1/15.
 */

public class StartDetect extends Activity {
    Radial radial;
    String gtag = new String("graphics:");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Radial radial = new Radial(this);
        setContentView(radial);


    }
    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        radial.thread.stop();


    }

    public class Radial extends SurfaceView implements SurfaceHolder.Callback{
    int numPts = 200; // resolution
    radialThread thread;
    Bitmap bgr;
    Paint paint;
    int maxH; int maxW;



    public Radial(Context context){
        super(context);
        //Initalize bitmap parameters
        bgr = Bitmap.createBitmap(480,800,Bitmap.Config.ARGB_8888);
        maxH = bgr.getWidth();
        maxW = bgr.getHeight();
        paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setStrokeWidth(3);


        //Set canvas thread
        getHolder().addCallback(this);

    }

    @Override
    public void onDraw(Canvas canvas){
        float t1 = (float)maxH;
        float t2 = (float)maxW;
        canvas.drawBitmap(bgr,t2,t1,paint);
        Log.v(gtag,"ondraw accessed");
        canvas.drawLines(makeArc(canvas,2),paint);
        canvas.drawLines(makeArc(canvas,3), paint);
        canvas.drawLines(makeArc(canvas,4),paint);
        makeSectors(canvas,paint);


    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new radialThread(getHolder(),this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(gtag,"DESTROY");

        thread.setRunning(false);
        boolean retry = true;
        while(retry){
            try{
                thread.join();
                retry =false;

            }catch(InterruptedException e){

            }
        }
    }
    /*
    *   Thread updates frames for canvas
    */
    class radialThread extends Thread{
        private SurfaceHolder surfaceHolder; //underlying canvas for next frame
        private Radial mainView;
        private boolean run = false;

        //constructor
        public radialThread(SurfaceHolder surfaceHolder, Radial mainView){
            this.surfaceHolder = surfaceHolder;
            this.mainView      = mainView;
        }

        //on/off
        public void setRunning(boolean run){
            this.run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        @Override
        public void run(){
            Canvas c;
            while(run){
                c=null;
                try{
                    Thread.sleep(100);
                }catch (InterruptedException e){

                }

                try {
                    c = surfaceHolder.lockCanvas(null);
                    if(c!=null){
                      synchronized (surfaceHolder) {
                          mainView.onDraw(c);
                      }
                    }
                }finally{
                    if(c!=null){
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }


    }

}



    public float[] makeArc(Canvas canvas, float rad_div){
        int numPts =200;
        float[] arcArray = new float[numPts];
        for(int i=0;i<numPts; ++i){
            float angle;
            float numPtsf = (float) numPts;
            angle = (float)Math.PI*(i/numPtsf);
            arcArray[i]   = (canvas.getWidth()/rad_div)*(FloatMath.cos(angle)) + canvas.getWidth()/2;
            arcArray[++i] = (canvas.getHeight()/rad_div)* FloatMath.sin(angle);
            //Log.v(gtag, arcArray[i-1] + " " + arcArray[i]);
        }

        return arcArray;
    }

    public void makeSectors(Canvas canvas,Paint paint){
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        int maxW = canvas.getWidth();
        int maxH = canvas.getHeight();
        float thirty = (float)Math.PI/6;
        float angle = 0;
        for(int i = 0; i <7 ; ++i){
            float endX =  (maxW/2)*(FloatMath.cos(angle)+1);
            float endY =  (maxH/2)* FloatMath.sin(angle);
            canvas.drawText(String.format("%d",(int)(180*(angle/Math.PI))),
                    endX, (float)(1.2*endY),paint);
            //Log.v(gtag, endX + " " + endY);
            angle += thirty;
            canvas.drawLine(maxW/2,0,endX, endY,paint);
        }

    }

    public void GoBack(View view){

        finish();

    }

}
