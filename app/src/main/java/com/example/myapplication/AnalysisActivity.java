package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DBHelper;

public class AnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        TextView tv = findViewById(R.id.analysisText);
        if (tv != null) {
            DBHelper db = new DBHelper(this);
            int clients = 0;
            android.database.Cursor cClients = db.fetchClients();
            if (cClients != null) {
                clients = cClients.getCount();
                cClients.close();
            }

            int sptCount = 0;
            android.database.Cursor cSpt = db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM spt_data", null);
            if (cSpt != null) {
                if (cSpt.moveToFirst()) sptCount = cSpt.getInt(0);
                cSpt.close();
            }

            tv.setText("Clients: " + clients + "\nSPT entries (total): " + sptCount);
        }
    }
}
