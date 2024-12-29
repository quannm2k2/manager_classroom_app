package com.example.managerclassroom.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivityLoginBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signUpRedirect, txtForgetPass;
    ImageButton btnHidePassword;
    LinearLayout loginLayout;
    ProgressBar pbLogin;
    private  boolean hidePass = true;
    private String email, pass;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginLayout = binding.loginLayout;
        loginButton = binding.loginButton;
        loginEmail = binding.loginUsername;
        loginPassword = binding.loginPassword;
        signUpRedirect = binding.txtSignupRedirect;
        btnHidePassword = binding.btnHidePassword;
        txtForgetPass = binding.txtForgetPass;
        pbLogin = binding.pbLogin;

        // Handle Login
        loginButton.setOnClickListener(v -> {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
            email = loginEmail.getText().toString().trim();
            pass = loginPassword.getText().toString().trim();

            if (isInputValid(email, pass)) {
                if (!isInternetConnected) {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    return;
                }
                loginButton.setVisibility(View.GONE);
                pbLogin.setVisibility(View.VISIBLE);
                checkUser();
            }
            hideKeyboard();
        });

        //signup redirect text click event
        signUpRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        //Show or hide password
        btnHidePassword.setOnClickListener(v -> {
            String password = loginPassword.getText().toString();
            hidePass = !hidePass;
            if(!password.isEmpty()){
                if (!hidePass) { //Show password
                    loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnHidePassword.setImageResource(R.drawable.visibility_off);
                } else { //Hide password
                    loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnHidePassword.setImageResource(R.drawable.visibility);
                }
            }
        });

        //Hide keyboard when touch main view
        loginLayout.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        // Forgot password text click
        txtForgetPass.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPass.class);
            startActivity(intent);
        });
    }

    public boolean isInputValid(String email, String pass){
        if (email.isEmpty()) {
            loginEmail.setError(getString(R.string.email_empty));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError(getString(R.string.email_format_error));
            return false;
        }
        if (pass.isEmpty()) {
            loginPassword.setError(getString(R.string.pass_empty));
            return false;
        }
        loginEmail.setError(null);
        loginPassword.setError(null);
        return true;
    }

    // Check information User
    public void checkUser() {
        toggleLoginLoading(true);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> handleLoginSuccess())
                .addOnFailureListener(this::handleLoginFailure);
    }

    private void handleLoginSuccess() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            Toast.makeText(LoginActivity.this,
                            getString(R.string.login_success),
                            Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            sendVerificationEmail(user); // Gửi lại email xác minh nếu chưa xác minh
        }
    }

    private void handleLoginFailure(Exception e) {
        try {
            throw e;
        } catch (FirebaseAuthInvalidUserException ex) {
            loginEmail.setError(getString(R.string.user_check));
            loginEmail.requestFocus();
        } catch (FirebaseAuthInvalidCredentialsException ex) {
            loginPassword.setError(getString(R.string.user_info_check));
            Toast.makeText(LoginActivity.this,
                    getString(R.string.user_info_check),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(LoginActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        toggleLoginLoading(false);
    }

    // Phương thức gửi lại email xác minh
    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(unused ->
                            showAlertDialogEmail())
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginActivity.this,
                                            getString(R.string.email_send_failed),
                                            Toast.LENGTH_SHORT).show());
        }
        toggleLoginLoading(false);
    }

    private void showAlertDialogEmail() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtDescDialog = dialog.findViewById(R.id.txtDescDialog);
        Button btnDialog = dialog.findViewById(R.id.btnDialog);
        Button btnSkip = dialog.findViewById(R.id.btnCancel);

        txtDescDialog.setText(getString(R.string.email_verification));
        btnDialog.setText(getString(R.string.email_verified));

        // Open email app
        btnDialog.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // skip
        btnSkip.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    //Hide keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    // Check if user already logged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, getText(R.string.please_login), Toast.LENGTH_SHORT).show();
        }
    }

    // Loading Processing
    private void toggleLoginLoading(boolean isLoading) {
        pbLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}