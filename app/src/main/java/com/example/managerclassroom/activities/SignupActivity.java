package com.example.managerclassroom.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivitySignupBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    LinearLayout signupLayout;
    EditText signUpEmail, signUpPassword, signUpConfirm, signUpName;
    TextView txtLoginRedirect;
    Button signUpButton;
    ImageButton btnHidePassword, btnHideConfirmPass;
    ProgressBar pbSignup;
    private String email, pass, confirmPass, name;
    private boolean hidePass = true;
    private boolean hideConfPass = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySignupBinding binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        signupLayout = binding.signupLayout;
        signUpButton = binding.signupButton;
        btnHidePassword = binding.btnHidePass;
        btnHideConfirmPass = binding.btnHideConfirmPass;
        signUpEmail = binding.signupEmail;
        signUpPassword = binding.signupPassword;
        signUpName = binding.signupName;
        txtLoginRedirect = binding.txtLoginRedirect;
        signUpConfirm = binding.signupConfirmPass;
        pbSignup = binding.pbSignUp;

        //Show or hide password
        btnHidePassword.setOnClickListener(v -> {
            String password = signUpPassword.getText().toString();
            hidePass = !hidePass;
            if(!password.isEmpty()){
                if (!hidePass) { //Show password
                    signUpPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnHidePassword.setImageResource(R.drawable.visibility_off);
                } else { //Hide password
                    signUpPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnHidePassword.setImageResource(R.drawable.visibility);
                }
            }
        });

        //Show or hide confirm password
        btnHideConfirmPass.setOnClickListener(v -> {
            String password = signUpPassword.getText().toString();
            hideConfPass = !hideConfPass;
            if(!password.isEmpty()){
                if (!hideConfPass) { //Show password
                    signUpConfirm.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnHideConfirmPass.setImageResource(R.drawable.visibility_off);
                } else { //Hide password
                    signUpConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnHideConfirmPass.setImageResource(R.drawable.visibility);
                }
            }
        });

        //Signup button click
        signUpButton.setOnClickListener(v -> {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
            // Show progress bar
            toggleLoading(true);
            hideKeyboard();

            // Lấy thông tin từ các trường
            confirmPass = signUpConfirm.getText().toString().trim();
            email = signUpEmail.getText().toString().trim();
            pass = signUpPassword.getText().toString().trim();
            name = signUpName.getText().toString().trim();

            // Kiểm tra điều kiện đầu vào
            if(isInternetConnected)
            {
                if (isInputValid(name, email, pass, confirmPass)){
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(this::handleSignupResult);
                }
                toggleLoading(false);
            } else {
                toggleLoading(false);
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });

        //Login redirect click
        txtLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        //Hide keyboard when touch main view
        signupLayout.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    // Phương thức kiểm tra điều kiện đầu vào
    private boolean isInputValid(String name, String email, String pass, String confirmPass) {
        if (name.isEmpty()) {
            signUpName.setError(getString(R.string.name_empty));
            return false;
        }
        if (email.isEmpty()) {
            signUpEmail.setError(getString(R.string.email_empty));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpEmail.setError(getString(R.string.email_format_error));
            return false;
        }
        if (pass.isEmpty()) {
            signUpPassword.setError(getString(R.string.pass_empty));
            return false;
        }
        if (pass.length() < 6) {
            signUpPassword.setError(getString(R.string.pass_lenght));
            return false;
        }
        if (confirmPass.isEmpty()) {
            signUpConfirm.setError(getString(R.string.confpass_empty));
            return false;
        }
        if (!confirmPass.equals(pass)) {
            signUpConfirm.setError(getString(R.string.pass_match));
            return false;
        }
        return true;
    }

    // Phương thức xử lý kết quả Signup
    private void handleSignupResult(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            createUserDatabase(); // Khởi tạo dữ liệu User
        } else {
            String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
            showAlertDialog(getString(R.string.warning), errorMessage);
            toggleLoading(false);
        }
    }

    // Khởi tạo user và lưu vào Database Firebase
    private void createUserDatabase() {
        FirebaseUser user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        if (user != null) {
            HashMap<String, Object> map = new HashMap<>();
            String id = user.getUid();
            email = user.getEmail();
            map.put("id", id);
            map.put("username", name);
            map.put("email", email);
            database.getReference().child("users").child(id).child("profile").setValue(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "User profile created successfully!");
                            Toast.makeText(SignupActivity.this, getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("Firebase", "Failed to create user profile.", task.getException());
                        }
                    });
        }
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Hide keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    // Process Loading UI
    private void toggleLoading(boolean isLoading) {
        signUpButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        pbSignup.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}