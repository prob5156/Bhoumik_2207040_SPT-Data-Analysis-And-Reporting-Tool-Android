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
        // adjust table heights in landscape so they expand to available vertical space
        android.content.res.Configuration cfg = getResources().getConfiguration();
        if (cfg.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            android.view.View listTable2 = findViewById(R.id.listTable2);
            android.view.View listTable3 = findViewById(R.id.listTable3);
            if (listTable2 != null && listTable3 != null) {
                int screenH = getResources().getDisplayMetrics().heightPixels;
                // give each table about 40% of screen height
                int target = (int) (screenH * 0.4f);
                android.view.ViewGroup.LayoutParams p2 = listTable2.getLayoutParams();
                android.view.ViewGroup.LayoutParams p3 = listTable3.getLayoutParams();
                p2.height = target;
                p3.height = target;
                listTable2.setLayoutParams(p2);
                listTable3.setLayoutParams(p3);
            }
        }
        if (tv == null) return;

        DBHelper db = new DBHelper(this);

        // Determine selected SPT id (prefer Intent extra if provided)
        int selectedSptId = getIntent().getIntExtra("selectedSptId", AppSession.selectedSptId);
        // If selected SPT exists, produce detailed analysis similar to desktop controller
        if (selectedSptId > 0) {
            StringBuilder out = new StringBuilder();
            out.append("Table-1 (Percent -> Term)\n");
            out.append("<5%: Trace, 5-10%: Few, 10-25%: Little, 25-45%: Some, 45-100%: Mostly\n\n");

            out.append("Table-2 (Total SPT -> Condition when Sand>Clay)\n");
            out.append("0-4: Very Loose, 4-10: Loose, 10-25: Medium Dense, 25-50: Dense, >50: Very Dense\n\n");

            out.append("Table-3 (Total SPT -> Condition when Sand<Clay)\n");
            out.append("0-2: Very Soft, 2-4: Soft, 4-8: Medium Stiff, 8-16: Stiff, 16-30: Very Stiff, >30: Hard\n\n");

            try {
                android.database.sqlite.SQLiteDatabase rdb = db.getReadableDatabase();
                android.database.Cursor spt = rdb.rawQuery("SELECT * FROM spt_data WHERE id=?", new String[]{String.valueOf(selectedSptId)});
                if (! (spt != null && spt.moveToFirst())) {
                    out.append("Selected SPT record not found.\n");
                    tv.setText(out.toString());
                    if (spt != null) spt.close();
                    return;
                }

                double depth = spt.getDouble(spt.getColumnIndexOrThrow("depth"));
                int boreholeId = spt.getInt(spt.getColumnIndexOrThrow("borehole_id"));
                int locationId = spt.getInt(spt.getColumnIndexOrThrow("location_id"));

                out.append(String.format("Selected SPT id=%d depth=%.2f (borehole=%d, location=%d)\n\n", selectedSptId, depth, boreholeId, locationId));

                // find visual classification covering this depth
                android.database.Cursor vc = rdb.rawQuery(
                        "SELECT * FROM visual_classification WHERE borehole_id=? AND location_id=? AND from_depth<=? AND to_depth>=? LIMIT 1",
                        new String[]{String.valueOf(boreholeId), String.valueOf(locationId), String.valueOf(depth), String.valueOf(depth)});

                if (!(vc != null && vc.moveToFirst())) {
                    out.append("No visual classification record covers this depth.\n");
                    tv.setText(out.toString());
                    if (spt != null) spt.close();
                    if (vc != null) vc.close();
                    return;
                }

                String colorCode = vc.getString(vc.getColumnIndexOrThrow("color_code"));
                double sandPct = vc.getDouble(vc.getColumnIndexOrThrow("sand_percentage"));
                double siltPct = vc.getDouble(vc.getColumnIndexOrThrow("silt_percentage"));
                double clayPct = vc.getDouble(vc.getColumnIndexOrThrow("clay_percentage"));
                double fromDepth = vc.getDouble(vc.getColumnIndexOrThrow("from_depth"));
                double toDepth = vc.getDouble(vc.getColumnIndexOrThrow("to_depth"));

                out.append("Step 1: Color mapping\n");
                out.append(String.format("Code: %s -> %s\n\n", colorCode, mapColorCode(colorCode)));

                out.append("Step 2: Choose SPT table (compare Sand% and Clay%)\n");
                out.append(String.format("Sand=%.2f, Clay=%.2f -> %s\n\n", sandPct, clayPct, sandPct > clayPct ? "Use Table-2" : "Use Table-3"));

                out.append(String.format("Step 3: Gather TOTAL SPT values for depths %.2f to %.2f\n", fromDepth, toDepth));

                // collect totals
                java.util.List<Double> totals = new java.util.ArrayList<>();
                android.database.Cursor rows = db.fetchSptDataByBorehole(boreholeId, locationId);
                if (rows != null) {
                    while (rows.moveToNext()) {
                        double d = rows.getDouble(rows.getColumnIndexOrThrow("depth"));
                        if (d >= fromDepth && d <= toDepth) {
                            int n2 = rows.getInt(rows.getColumnIndexOrThrow("n2"));
                            int n3 = rows.getInt(rows.getColumnIndexOrThrow("n3"));
                            totals.add((double)(n2 + n3));
                            out.append(String.format("  depth %.2f -> total SPT = %d\n", d, n2 + n3));
                        }
                    }
                    rows.close();
                }

                if (totals.isEmpty()) {
                    out.append("No SPT rows found in the range.\n");
                    tv.setText(out.toString());
                    spt.close(); vc.close();
                    return;
                }

                double min = totals.stream().min(java.util.Comparator.naturalOrder()).orElse(0.0);
                double max = totals.stream().max(java.util.Comparator.naturalOrder()).orElse(0.0);
                out.append(String.format("\nMin total = %.0f, Max total = %.0f\n\n", min, max));

                out.append("Step 3 (continued): Map min/max to condition(s)\n");
                boolean useTable2 = sandPct > clayPct;
                String condLow = mapSptToCondition(min, useTable2);
                String condHigh = mapSptToCondition(max, useTable2);
                if (condLow.equals(condHigh)) out.append(String.format("Condition: %s\n\n", condLow));
                else out.append(String.format("Conditions: %s / %s\n\n", condLow, condHigh));

                out.append("Step 4: Map percentages to terms (Table-1)\n");
                java.util.Map<String, Double> perc = new java.util.HashMap<>();
                perc.put("sand", sandPct);
                perc.put("silt", siltPct);
                perc.put("clay", clayPct);
                for (java.util.Map.Entry<String, Double> e : perc.entrySet()) {
                    if (e.getValue() <= 0) continue;
                    out.append(String.format("  %s = %.2f -> %s\n", e.getKey(), e.getValue(), mapPercentageToTerm(e.getValue())));
                }

                out.append("\nStep 5: Combine description\n");
                java.util.List<java.util.Map.Entry<String, Double>> comps = new java.util.ArrayList<>(perc.entrySet());
                comps.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));
                StringBuilder compSb = new StringBuilder();
                for (int i=0;i<comps.size();i++){
                    String name = comps.get(i).getKey();
                    double pct = comps.get(i).getValue();
                    if (pct <= 0) continue;
                    if (i==0) compSb.append(name.toUpperCase());
                    else {
                        String term = mapPercentageToTerm(pct);
                        String displayName = name;
                        double sandP = perc.getOrDefault("sand", 0.0);
                        double siltP = perc.getOrDefault("silt", 0.0);
                        double clayP = perc.getOrDefault("clay", 0.0);
                        if (name.equals("sand") && sandP <= siltP && sandP <= clayP) displayName = "fine sand";
                        compSb.append(String.format(", %s %s", term.toLowerCase(), displayName));
                    }
                }

                String finalDesc = String.format("%s %s %s", mapColorCode(colorCode),
                        condLow.equals(condHigh) ? condLow.toLowerCase() : (condLow + " / " + condHigh).toLowerCase(),
                        compSb.toString());

                out.append(String.format("Final Description:\n%s\n", finalDesc.trim()));

                tv.setText(out.toString());

                spt.close(); vc.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                tv.setText("Failed to generate analysis: " + ex.getMessage());
            }

            return;
        }

        // fallback: simple counts
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

    private String mapPercentageToTerm(double p) {
        if (p < 5) return "Trace";
        if (p < 10) return "Few";
        if (p < 25) return "Little";
        if (p < 45) return "Some";
        return "Mostly";
    }

    private String mapSptToCondition(double val, boolean useTable2) {
        if (useTable2) {
            if (val <= 4) return "Very Loose";
            if (val <= 10) return "Loose";
            if (val <= 25) return "Medium Dense";
            if (val <= 50) return "Dense";
            return "Very Dense";
        } else {
            if (val <= 2) return "Very Soft";
            if (val <= 4) return "Soft";
            if (val <= 8) return "Medium Stiff";
            if (val <= 16) return "Stiff";
            if (val <= 30) return "Very Stiff";
            return "Hard";
        }
    }

    private String mapColorCode(String code) {
        if (code == null) return "";
        switch (code.trim().toUpperCase()) {
            case "RB": return "Redish Brown";
            case "BL": return "Black";
            case "BR": return "Brown";
            default: return code;
        }
    }
}
