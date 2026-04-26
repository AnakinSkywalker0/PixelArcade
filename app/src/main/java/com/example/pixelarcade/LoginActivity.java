package com.example.pixelarcade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLoginSubmit.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, R.string.auth_setup_pending, Toast.LENGTH_SHORT).show());

        tvCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}

