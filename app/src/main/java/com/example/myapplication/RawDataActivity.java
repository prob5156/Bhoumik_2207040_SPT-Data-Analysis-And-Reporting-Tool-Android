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
import android.text.TextUtils;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class RawDataActivity extends AppCompatActivity {
    private DBHelper db;
    private LinearLayout llSptList;
    private View selectedRowView = null;

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
                    runOnUiThread(this::refreshSptList);
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(RawDataActivity.this, "Invalid number input", Toast.LENGTH_LONG).show();
            }
        });

        Button btnAnalysisBottom = findViewById(R.id.btnAnalysisBottom);
        btnAnalysisBottom.setOnClickListener(v -> {
            if (AppSession.selectedSptId > 0) {
                Log.d("RawDataActivity", "Launching AnalysisActivity with selectedSptId=" + AppSession.selectedSptId);
                Toast.makeText(RawDataActivity.this, "Launching Analysis for SPT id=" + AppSession.selectedSptId, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RawDataActivity.this, AnalysisActivity.class);
                i.putExtra("selectedSptId", AppSession.selectedSptId);
                startActivity(i);
            } else {
                Toast.makeText(RawDataActivity.this, "Please select an SPT row before Analysis", Toast.LENGTH_SHORT).show();
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

                // Build a horizontal row with weighted columns to align with header
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0,8,0,8);
                row.setLayoutParams(rp);

                // helper to create text cells
                TextView cSample = new TextView(this);
                LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f);
                cSample.setLayoutParams(lp0);
                cSample.setText(sample == null || sample.isEmpty() ? ("#"+id) : ("#"+id+" "+sample));
                cSample.setSingleLine(true);
                cSample.setEllipsize(TextUtils.TruncateAt.END);
                cSample.setPadding(8,4,8,4);
                row.addView(cSample);

                TextView cDepth = new TextView(this);
                cDepth.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                cDepth.setText(String.valueOf(depth));
                cDepth.setSingleLine(true);
                cDepth.setEllipsize(TextUtils.TruncateAt.END);
                cDepth.setPadding(6,4,6,4);
                row.addView(cDepth);

                TextView cN1 = new TextView(this);
                cN1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                cN1.setText(String.valueOf(n1));
                cN1.setSingleLine(true);
                cN1.setEllipsize(TextUtils.TruncateAt.END);
                cN1.setPadding(6,4,6,4);
                row.addView(cN1);

                TextView cN2 = new TextView(this);
                cN2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                cN2.setText(String.valueOf(n2));
                cN2.setSingleLine(true);
                cN2.setEllipsize(TextUtils.TruncateAt.END);
                cN2.setPadding(6,4,6,4);
                row.addView(cN2);

                TextView cN3 = new TextView(this);
                cN3.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                cN3.setText(String.valueOf(n3));
                cN3.setSingleLine(true);
                cN3.setEllipsize(TextUtils.TruncateAt.END);
                cN3.setPadding(6,4,6,4);
                row.addView(cN3);

                int nTotal = n1 + n2 + n3;
                TextView cTotal = new TextView(this);
                cTotal.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
                cTotal.setText(String.valueOf(nTotal));
                cTotal.setSingleLine(true);
                cTotal.setEllipsize(TextUtils.TruncateAt.END);
                cTotal.setPadding(6,4,6,4);
                row.addView(cTotal);

                double nAvg = nTotal / 3.0;
                String desc = describeSoil(nTotal, nAvg, depth);
                TextView cDesc = new TextView(this);
                cDesc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f));
                cDesc.setText(desc);
                cDesc.setSingleLine(true);
                cDesc.setEllipsize(TextUtils.TruncateAt.END);
                cDesc.setPadding(6,4,6,4);
                row.addView(cDesc);

                Button del = new Button(this);
                del.setText("Delete");
                del.setOnClickListener(v -> {
                    int removed = db.deleteSptData(id);
                    if (removed > 0) {
                        if (AppSession.selectedSptId == id) {
                            AppSession.selectedSptId = 0;
                            selectedRowView = null;
                        }
                        Toast.makeText(RawDataActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        runOnUiThread(this::refreshSptList);
                    }
                });
                
                // make the whole row selectable
                row.setClickable(true);
                row.setOnClickListener(v -> {
                    AppSession.selectedSptId = id;
                    // clear previous highlight
                    for (int i = 0; i < llSptList.getChildCount(); i++) {
                        View child = llSptList.getChildAt(i);
                        child.setBackgroundColor(Color.TRANSPARENT);
                    }
                    // highlight this row
                    row.setBackgroundColor(Color.LTGRAY);
                    selectedRowView = row;
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
