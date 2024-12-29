package com.example.managerclassroom.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.FragmentSettingsBinding;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    SwitchCompat swDarkMode;
    ImageView btnVN, btnUK, btnBackSettings;
    boolean darkMode;
    String langCodeDefault;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private FragmentSettingsBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        swDarkMode = binding.swDarkMode;
        btnVN = binding.btnVN;
        btnUK = binding.btnUK;
        btnBackSettings = binding.btnBackSettings;

        // get from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        darkMode = sharedPreferences.getBoolean("dark",false);
        langCodeDefault = sharedPreferences.getString("lang","");

        if(darkMode){
            swDarkMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // switch to dark mode
        swDarkMode.setOnClickListener(v -> {
            editor = sharedPreferences.edit();
            if(darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("dark", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("dark", true);
            }
            editor.apply();
            darkMode = !darkMode; // Cập nhật lại darkMode sau khi thay đổi

            resetActivity();
        });

        // set vietnamese
        btnVN.setOnClickListener(v -> {
            setLanguage("vi");
        });

        // set english
        btnUK.setOnClickListener(v -> {
            setLanguage("en");
        });

        // back
        btnBackSettings.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return binding.getRoot();
    }
    public void setLanguage(String langCode) {
        if (langCodeDefault.equals(langCode)) {
            return; // Dừng lại nếu ngôn ngữ đã đúng
        }
        // Cấu hình ngôn ngữ
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Lưu tùy chọn ngôn ngữ vào SharedPreferences
        editor = sharedPreferences.edit();
        editor.putString("lang", langCode);
        editor.apply();

        resetActivity(); // Yêu cầu Activity khởi động lại để áp dụng toàn bộ
    }

    private void resetActivity(){
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Đặt binding thành null khi Fragment bị hủy
    }
}