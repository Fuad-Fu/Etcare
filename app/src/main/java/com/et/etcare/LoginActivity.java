package com.et.etcare;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp, tvOfflineIndicator;
    private ProgressBar pbLoading;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        pbLoading = findViewById(R.id.pbLoading);
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator);

        btnLogin.setOnClickListener(v -> loginUser());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        checkConnectivity();
    }

    private void checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected && tvOfflineIndicator != null) {
            tvOfflineIndicator.setVisibility(View.VISIBLE);
        } else if (tvOfflineIndicator != null) {
            tvOfflineIndicator.setVisibility(View.GONE);
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) { etEmail.setError("Required"); return; }
        if (password.isEmpty()) { etPassword.setError("Required"); return; }

        btnLogin.setVisibility(View.INVISIBLE);
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvOfflineIndicator != null) tvOfflineIndicator.setVisibility(View.GONE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setVisibility(View.VISIBLE);
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        checkConnectivity();
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ForgotPasswordDialogTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        EditText emailInput = dialogView.findViewById(R.id.etResetEmail);

        builder.setView(dialogView)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}