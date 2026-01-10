package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DBHelper db = new DBHelper(this);

        EditText tfUser = findViewById(R.id.tfUser);
        EditText tfPass = findViewById(R.id.tfPass);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSenior = findViewById(R.id.btn_senior);
        Button btnSub = findViewById(R.id.btn_sub);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = tfUser.getText().toString().trim();
                String pass = tfPass.getText().toString().trim();
                if (phone.isEmpty() || pass.isEmpty()) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Validation Error")
                            .setMessage("Please enter phone number and password.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                try (Cursor c = db.fetchClients()) {
                    boolean found = false;
                    while (c.moveToNext()) {
                        String dbPhone = c.getString(c.getColumnIndexOrThrow("phone"));
                        String dbPass = c.getString(c.getColumnIndexOrThrow("password"));
                        String dbName = c.getString(c.getColumnIndexOrThrow("name"));
                        int dbId = c.getInt(c.getColumnIndexOrThrow("id"));
                        if (dbPhone.equals(phone) && dbPass.equals(pass)) {
                            AppSession.role = "CLIENT";
                            AppSession.clientId = dbId;
                            AppSession.clientName = dbName;
                            AppSession.phoneNumber = phone;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Authentication Failed")
                                .setMessage("Phone number or password is incorrect.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // Navigate to client locations activity
                    startActivity(new Intent(LoginActivity.this, ClientLocationsActivity.class));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Login error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSenior.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SeniorLoginActivity.class)));
        btnSub.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SubLoginActivity.class)));
    }
}
