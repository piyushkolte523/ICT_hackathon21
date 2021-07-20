package com.example.chronology;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SlpashActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME = 1500;

    //variables
    Animation top_logo_Anim, bottom_text_anim;
    ImageView logoImg;
    TextView bottom_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_slpash);

        top_logo_Anim = AnimationUtils.loadAnimation(this, R.anim.top_logo_anim);
        bottom_text_anim = AnimationUtils.loadAnimation(this, R.anim.bottom_text_anim);

        logoImg = findViewById(R.id.tifrSplashLogo);
        bottom_text = findViewById(R.id.tifrSplashText);

        logoImg.setAnimation(top_logo_Anim);
        bottom_text.setAnimation(bottom_text_anim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN_TIME);
    }
}