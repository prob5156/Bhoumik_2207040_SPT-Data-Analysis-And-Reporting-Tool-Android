package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize DB and seed sample data if empty
        DBHelper db = new DBHelper(this);
        try (android.database.Cursor c = db.fetchClients()) {
            if (!c.moveToNext()) {
                // No clients — seed sample data
                long cid = db.insertClient("Sample Client", "0123456789", "pass123");
                if (cid > 0) {
                    db.insertLocation((int)cid, "Site A", 3);
                    db.insertLocation((int)cid, "Site B", 2);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        findViewById(R.id.btn_dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            }
        });
    }
}