package com.example.managerclassroom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.managerclassroom.R;
import com.example.managerclassroom.models.NotifiClass;
import com.example.managerclassroom.models.OnItemClickListener;


import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NotifiClass> notifiClassArrayList;
    private final OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<NotifiClass> notifiClassArrayList, OnItemClickListener listener) {
        this.context = context;
        this.notifiClassArrayList = notifiClassArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        holder.txtNotification.setText(notifiClassArrayList.get(position).getDescription());
        holder.txtTimeNoti.setText(notifiClassArrayList.get(position).getTime_noti());
        int status = notifiClassArrayList.get(position).getStatus();
        if(status==0){ // cancel register room
            holder.imgNotiItem.setImageResource(R.drawable.delete);
        }else if(status==1){ // success register room
            holder.imgNotiItem.setImageResource(R.drawable.success);
        }else if(status==2){ // damaged room
            holder.imgNotiItem.setImageResource(R.drawable.warning);
        }
    }

    @Override
    public int getItemCount() {
        return notifiClassArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNotification;
        TextView txtTimeNoti;
        ImageView imgNotiItem;
        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            txtNotification = itemView.findViewById(R.id.txtNotiItem);
            txtTimeNoti = itemView.findViewById(R.id.txtTimeNoti);
            imgNotiItem = itemView.findViewById(R.id.imgNotiItem);
        }
    }
}
