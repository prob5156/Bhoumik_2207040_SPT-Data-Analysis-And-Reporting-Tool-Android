package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SeniorLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_login);

        EditText tfUser = findViewById(R.id.tfUserSenior);
        EditText tfPass = findViewById(R.id.tfPassSenior);
        Button btnLogin = findViewById(R.id.btnLoginSenior);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = tfUser.getText().toString().trim();
                String pass = tfPass.getText().toString().trim();
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SeniorLoginActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simple fixed credential check for senior
                final String ADMIN_USER = "senior";
                final String ADMIN_PASS = "senior123";
                if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
                    AppSession.role = "SENIOR";
                    AppSession.clientName = user;
                    startActivity(new Intent(SeniorLoginActivity.this, ModifiersDashboardActivity.class));
                } else {
                    Toast.makeText(SeniorLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
