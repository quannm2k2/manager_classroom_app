package com.example.managerclassroom.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.adapter.NotificationAdapter;
import com.example.managerclassroom.databinding.FragmentNotificationBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.models.DatabaseUpdateListener;
import com.example.managerclassroom.models.NotifiClass;
import com.example.managerclassroom.models.OnItemClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class NotificationFragment extends Fragment implements OnItemClickListener {

    private RecyclerView rvNotification;
    private TextView txtClearAll, txtNoNoti;
    private ArrayList<NotifiClass> notifiClassArrayList;
    private NotificationAdapter adapter;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String id;
    private NotifiClass deleteItem;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentNotificationBinding binding = FragmentNotificationBinding.inflate(inflater, container, false);

        rvNotification = binding.rvNotification;
        txtClearAll = binding.txtClearAll;
        txtNoNoti = binding.txtNoNoti;

        // set up recycle view
        setUpRecycleView();

        if (getArguments() != null) {
            id = getArguments().getString("id");
        }

        // database process
        databaseProcess();

        // Clear all notifications
        txtClearAll.setOnClickListener(v -> {
            clearAllNoti();
        });

        // Swipe left to delete item notification
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvNotification);

        return binding.getRoot();
    }

    private void setUpRecycleView(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvNotification.setLayoutManager(gridLayoutManager);
        notifiClassArrayList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notifiClassArrayList, this);
        rvNotification.setAdapter(adapter);
    }

    // database process
    private void databaseProcess() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if(!isInternetConnected){
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.loading_layout);
        AlertDialog dialog = builder.create();

        DatabaseReference reference = database.getReference().child("users").child(id).child("notifications");
        dialog.show();
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    txtNoNoti.setVisibility(View.GONE);
                    notifiClassArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        NotifiClass notifiClass = dataSnapshot.getValue(NotifiClass.class);
                        notifiClassArrayList.add(0,notifiClass);
                    }
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    txtNoNoti.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });
    }

    // Remove a notification from database
    private void deleteOneNoti(NotifiClass notifiClass, DatabaseUpdateListener listener){
        DatabaseReference reference = database.getReference().child("users").child(id).child("notifications").child(notifiClass.getTime_noti());
        reference.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), getString(R.string.deleted_noti), Toast.LENGTH_SHORT).show();
            listener.onUpdateSuccess();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), getText(R.string.error), Toast.LENGTH_SHORT).show();
            listener.onUpdateFailure(e.getMessage());
        });
    }

    // Restore a specific notification to the database
    private void restoreNoti(NotifiClass notifiClass, DatabaseUpdateListener listener) {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if(!isInternetConnected){
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = database.getReference().child("users").child(id).child("notifications").child(notifiClass.getTime_noti());
        reference.setValue(notifiClass).addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
            listener.onUpdateSuccess();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), getText(R.string.error), Toast.LENGTH_SHORT).show();
            listener.onUpdateFailure(e.getMessage());
        });
    }

    // Xóa item notification khi kéo item sang trái
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
            if(!isInternetConnected){
                Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            int position = viewHolder.getAdapterPosition();
            deleteItem = notifiClassArrayList.get(position);
            notifiClassArrayList.remove(position);
            adapter.notifyItemRemoved(position);

            // Remove a notification from firebase
            deleteOneNoti(deleteItem, new DatabaseUpdateListener() {
                @Override
                public void onUpdateSuccess() {
                }

                @Override
                public void onUpdateFailure(String errorMessage) {
                    // Restore a specific notification if error
                    restoreNoti(deleteItem, new DatabaseUpdateListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onUpdateSuccess() {
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            Snackbar.make(rvNotification, R.string.deleted_noti, Snackbar.LENGTH_SHORT).setAction("Hoàn tác", v -> {
                // Restore a specific notification to firebase
                restoreNoti(deleteItem, new DatabaseUpdateListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onUpdateSuccess() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onUpdateFailure(String errorMessage) {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }).show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.damaged))
                    .addSwipeLeftActionIcon(R.drawable.bin)
                    .addSwipeLeftLabel(getString(R.string.delete))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    // Remove all notification from database
    @SuppressLint("NotifyDataSetChanged")
    private void deleteAllNoti(DatabaseUpdateListener listener){
        DatabaseReference reference = database.getReference().child("users").child(id).child("notifications");
        reference.removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), getString(R.string.clear_all_success), Toast.LENGTH_SHORT).show();
            notifiClassArrayList.clear();
            adapter.notifyDataSetChanged();
            txtNoNoti.setVisibility(View.VISIBLE);

            listener.onUpdateSuccess();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), getText(R.string.error), Toast.LENGTH_SHORT).show();
            listener.onUpdateFailure(e.getMessage());
        });
    }

    // Clear all notification
    private void clearAllNoti() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if(!isInternetConnected){
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_noti_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnDialog = dialog.findViewById(R.id.btnDialogNoti);
        Button btnDone = dialog.findViewById(R.id.btnDoneNoti);

        btnDialog.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setCancelable(false);
            builder.setView(R.layout.loading_layout);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            deleteAllNoti(new DatabaseUpdateListener() {
                @Override
                public void onUpdateSuccess() {
                    alertDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onUpdateFailure(String errorMessage) {
                    alertDialog.dismiss();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Skip
        btnDone.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onItemClick(int position) {}

    @Override
    public void onItemLongClick(int position) {}
}