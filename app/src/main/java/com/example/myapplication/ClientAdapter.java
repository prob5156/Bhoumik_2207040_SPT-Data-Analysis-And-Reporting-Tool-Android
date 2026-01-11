package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.VH> {
    public static class Client {
        public int id; public String name; public String phone; public String pwd;
        public Client(int id, String name, String phone, String pwd){this.id=id;this.name=name;this.phone=phone;this.pwd=pwd;}
    }

    private final List<Client> items = new ArrayList<>();
    private final Context ctx;
    private final String role;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onClientSelected(Client client);
        void onEdit(Client client);
        void onDelete(Client client);
    }

    public ClientAdapter(Context ctx, String role, OnActionListener l){ this.ctx=ctx; this.role=role; this.listener=l; }

    public void setFromCursor(Cursor c){ items.clear(); if (c==null) return; while(c.moveToNext()){ int id=c.getInt(c.getColumnIndexOrThrow("id")); String name=c.getString(c.getColumnIndexOrThrow("name")); String phone=c.getString(c.getColumnIndexOrThrow("phone")); String pwd=null; try{pwd=c.getString(c.getColumnIndexOrThrow("password"));}catch(Exception ignored){} items.add(new Client(id,name,phone,pwd)); } notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType){ View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker,parent,false); return new VH(v); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){ Client it = items.get(pos); h.btn.setText(it.name); h.btn.setOnClickListener(v -> listener.onClientSelected(it));
        if ("SENIOR".equalsIgnoreCase(role)) { h.actionRow.setVisibility(View.VISIBLE); h.btnEdit.setOnClickListener(v->listener.onEdit(it)); h.btnDelete.setOnClickListener(v->listener.onDelete(it)); }
        else h.actionRow.setVisibility(View.GONE);
        // apply sticker styling per item
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

    static class VH extends RecyclerView.ViewHolder{
        Button btn; LinearLayout actionRow; Button btnEdit, btnDelete;
        VH(@NonNull View v){ super(v); btn = v.findViewById(R.id.btnSticker); actionRow = v.findViewById(R.id.actionRow); btnEdit = v.findViewById(R.id.btnEdit); btnDelete = v.findViewById(R.id.btnDelete); }
    }
}
