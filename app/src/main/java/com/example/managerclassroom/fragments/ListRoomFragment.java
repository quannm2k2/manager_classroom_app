package com.example.managerclassroom.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.adapter.RoomAdapter;
import com.example.managerclassroom.databinding.FragmentListRoomBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.OnItemClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ListRoomFragment extends Fragment implements OnItemClickListener {
    private ArrayList<Classroom> classroomArrayList;
    private RoomAdapter roomAdapter;
    RecyclerView rvListRoom;
    TextView txtCheckRoomList;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String id;
    private Dialog dialog;
    private boolean checkResume;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkResume = false;
        FragmentListRoomBinding fragmentListRoomBinding = FragmentListRoomBinding.inflate(inflater, container, false);

        rvListRoom = fragmentListRoomBinding.recycleViewList;
        txtCheckRoomList = fragmentListRoomBinding.txtCheckRoomList;

        // Set up recycle view
        setupRecycleView();

        // get data from MainActivity
        if(getArguments()!=null) {
            id = getArguments().getString("id");
        }

        // Database process
        fetchDataFromFirebase();

        return fragmentListRoomBinding.getRoot();
    }

    private void setupRecycleView(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvListRoom.setLayoutManager(gridLayoutManager);
        classroomArrayList = new ArrayList<>();
        roomAdapter = new RoomAdapter(classroomArrayList, getContext(), this);
        rvListRoom.setAdapter(roomAdapter);
    }

    private void fetchDataFromFirebase() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();
        DatabaseReference reference = database.getReference("users").child(id).child("data");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateClassroomList(snapshot);
                if(classroomArrayList.isEmpty()){
                    txtCheckRoomList.setVisibility(View.VISIBLE);
                } else {
                    txtCheckRoomList.setVisibility(View.GONE);
                }
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissLoadingDialog();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateClassroomList(DataSnapshot snapshot) {
        if (snapshot.exists()) {
            classroomArrayList.clear();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Classroom classroom = dataSnapshot.getValue(Classroom.class);
                if (classroom != null && isOngoingClassroom(classroom)) {
                    classroomArrayList.add(classroom);
                }
            }
            roomAdapter.notifyDataSetChanged();
        }
    }

    private boolean isOngoingClassroom(Classroom classroom) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date currentDate = new Date();

            String currentDateStr = dateFormatter.format(currentDate);
            String currentTimeStr = timeFormatter.format(currentDate);

            if (!currentDateStr.equals(classroom.getDate_study())) {
                return false;
            }

            // Xem lại ngày giờ học
            switch (classroom.getTime_study()) {
                case "morning":
                    return currentTimeStr.compareTo("06:50") >= 0 && currentTimeStr.compareTo("12:10") <= 0;
                case "afternoon":
                    return currentTimeStr.compareTo("11:50") >= 0 && currentTimeStr.compareTo("17:10") <= 0;
                case "evening":
                    return currentTimeStr.compareTo("16:50") >= 0 && currentTimeStr.compareTo("22:10") <= 0;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.loading_layout);
        dialog = builder.create();
        dialog.show();
    }

    private void dismissLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onItemClick(int position) {
        Bundle bundle = getBundle(position);
        ControlFragment controlFragment = new ControlFragment();
        controlFragment.setArguments(bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, controlFragment); // Thay thế Fragment hiện tại bằng controlFragment
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private @NonNull Bundle getBundle(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("time_signup",classroomArrayList.get(position).getTime_signup());
        bundle.putString("date_study",classroomArrayList.get(position).getDate_study());
        bundle.putString("time_study",classroomArrayList.get(position).getTime_study());
        bundle.putString("floor",classroomArrayList.get(position).getFloorName());
        bundle.putString("room",classroomArrayList.get(position).getRoomName());
        return bundle;
    }

    @Override
    public void onItemLongClick(int position) {
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected && checkResume) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            setupRecycleView();
        }
        checkResume = true;
    }
}