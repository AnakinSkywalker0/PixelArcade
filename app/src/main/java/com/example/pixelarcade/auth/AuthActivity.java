package com.example.pixelarcade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pixelarcade.R;
import com.example.pixelarcade.auth.LoginActivity;
import com.example.pixelarcade.auth.SignUpActivity;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}

