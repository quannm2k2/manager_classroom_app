package com.example.managerclassroom.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.activities.LoginActivity;
import com.example.managerclassroom.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding profileBinding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String id, username, email;
    ValueEventListener profileListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);

        // get data from MainActivity
        assert getArguments() != null;
        id = getArguments().getString("id");

        email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();

        // Lấy dữ liệu từ Firebase và lưu listener để hủy khi cần
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.getValue(String.class);
                    profileBinding.txtUsername.setText(username);
                    profileBinding.txtEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        database.getReference().child("users").child(id).child("profile").child("username")
                .addValueEventListener(profileListener);

        // Open settings
        profileBinding.cvSettings.setOnClickListener(v -> {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, settingsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Open introductions
        profileBinding.cvInfo.setOnClickListener(v -> {
            IntroFragment introFragment = new IntroFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frameLayout, introFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        //Logout
        profileBinding.cvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(getString(R.string.desc_logout));
            }
        });

        return profileBinding.getRoot();
    }

    // Alert dialog
    private void showAlertDialog(String message) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.logout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtDescDialog = dialog.findViewById(R.id.txtDescLogout);
        Button btnDialog = dialog.findViewById(R.id.btnDialogLogout);
        Button btnSkip = dialog.findViewById(R.id.btnSkipLogout);

        txtDescDialog.setText(message);

        // move to login page
        btnDialog.setOnClickListener(v -> {
            auth.signOut();
            // Clear back stack and start LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Skip
        btnSkip.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileListener != null) {
            database.getReference().child("users").child(id).child("profile").child("username")
                    .removeEventListener(profileListener);
        }
    }
}