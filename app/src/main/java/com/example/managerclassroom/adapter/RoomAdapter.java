package com.example.managerclassroom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managerclassroom.R;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.OnItemClickListener;
import com.example.managerclassroom.view_holders.RoomViewHolder;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomViewHolder> {
    private final ArrayList<Classroom> classroomArrayList;
    private final Context context;
    private final OnItemClickListener listener;

    public RoomAdapter(ArrayList<Classroom> classroomArrayList, Context context, OnItemClickListener listener) {
        this.classroomArrayList = classroomArrayList;
        this.context = context;
        this.listener = listener;
    }

    // Tạo ViewHolder mới khi không còn View nào có sẵn để tái sử dụng (InfoClassroom)
    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.info_classroom, parent, false);
        return new RoomViewHolder(view, listener);
    }

    // Gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.txtRoomInfo.setText(classroomArrayList.get(position).getRoomName());
        holder.txtDateInfo.setText(classroomArrayList.get(position).getDate_study());
        holder.txtSubjectInfo.setText(classroomArrayList.get(position).getSubject());
        holder.txtTimeInfo.setText(classroomArrayList.get(position).getTime_study());
        // position đại diện cho index của danh sách phần tử
        String time = classroomArrayList.get(position).getTime_study();
        switch (time){
            case "morning":
                holder.txtTimeInfo.setText(context.getText(R.string.morning_time));
                break;
            case "afternoon":
                holder.txtTimeInfo.setText(context.getText(R.string.afternoon_time));
                break;
            case "evening":
                holder.txtTimeInfo.setText(context.getText(R.string.evening_time));
                break;
        }
    }

    // Số lượng phần tử trong danh sách
    @Override
    public int getItemCount() {
        return classroomArrayList.size();
    }
}
