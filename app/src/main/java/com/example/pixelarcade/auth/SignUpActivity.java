package com.example.pixelarcade.auth;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;
import com.example.pixelarcade.main.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private static final String WEB_CLIENT_ID = "77387860683-6liml9gb2ujv9pvj3qmtepjob2u8o9l0.apps.googleusercontent.com";
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);

        // Email/Password Sign Up
        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account created! Welcome!", Toast.LENGTH_SHORT).show();
                            UserDataManager.getInstance(this).pushAllToCloud();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "";
                            if (msg.contains("email address is already in use")) {
                                Toast.makeText(this, "This email is already registered. Try logging in!", Toast.LENGTH_LONG).show();
                            } else if (msg.contains("badly formatted")) {
                                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Sign up failed: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // Google Sign-Up (same flow as login — Firebase handles both)
        findViewById(R.id.btnGoogleSignUp).setOnClickListener(v -> signInWithGoogle());

        tvAlreadyAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new android.os.CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("Auth", "Google Sign-Up failed", e);
                        if (e instanceof androidx.credentials.exceptions.GetCredentialCancellationException) {
                            Toast.makeText(SignUpActivity.this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof androidx.credentials.exceptions.NoCredentialException) {
                            Toast.makeText(SignUpActivity.this, "No Google accounts found on this device", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Google Sign-In error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void handleSignInResult(GetCredentialResponse result) {
        try {
            if (result.getCredential() instanceof CustomCredential &&
                result.getCredential().getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

                GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                String idToken = credential.getIdToken();

                AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
                mAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                String name = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : "Player";
                                Toast.makeText(SignUpActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                                UserDataManager.getInstance(SignUpActivity.this).syncFromCloud(() -> {
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                });
                            } else {
                                Toast.makeText(SignUpActivity.this, "Firebase Auth Failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Auth", "Error handling sign-in result", e);
            Toast.makeText(this, "Error processing sign-in", Toast.LENGTH_SHORT).show();
        }
    }
}
