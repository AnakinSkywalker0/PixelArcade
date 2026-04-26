package com.example.pixelarcade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);

        btnCreateAccount.setOnClickListener(v ->
                Toast.makeText(this, R.string.auth_setup_pending, Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnGoogleSignUp).setOnClickListener(v ->
                Toast.makeText(this, R.string.auth_setup_pending, Toast.LENGTH_SHORT).show());

        tvAlreadyAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

