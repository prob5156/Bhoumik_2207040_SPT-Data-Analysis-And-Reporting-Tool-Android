package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SubLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_login);

        EditText tfUser = findViewById(R.id.tfUserSub);
        EditText tfPass = findViewById(R.id.tfPassSub);
        Button btnLogin = findViewById(R.id.btnLoginSub);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = tfUser.getText().toString().trim();
                String pass = tfPass.getText().toString().trim();
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SubLoginActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simple fixed credential check for sub role
                final String SUB_USER = "sub";
                final String SUB_PASS = "sub123";
                if (SUB_USER.equals(user) && SUB_PASS.equals(pass)) {
                    AppSession.role = "SUB";
                    AppSession.clientName = user;
                    startActivity(new Intent(SubLoginActivity.this, ModifiersDashboardActivity.class));
                } else {
                    Toast.makeText(SubLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
