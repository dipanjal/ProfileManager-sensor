package com.profilemanager.dipanjal.profilemanager;

import android.app.ExpandableListActivity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Dipanjal on 4/18/2017.
 */

public class ProfileManager extends Service implements SensorEventListener {

    MainActivity context;
    TextView display;

    private boolean isActive=false;
    private boolean active;


    private boolean accSilent,accRinger,accVibrate;
    private boolean proxySilent,proxyRinger;
    private boolean lightSilent,lightRinger;


    private double valueX;
    private double valueY;
    private double valueZ;


    AudioManager audioManager; //For Switching Profiles
    SensorManager sensorManager;
    Sensor lightSensor,accelerometer,proximity;
    NotificationManager notificationManager;


    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();


        //CREATE AUDIO MANAGER
        this.audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        this.sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        this.notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //Create Sensors
        this.lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.proximity=sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        /**
         * FOR light.
         */
        if( lightSensor != null ){
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL); //Turning on Light Sensor
        }


        /**
         * FOR accelerometer.
         */
        if( accelerometer != null )
        {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL); //Turning on Accelerometer Sensor
        }

        if(this.proximity!=null)
        {
            sensorManager.registerListener(this,proximity,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        if(!this.isActive) //if service is not running : isActive==false
        {
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            this.isActive=true;

        }
        else
        {
            Toast.makeText(this, "Already Running", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            if(this.isActive)
            {
                Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
                sensorManager.unregisterListener(this);
                this.isActive=false;
            }
            else {
                Toast.makeText(this, "No Service Running", Toast.LENGTH_SHORT).show();
            }

        }catch (NullPointerException ex)
        {
            Log.d("onDestroy Ex",ex.getMessage());
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        //FOR LIGHT SENSOR
        /*if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            //if type LIGHT == true
//            lightSensorData.setText("" + event.values[0]);
            lightValue = event.values[0];

            if(lightValue < 8.0){

                this.lightSilent=true;
                this.lightRinger=false;

                //setVibrate();
                Log.d("Light","Low - setVibrate()");

            }else if(lightValue >= 10.0){

                this.lightSilent=false;
                this.lightRinger=true;

                //setRinging();
                Log.d("Light","Hight - setRinger()");

            }
        }*/


        //FOR ACCELEROMETER
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
//            mode.setText("sensor.TYPE_ACCELEROMETER Not Available.");
            valueX = event.values[0];
            valueY = event.values[1];
            valueZ = event.values[2];

//            valX.setText("Value X : " + valueX);
//            valY.setText("Value Y : " + valueY);
//            valZ.setText("Value Z : " + valueZ);


            if( (valueX <= 2.0 && valueX >= -1.5 ) && (valueY >= 0.5 && valueY <= 2.5) &&  valueZ >= 9.1 )
            {

                this.accRinger=true;
                this.accSilent=false;
                this.accVibrate=false;

                Log.d("Acclerameter","UPSIDE - setRinger()");
                //setRinging();

            }
            else if( (valueX >= -3.0 && valueX <= 3.0) && (valueY >= 0.5 && valueY <= 1.8) && (valueZ <= -7.5 && valueZ >= -9.0) )
            {

                this.accRinger=false;
                this.accSilent=false;
                this.accVibrate=true;
                setVibrate();
            }

            else if( valueZ <= 5.0 )
            {
                this.accRinger=false;
                this.accSilent=true;
                this.accVibrate=false;
                Log.d("Acclerameter","DownSide - setSilent()");
                //setSilent(); //DO NOT DISTURB

            }
            else
            {
//                mode.setText("No Mode Activated.");
            }
        }

        //FOR PROXIMITY
        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY)
        {
            if(event.values[0]==0)
            {
                Log.d("Proximity","Near - setSilent()");
                this.proxySilent=true;
                this.proxyRinger=false;
                //this.setSilent();
                //this.setVibrate();
            }
            else
            {
                Log.d("Proximity","Far - setRinger()");
                this.proxySilent=false;
                this.proxyRinger=true;
                //this.setRinging();
            }
        }




       /* if(this.proxySilent || this.lightSilent || this.accSilent)
        {
            this.setSilent();
            Log.d("Profile","Silent()");
        }
        if(this.proxyRinger || this.lightRinger || this.accRinger)
        {
            this.setRinging();
            Log.d("Profile","Ringer()");
        }
        this.resetFlags();*/


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void setVibrate(){
        try {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            Log.d("Profile","Vibrate");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Vibrate Ex",ex.getMessage());
        }
//        mode.setText("Mode : Vibration Mode Activated.");

    }


    public void setRinging()
    {
        try {
//        mode.setText("Mode : Ringing Mode Activated.");
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.d("Profile","Ringer");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Ringing Ex",ex.getMessage());
        }
    }


    public void setSilent(){
        try {
//        mode.setText("Mode : Silent Mode Activated.");
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.d("Profile","Silent");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Silent Ex",ex.getMessage());
            //IF THE CODE IS NOT WORKING FOR UPGRATED API
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted())
                {
                    //Toast.makeText(this, "Enable Profile Manager", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                }
            }
            catch (Exception e)
            {
                Log.d("Intent Ex:",e.getMessage());
            }
        }
    }

    public boolean isServiceRunning()
    {
        return this.isActive;
    }

    private void resetFlags()
    {
        this.lightRinger=false;
        this.lightSilent=false;

        this.accRinger=false;
        this.accSilent=false;
        this.accVibrate=false;

        this.proxySilent=false;
        this.proxyRinger=false;
    }
}
