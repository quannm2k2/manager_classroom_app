package com.example.managerclassroom.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.FragmentIntroBinding;

public class IntroFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentIntroBinding binding = FragmentIntroBinding.inflate(inflater, container, false);

        TextView txtIntroStart = binding.introStart;
        TextView txtIntro_feature1 = binding.txtIntroFeature1;
        TextView txtIntro_feature2 = binding.txtIntroFeature2;
        TextView txtIntro_feature3 = binding.txtIntroFeature3;
        TextView txtIntro_feature4 = binding.txtIntroFeature4;
        TextView txtIntroEnd = binding.introEnd;
        ImageView btnIntro = binding.btnBackIntro;

        String introStart = "<b>"+getString(R.string.manager_classroom)+"</b> "+getString(R.string.intro_start);
        String feature1 = "<b>"+getString(R.string.feature1)+"</b> "+getString(R.string.intro_feature1);
        String feature2 = "<b>"+getString(R.string.feature2)+"</b> "+getString(R.string.intro_feature2);
        String feature3 = "<b>"+getString(R.string.feature3)+"</b> "+getString(R.string.intro_feature3);
        String feature4 = "<b>"+getString(R.string.feature4)+"</b> "+getString(R.string.intro_feature4);
        String introEnd = "<b>"+getString(R.string.manager_classroom)+"</b> "+getString(R.string.intro_end);

        txtIntroStart.setText(HtmlCompat.fromHtml(introStart,HtmlCompat.FROM_HTML_MODE_COMPACT));
        txtIntro_feature1.setText(HtmlCompat.fromHtml(feature1,HtmlCompat.FROM_HTML_MODE_COMPACT));
        txtIntro_feature2.setText(HtmlCompat.fromHtml(feature2,HtmlCompat.FROM_HTML_MODE_COMPACT));
        txtIntro_feature3.setText(HtmlCompat.fromHtml(feature3,HtmlCompat.FROM_HTML_MODE_COMPACT));
        txtIntro_feature4.setText(HtmlCompat.fromHtml(feature4,HtmlCompat.FROM_HTML_MODE_COMPACT));
        txtIntroEnd.setText(HtmlCompat.fromHtml(introEnd,HtmlCompat.FROM_HTML_MODE_COMPACT));

        // back
        btnIntro.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        return binding.getRoot();
    }
}