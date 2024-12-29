package com.example.managerclassroom.view_holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managerclassroom.R;
import com.example.managerclassroom.models.OnItemClickListener;

// Chứa các thành phần UI
public class RoomViewHolder extends RecyclerView.ViewHolder {
    public TextView txtRoomInfo, txtTimeInfo, txtDateInfo, txtSubjectInfo;
    CardView cardView;

    public RoomViewHolder(@NonNull View itemView, OnItemClickListener listener) {
        super(itemView);
        txtRoomInfo = itemView.findViewById(R.id.txtRoomInfo);
        txtTimeInfo = itemView.findViewById(R.id.txtTimeInfo);
        txtSubjectInfo = itemView.findViewById(R.id.txtSubjectInfo);
        txtDateInfo = itemView.findViewById(R.id.txtDateStudyInfo);
        cardView = itemView.findViewById(R.id.cvRecycleView);

        // Click
        itemView.setOnClickListener(v -> {
            if(listener!=null){
                int position = getBindingAdapterPosition();
                if(position!= RecyclerView.NO_POSITION){
                    listener.onItemClick(position);
                }
            }
        });
        // Long click
        itemView.setOnLongClickListener(v -> {
            if(listener!=null){
                int position = getBindingAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    listener.onItemLongClick(position);
                    return true;
                }
            }
            return false;
        });
    }
}
