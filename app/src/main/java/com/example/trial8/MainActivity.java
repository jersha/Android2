package com.example.trial8;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, index_page.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Intent intent = new Intent(MainActivity.this, index_page.class);
        startActivity(intent);
        finish();
        return false;
    }
}