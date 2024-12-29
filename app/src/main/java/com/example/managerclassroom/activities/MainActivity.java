package com.example.managerclassroom.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivityMainBinding;
import com.example.managerclassroom.fragments.HomeFragment;
import com.example.managerclassroom.fragments.ListRoomFragment;
import com.example.managerclassroom.fragments.NotificationFragment;
import com.example.managerclassroom.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    String id;
    FirebaseAuth auth;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Đặt ngôn ngữ trước khi gọi super.onCreate()
        SharedPreferences sharedPreferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        String langCode = sharedPreferences.getString("lang", "vi"); // "vi" là giá trị mặc định
        setLocale(langCode);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null)
            id = user.getUid();

        // Đặt Fragment mặc định
        replaceFragment(new HomeFragment());
        // Điều hướng sang Fragment chỉ định
        binding.bottomView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navHome) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.navControl) {
                replaceFragment(new ListRoomFragment());
            } else if (item.getItemId() == R.id.navNoti) {
                replaceFragment(new NotificationFragment());
            } else {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
    }

    // Thực hiện điều hướng
    private void replaceFragment(Fragment fragment) {
        Bundle bundle = new Bundle(); // container chứa các cặp key-value, giúp truyền dữ liệu giữa Activity và Fragment
        bundle.putString("id", id);
        Log.d("ID",id);
        fragment.setArguments(bundle); // truyền Bundle vào Fragment mới để lấy dữ liệu từ trong Fragment mới
        getSupportFragmentManager().beginTransaction() // Thay thế bằng Fragment mới
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    // Phương thức setLocale để thiết lập ngôn ngữ
    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}