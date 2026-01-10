package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;

import android.widget.EditText;

public class BoreholeDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borehole_dashboard);
        DBHelper db = new DBHelper(this);

        TextView lblHeader = findViewById(R.id.lblHeader);
        TextView lblLocationName = findViewById(R.id.lblLocationName);
        TextView lblBoreHoles = findViewById(R.id.lblBoreHoles);
        Button btnEditLocation = findViewById(R.id.btnEditLocation);
        Button btnBack = findViewById(R.id.btnBack);
        LinearLayout vboxBoreholes = findViewById(R.id.vboxBoreholes);
        EditText tfSearchBoreholes = findViewById(R.id.tfSearchBoreholes);

        if ("SENIOR".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Senior Executive Engineer");
        else if ("SUB".equalsIgnoreCase(AppSession.role)) lblHeader.setText("Subconductor Engineer");
        else lblHeader.setText("");

        int locationId = AppSession.editLocationId;
        String locationName = AppSession.editLocationName;
        int boreHoles = AppSession.editBoreHoles;

        lblLocationName.setText(locationName);
        lblBoreHoles.setText(String.valueOf(boreHoles));

        btnEditLocation.setVisibility("CLIENT".equalsIgnoreCase(AppSession.role) ? View.GONE : View.VISIBLE);

        btnEditLocation.setOnClickListener(v -> startActivity(new Intent(BoreholeDashboardActivity.this, EditLocationActivity.class)));
        btnBack.setOnClickListener(v -> startActivity(new Intent(BoreholeDashboardActivity.this, ClientLocationsActivity.class)));

        // generate borehole buttons
        vboxBoreholes.removeAllViews();
        for (int i = 1; i <= boreHoles; i++) {
            final int boreholeNum = i;
            Button b = new Button(this);
            b.setText("Borehole " + i);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(0,8,0,8);
            b.setLayoutParams(p);
            b.setMinHeight(120);
            b.setAllCaps(false);
            b.setOnClickListener(v -> {
                AppSession.selectedBorehole = boreholeNum;
                startActivity(new Intent(BoreholeDashboardActivity.this, RawDataActivity.class));
            });
            vboxBoreholes.addView(b);
        }

        tfSearchBoreholes.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String q = s == null ? "" : s.toString().trim().toLowerCase();
                for (int idx=0; idx<vboxBoreholes.getChildCount(); idx++) {
                    View child = vboxBoreholes.getChildAt(idx);
                    if (child instanceof Button) {
                        Button btn = (Button) child;
                        String name = btn.getText() == null ? "" : btn.getText().toString().toLowerCase();
                        btn.setVisibility(q.isEmpty() || name.contains(q) ? View.VISIBLE : View.GONE);
                    }
                }
            }
        });
    }
}
