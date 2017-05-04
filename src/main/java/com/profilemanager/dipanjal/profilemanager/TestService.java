package com.profilemanager.dipanjal.profilemanager;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Dipanjal on 4/29/2017.
 */

public class TestService extends Service implements SensorEventListener {

    //MANAGERS
    AudioManager audioManager;
    SensorManager sensorManager;
    NotificationManager notificationManager;

    //SERSORS
    Sensor lightSensor,accelerometerSensor,proximitySensor;

    //FLAGS
    private boolean onSurface=true,upsideDown=false,unknownState=false;
    private boolean isProximityRunning=false;
    private boolean isActive=false; //FOR CHECKING IS SERVICE IS ALREADY RUNNING ?

    //VALUE TRACKERS
    private float lightValue;

    //FLAGS
    private int priority=0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //INSTANTIATING MANGERS
        this.audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        this.sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        this.notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        //CREATING SENSORS
        this.proximitySensor=this.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        this.accelerometerSensor=this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.lightSensor=this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        try
        {
            //REGISTERING SENSORS
            this.sensorManager.registerListener(this,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);

        }catch (NullPointerException e)
        {
            Log.d("Sensor","REGISTRATION FAILURE");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!this.isActive) //if service is not running : isActive==false
        {
            Toast.makeText(this, "TEST Service Started", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "TEST Service Destroyed", Toast.LENGTH_LONG).show();
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
    public void onSensorChanged(SensorEvent event)
    {
        //this.unknownState=false;
        /*LIGHT SENSOR*/
        /*if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            //if type LIGHT == true
//            lightSensorData.setText("" + event.values[0]);
            if(this.priority==1)
            {
                lightValue = event.values[0];

                if(lightValue < 8.0)
                {

                    Log.d("LIGHT"," LOW - SET VIBRATE()"+this.priority);

//                Toast.makeText(MainActivity.this, "Now in Vibration Mode.", Toast.LENGTH_SHORT);
                    //setVibrate();

                }else if(lightValue >= 10.0)
                {
                    Log.d("LIGHT"," HIGHT - SET RINGING()"+this.priority);
                    //this.priority=1;
//                Toast.makeText(MainActivity.this, "Now in Vibration Mode.", Toast.LENGTH_LONG);
                    //setRinging();

                }
            }

        }*/





        /*ACCELEROMETER SENSOR*/
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            float valueX = event.values[0];
            float valueY=event.values[1];
            float valueZ=event.values[2];
            if((valueX <= 4.0 && valueX>=-4.0) && (valueY<=5.0 && valueY>-2.5) && valueZ>=9.0 )
            {

                this.priority=1;
                this.onSurface=true;
                this.upsideDown=false;
                this.unknownState=false;
                this.isProximityRunning=false;
                Log.d("State","OnSurface - SET RINGING() Priority - "+this.priority+"__"+this.onSurface);

            }

            else if(valueZ<=-5.0 )
            {
                this.onSurface=false;
                this.upsideDown=true;
                this.unknownState=false;
                this.isProximityRunning=false;
                this.priority=3;
                Log.d("State","DownFace - DO NOT DISTURB () "+this.priority+"__"+this.onSurface);
                this.setSilent();


            }
            else if( (valueZ>=-2.0&&valueZ<=8.5) && !this.isProximityRunning ) //THIS IS MOVEMENT STATE
            {
                this.unknownState=true;
                Log.d("State","MOVEMENT - Pass Decission to Proximity"+this.priority+"__"+this.onSurface);
                this.setRinging();

            }
        }

        if(event.sensor.getType()==Sensor.TYPE_PROXIMITY)
        {
            if(this.unknownState)
            {
                if(event.values[0]==0)
                {
                    Log.d("Proximity"," NEAR - SET VIBRATE()"+this.priority+"__"+this.unknownState);
                    this.setVibrate();
                }
                else
                {
                    Log.d("Proximity"," FAR - SET RINGER()"+this.priority+"__"+this.unknownState);
                    this.setRinging();
                }
                this.isProximityRunning=true;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setVibrate(){
        try {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            //Log.d("Profile","Vibrate");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Vibrate Ex",ex.getMessage());
        }
    }


    public void setRinging()
    {
        try {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            //Log.d("Profile","Ringer");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Ringing Ex",ex.getMessage());
        }
    }


    public void setSilent(){
        try {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            //Log.d("Profile","Silent");
            SystemClock.sleep(30);
        }catch (Exception ex)
        {
            Log.d("Silent Ex",ex.getMessage());
            //IF THE CODE IS NOT WORKING FOR UPGRATED API
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted())
                {
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
}
