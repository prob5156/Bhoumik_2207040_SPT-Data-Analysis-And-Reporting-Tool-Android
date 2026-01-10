package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.content.Intent;


public class EditLocationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        DBHelper db = new DBHelper(this);

        EditText txfLocationName = findViewById(R.id.txfLocationName);
        EditText txfBoreHoles = findViewById(R.id.txfBoreHoles);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Populate fields from session
        if (AppSession.editLocationId > 0) {
            txfLocationName.setText(AppSession.editLocationName);
            txfBoreHoles.setText(String.valueOf(AppSession.editBoreHoles));
        }

        btnSave.setOnClickListener(v -> {
            if ("SUB".equalsIgnoreCase(AppSession.role)) {
                Toast.makeText(this, "Subconductor Engineer cannot edit locations.", Toast.LENGTH_LONG).show();
                return;
            }
            String name = txfLocationName.getText().toString().trim();
            String holesStr = txfBoreHoles.getText().toString().trim();
            if (name.isEmpty() || holesStr.isEmpty()) {
                Toast.makeText(this, "Location name and bore holes cannot be empty.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                int holes = Integer.parseInt(holesStr);
                int updated = db.updateLocation(AppSession.editLocationId, name, holes);
                if (updated > 0) {
                    AppSession.editLocationName = name;
                    AppSession.editBoreHoles = holes;
                    Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, BoreholeDashboardActivity.class));
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Bore holes must be a valid number.", Toast.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> startActivity(new Intent(this, ClientLocationsActivity.class)));
    }
}
