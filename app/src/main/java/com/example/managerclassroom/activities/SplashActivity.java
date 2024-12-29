package com.example.managerclassroom.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding splashBinding;
    Animation topAnim, bottomAnim;
    boolean darkMode;
    String langCode;
    SharedPreferences sharedPreferences;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy dữ liệu từ SharedPreferences
        sharedPreferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        darkMode = sharedPreferences.getBoolean("dark", false);
        langCode = sharedPreferences.getString("lang", "");

        // Cài đặt chế độ Dark/Light Mode
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Cài đặt ngôn ngữ
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Ánh xạ giao diện
        splashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(splashBinding.getRoot());

        // Gán animation
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        splashBinding.cardviewSplash.setAnimation(topAnim);
        splashBinding.txtSplash.setAnimation(bottomAnim);

        // Khởi tạo Handler
        handler = new Handler();
        int TIME = 3000;

        handler.postDelayed(() -> {
            if (!isFinishing()) {
                // Kiểm tra trạng thái người dùng
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent;

                if (user != null && user.isEmailVerified()) {
                    intent = new Intent(this, MainActivity.class);
                } else {
                    intent = new Intent(this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        }, TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
