package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;


public class EditClientActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_client);
        DBHelper db = new DBHelper(this);

        EditText txfClientName = findViewById(R.id.txfClientName);
        EditText txfPhoneNumber = findViewById(R.id.txfPhoneNumber);
        EditText txfPasswordVisible = findViewById(R.id.txfPasswordVisible);
        Button btnSaveClient = findViewById(R.id.btnSaveClient);
        Button btnCancelClient = findViewById(R.id.btnCancelClient);

        // populate from session
        txfClientName.setText(AppSession.clientName);
        txfPhoneNumber.setText(AppSession.phoneNumber);
        txfPasswordVisible.setText(AppSession.clientPassword);

        btnSaveClient.setOnClickListener(v -> {
            if ("SUB".equalsIgnoreCase(AppSession.role)) {
                Toast.makeText(this, "Subconductor Engineer cannot edit client details.", Toast.LENGTH_LONG).show();
                return;
            }
            String name = txfClientName.getText().toString().trim();
            String phone = txfPhoneNumber.getText().toString().trim();
            String pwd = txfPasswordVisible.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_LONG).show();
                return;
            }
            int updated = db.updateClient(AppSession.clientId, name, phone, pwd);
            if (updated > 0) {
                AppSession.clientName = name;
                AppSession.phoneNumber = phone;
                AppSession.clientPassword = pwd;
                Toast.makeText(this, "Client updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ModifiersDashboardActivity.class));
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelClient.setOnClickListener(v -> startActivity(new Intent(this, ModifiersDashboardActivity.class)));
    }
}
