package com.example.managerclassroom.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivityForgotPassBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPass extends AppCompatActivity {

    Button btnSubmit;
    ImageView btnBackForgot;
    EditText edtForgotPass;
    ProgressBar progressBar;
    FirebaseAuth auth;
    String email;
    LinearLayout forgotLayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityForgotPassBinding binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        edtForgotPass = binding.edtForgotPass;
        btnSubmit = binding.btnSubmit;
        btnBackForgot = binding.btnBackForgot;
        progressBar = binding.pbForgotPass;
        forgotLayout = binding.forgotLayout;

        // Submit button click
        btnSubmit.setOnClickListener(v -> {
            email = edtForgotPass.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtForgotPass.setError(getString(R.string.email_invalid));
                Toast.makeText(ForgotPass.this, getString(R.string.email_pre_enter), Toast.LENGTH_SHORT).show();
            } else {
                boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
                if (!isInternetConnected) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                edtForgotPass.setError(null);
                hideKeyboard();
                resetPassWord();
            }
        });

        //Hide keyboard when touch main view
        forgotLayout.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        // Back
        btnBackForgot.setOnClickListener(v -> finish());
    }

    // Reset pass
    private void resetPassWord() {
        toggleLoginLoading(true);

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                            showAlertDialog(getString(R.string.forgot_pass_reset));
                            toggleLoginLoading(false);
                        })
                .addOnFailureListener(e -> {
                            Toast.makeText(ForgotPass.this,getString(R.string.error), Toast.LENGTH_SHORT).show();
                            toggleLoginLoading(false);
                        });
    }

    // Alert dialog
    private void showAlertDialog(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtDescDialog = dialog.findViewById(R.id.txtDescDialog);
        Button btnDialog = dialog.findViewById(R.id.btnDialog);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        txtDescDialog.setText(message);

        // Open email app
        btnDialog.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Cancel
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    // Loading Processing
    private void toggleLoginLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    //Hide keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
}