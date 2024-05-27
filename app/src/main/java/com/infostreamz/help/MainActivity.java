package com.infostreamz.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView pbtn;
    private Button hbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbtn=findViewById(R.id.pbtn);
        pbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                PrefManager npf= new PrefManager(MainActivity.this);
                npf.setFirstTimeLaunch(true);
                startActivity(new Intent(MainActivity.this,WelcomeActivity.class));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        hbtn=findViewById(R.id.hbtn);
        hbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
