package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnterNewClientActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_new_client);
        DBHelper db = new DBHelper(this);

        EditText txfNewLocationName = findViewById(R.id.txfNewLocationName);
        EditText txfNewBoreHoles = findViewById(R.id.txfNewBoreHoles);
        Button btnSaveNewLocation = findViewById(R.id.btnSaveNewLocation);
        Button btnCancelNewLocation = findViewById(R.id.btnCancelNewLocation);

        btnSaveNewLocation.setOnClickListener(v -> {
            String name = txfNewLocationName.getText().toString().trim();
            String holesStr = txfNewBoreHoles.getText().toString().trim();
            if (name.isEmpty() || holesStr.isEmpty()) {
                Toast.makeText(this, "Please enter name and number of bore holes", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                int holes = Integer.parseInt(holesStr);
                long id = db.insertLocation(AppSession.clientId > 0 ? AppSession.clientId : 1, name, holes);
                if (id > 0) {
                    Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show();
                    AppSession.editLocationId = (int) id;
                    AppSession.editLocationName = name;
                    AppSession.editBoreHoles = holes;
                    startActivity(new Intent(this, ClientLocationsActivity.class));
                } else {
                    Toast.makeText(this, "Failed to add location", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Invalid number for bore holes", Toast.LENGTH_LONG).show();
            }
        });

        btnCancelNewLocation.setOnClickListener(v -> startActivity(new Intent(this, ClientLocationsActivity.class)));
    }
}
