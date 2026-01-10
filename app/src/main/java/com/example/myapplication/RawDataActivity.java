package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;
import android.view.LayoutInflater;

public class RawDataActivity extends AppCompatActivity {
    private DBHelper db;
    private LinearLayout llSptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);
        db = new DBHelper(this);

        TextView lblRawHeader = findViewById(R.id.lblRawHeader);
        EditText etSampleCode = findViewById(R.id.etSampleCode);
        EditText etDepth = findViewById(R.id.etDepth);
        EditText etN1 = findViewById(R.id.etN1);
        EditText etN2 = findViewById(R.id.etN2);
        EditText etN3 = findViewById(R.id.etN3);
        EditText etHammer = findViewById(R.id.etHammer);
        EditText etWater = findViewById(R.id.etWater);
        Button btnAddSpt = findViewById(R.id.btnAddSpt);
        llSptList = findViewById(R.id.llSptList);

        int borehole = AppSession.selectedBorehole;
        lblRawHeader.setText("Location: " + AppSession.editLocationName + " — Borehole " + borehole);

        btnAddSpt.setOnClickListener(v -> {
            String sample = etSampleCode.getText().toString().trim();
            String depthStr = etDepth.getText().toString().trim();
            String n1s = etN1.getText().toString().trim();
            String n2s = etN2.getText().toString().trim();
            String n3s = etN3.getText().toString().trim();
            if (depthStr.isEmpty() || n1s.isEmpty() || n2s.isEmpty() || n3s.isEmpty()) {
                Toast.makeText(RawDataActivity.this, "Please fill depth and n1,n2,n3", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                double depth = Double.parseDouble(depthStr);
                int n1 = Integer.parseInt(n1s);
                int n2 = Integer.parseInt(n2s);
                int n3 = Integer.parseInt(n3s);
                long id = db.insertSptData(AppSession.selectedBorehole, AppSession.editLocationId, sample, depth, n1, n2, n3);
                if (id > 0) {
                    Toast.makeText(RawDataActivity.this, "SPT added", Toast.LENGTH_SHORT).show();
                    etSampleCode.setText(""); etDepth.setText(""); etN1.setText(""); etN2.setText(""); etN3.setText("");
                    if (etHammer != null) etHammer.setText("");
                    if (etWater != null) etWater.setText("");
                    runOnUiThread(this::refreshSptList);
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(RawDataActivity.this, "Invalid number input", Toast.LENGTH_LONG).show();
            }
        });

        // initial load
        runOnUiThread(this::refreshSptList);
    }

    private void refreshSptList() {
        llSptList.removeAllViews();
        try (Cursor c = db.fetchSptDataByBorehole(AppSession.selectedBorehole, AppSession.editLocationId)) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String sample = c.getString(c.getColumnIndexOrThrow("sample_code"));
                double depth = c.getDouble(c.getColumnIndexOrThrow("depth"));
                int n1 = c.getInt(c.getColumnIndexOrThrow("n1"));
                int n2 = c.getInt(c.getColumnIndexOrThrow("n2"));
                int n3 = c.getInt(c.getColumnIndexOrThrow("n3"));

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0,8,0,8);
                row.setLayoutParams(rp);
                TextView t = new TextView(this);
                t.setText("#"+id+" " + sample + " — depth:"+depth+" n1:"+n1+" n2:"+n2+" n3:"+n3);
                row.addView(t);

                int nTotal = n1 + n2 + n3;
                double nAvg = nTotal / 3.0;
                String desc = describeSoil(nTotal, nAvg, depth);
                TextView meta = new TextView(this);
                meta.setText("N_total: " + nTotal + "  N_avg: " + String.format("%.1f", nAvg) + "  — " + desc);
                row.addView(meta);

                Button del = new Button(this);
                del.setText("Delete");
                del.setOnClickListener(v -> {
                    int removed = db.deleteSptData(id);
                    if (removed > 0) {
                        Toast.makeText(RawDataActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        runOnUiThread(this::refreshSptList);
                    }
                });
                row.addView(del);
                llSptList.addView(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String describeSoil(int nTotal, double nAvg, double depth) {
        // Simple classification based on SPT N-values and depth
        if (nTotal <= 4) return "Very soft / Very loose";
        if (nTotal <= 10) return "Soft / Loose";
        if (nTotal <= 30) return "Medium dense / Stiff";
        if (nTotal <= 50) return "Dense / Very stiff";
        return "Very dense / Very stiff";
    }
}
