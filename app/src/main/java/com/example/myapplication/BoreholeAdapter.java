package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BoreholeAdapter extends RecyclerView.Adapter<BoreholeAdapter.VH> {
    private final List<Integer> items = new ArrayList<>();
    private final OnSelect listener;
    public interface OnSelect { void onBoreholeSelected(int num); }
    public BoreholeAdapter(OnSelect l){ this.listener = l; }
    public void setCount(int count){ items.clear(); for(int i=1;i<=count;i++) items.add(i); notifyDataSetChanged(); }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){ View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker,parent,false); return new VH(v); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){ int num = items.get(pos); h.btn.setText("Borehole " + num); h.btn.setOnClickListener(v->listener.onBoreholeSelected(num));
        int[] STICKER_PALETTE = new int[] { 0xFF6EC6FF, 0xFFFF8A65, 0xFFAED581, 0xFFF48FB1, 0xFFFFF176, 0xFFB39DDB };
        int color = STICKER_PALETTE[pos % STICKER_PALETTE.length];
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(color);
        float radius = h.itemView.getResources().getDisplayMetrics().density * 12f;
        gd.setCornerRadius(radius);
        int stroke = (int)(h.itemView.getResources().getDisplayMetrics().density * 0.5f);
        gd.setStroke(stroke, 0x22000000);
        h.btn.setBackground(gd);
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        h.btn.setTextColor(luminance > 0.6 ? 0xFF222222 : android.graphics.Color.WHITE);
        int pad = (int)(h.itemView.getResources().getDisplayMetrics().density * 12f);
        h.btn.setPadding(pad, pad, pad, pad);
        h.btn.setAllCaps(false);
        h.btn.setElevation(h.itemView.getResources().getDisplayMetrics().density * 4f);
    }
    @Override public int getItemCount(){ return items.size(); }
    static class VH extends RecyclerView.ViewHolder{ Button btn; VH(@NonNull View v){ super(v); btn = v.findViewById(R.id.btnSticker); } }
}
