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
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;

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

        // generate borehole buttons
        vboxBoreholes.removeAllViews();
        androidx.recyclerview.widget.RecyclerView rc = findViewById(R.id.rcBoreholes);
        BoreholeAdapter adapter = new BoreholeAdapter(num -> { AppSession.selectedBorehole = num; startActivity(new Intent(BoreholeDashboardActivity.this, RawDataActivity.class)); });
        rc.setAdapter(adapter);
        rc.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        adapter.setCount(boreHoles);

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

    private final int[] STICKER_PALETTE = new int[] {
            0xFF6EC6FF,
            0xFFFF8A65,
            0xFFAED581,
            0xFFF48FB1,
            0xFFFFF176,
            0xFFB39DDB
    };

    private void applyStickerStyle(Button btn, int index) {
        int color = STICKER_PALETTE[index % STICKER_PALETTE.length];
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        float radius = getResources().getDisplayMetrics().density * 12f;
        gd.setCornerRadius(radius);
        int stroke = (int)(getResources().getDisplayMetrics().density * 0.5f);
        gd.setStroke(stroke, 0x22000000);
        btn.setBackground(gd);
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        btn.setTextColor(luminance > 0.6 ? 0xFF222222 : Color.WHITE);
        int pad = (int)(getResources().getDisplayMetrics().density * 12f);
        btn.setPadding(pad, pad, pad, pad);
        btn.setAllCaps(false);
        btn.setElevation(getResources().getDisplayMetrics().density * 4f);
    }
}
