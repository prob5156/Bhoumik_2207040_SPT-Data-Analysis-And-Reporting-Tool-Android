package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ClientDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);
        DBHelper db = new DBHelper(this);

        android.widget.EditText tfClientName = findViewById(R.id.tfClientName);
        android.widget.EditText tfPhoneNumber = findViewById(R.id.tfPhoneNumber);
        android.widget.EditText tfPassword = findViewById(R.id.tfPassword);
        android.widget.Button btnSubmitClient = findViewById(R.id.btnSubmitClient);

        btnSubmitClient.setOnClickListener(v -> {
            if ("SUB".equalsIgnoreCase(AppSession.role)) {
                android.widget.Toast.makeText(this, "Subconductor Engineer cannot create new clients.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            String clientName = tfClientName.getText().toString().trim();
            String phone = tfPhoneNumber.getText().toString().trim();
            String pwd = tfPassword.getText().toString().trim();
            if (clientName.isEmpty() || phone.isEmpty() || pwd.isEmpty()) {
                android.widget.Toast.makeText(this, "All fields are required.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            long id = db.insertClientWithRemote(clientName, phone, pwd);
            if (id > 0) {
                AppSession.clientId = (int) id;
                AppSession.clientName = clientName;
                AppSession.phoneNumber = phone;
                android.widget.Toast.makeText(this, "Client created", android.widget.Toast.LENGTH_SHORT).show();
                startActivity(new android.content.Intent(this, ModifiersDashboardActivity.class));
            } else {
                android.widget.Toast.makeText(this, "Failed to create client", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // back navigation handled by system back button; no explicit Back UI control
    }
}
