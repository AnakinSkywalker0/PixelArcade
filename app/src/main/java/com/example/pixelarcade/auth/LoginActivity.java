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

public class LoginActivity extends AppCompatActivity {

    private static final String WEB_CLIENT_ID = "77387860683-6liml9gb2ujv9pvj3qmtepjob2u8o9l0.apps.googleusercontent.com";
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Email/Password Login
        btnLoginSubmit.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            UserDataManager.getInstance(this).syncFromCloud(() -> {
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            });
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "";
                            if (msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("no user record") || msg.contains("credential")) {
                                Toast.makeText(this, "Account does not exist or wrong password. Please sign up first!", Toast.LENGTH_LONG).show();
                            } else if (msg.contains("badly formatted")) {
                                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Login failed: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        // Google Sign-In
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first, then tap Forgot Password", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Could not send reset email. Check your email address.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
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
                        Log.e("Auth", "Google Sign-In failed | Type: " + e.getType(), e);
                        
                        if (e instanceof androidx.credentials.exceptions.GetCredentialCancellationException) {
                            Toast.makeText(LoginActivity.this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof androidx.credentials.exceptions.NoCredentialException) {
                            // This usually means the SHA-1 is missing in Firebase or the account isn't logged in
                            String msg = "No Google accounts found. Please ensure you are logged into Google and your SHA-1 is added to Firebase.";
                            Log.w("Auth", "NoCredentialException: This is often a developer setup error (SHA-1).");
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        } else {
                            // Show the technical type to help us debug
                            String technicalType = e.getType().substring(e.getType().lastIndexOf('.') + 1);
                            Toast.makeText(LoginActivity.this, "Google Error (" + technicalType + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                                Toast.makeText(LoginActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                                UserDataManager.getInstance(LoginActivity.this).syncFromCloud(() -> {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, "Firebase Auth Failed.", Toast.LENGTH_SHORT).show();
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
