package com.profilemanager.dipanjal.profilemanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button buttonStart,buttonStop;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.buttonStart=(Button)findViewById(R.id.button_start);
        this.buttonStop=(Button)findViewById(R.id.button_stop);
        this.textView=(TextView)findViewById(R.id.textView);

        //new ProfileManager(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        this.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //STARTING THE SERVICE
                startService(new Intent(getBaseContext(),TestService.class));


            }
        });

        this.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //STOPPING THE SERVICE
                stopService(new Intent(getBaseContext(),TestService.class));

            }
        });
    }
}
