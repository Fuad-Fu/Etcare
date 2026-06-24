package com.et.etcare;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ProgressBar pbLoading;
    private TextView tvOfflineIndicator;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
            return;
        }

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        pbLoading = findViewById(R.id.pbLoading);
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator);

        btnSignUp.setOnClickListener(v -> registerUser());

        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
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

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (email.isEmpty()) { etEmail.setError("Required"); return; }
        if (password.isEmpty()) { etPassword.setError("Required"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 chars"); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError("No match"); return; }

        btnSignUp.setVisibility(View.INVISIBLE);
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvOfflineIndicator != null) tvOfflineIndicator.setVisibility(View.GONE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates);
                            
                            // Initialize user data in Firestore with default Amharic preference if requested
                            saveUserToFirestore(user.getUid(), name, email);
                        }
                    } else {
                        btnSignUp.setVisibility(View.VISIBLE);
                        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                        checkConnectivity();
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("language", "am"); // Defaulting to Amharic as requested

        db.collection("users").document(uid).set(userMap)
                .addOnCompleteListener(task -> {
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                });
    }
}