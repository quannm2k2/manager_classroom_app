package com.example.managerclassroom.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.managerclassroom.R;
import com.example.managerclassroom.activities.ClassroomActivity;
import com.example.managerclassroom.activities.RoomInfoActivity;
import com.example.managerclassroom.adapter.RoomAdapter;
import com.example.managerclassroom.databinding.FragmentHomeBinding;
import com.example.managerclassroom.databinding.TimeStudyDialogBinding;
import com.example.managerclassroom.methods.GetTimes;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.methods.NotificationsHelper;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.DamagedRoom;
import com.example.managerclassroom.models.DatabaseUpdateListener;
import com.example.managerclassroom.models.NotifiClass;
import com.example.managerclassroom.models.OnItemClickListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnItemClickListener {
    private ArrayList<Classroom> classroomArrayList;
    private RoomAdapter roomAdapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String id, date_study;
    private FragmentHomeBinding homeBinding;
    private TimeStudyDialogBinding timeStudyDialogBinding;
    private AlertDialog loadingDialog;
    DatabaseReference referenceUser, referenceRoom;
    private boolean checkResume;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkResume = false;
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);

        setupUI();
        setupListeners();

        // Get user ID from arguments
        if (getArguments() != null) {
            id = getArguments().getString("id");
        }

        // Initialize RecyclerView and fetch data
        setupRecyclerView();
        fetchData();
        damagedRoomExpired();

        return homeBinding.getRoot();
    }

    private void setupUI() {
        // Show greeting based on time of day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        homeBinding.txtGreeting.setText(getGreeting(hour));
    }

    // Handle greeting
    private String getGreeting(int hour) {
        if (hour >= 5 && hour <= 12) return getString(R.string.good_morning);
        if (hour >= 13 && hour <= 18) return getString(R.string.good_afternoon);
        if (hour >= 19 && hour <= 23) return getString(R.string.good_evening);
        return "";
    }

    private void setupListeners() {
        homeBinding.cvSelectTime.setOnClickListener(v -> openDatePicker()); // Handle when select time study
        homeBinding.swipeRefreshLayout.setOnRefreshListener(this::fetchData); // Handle when swipe refresh
    }

    // Khởi tạo Calendar
    private void openDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.MyDatePickerTheme) // Set theme
                .setTitleText(getString(R.string.select_day)) // Tiêu đề hộp thoại
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Default là ngày hiện tại
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.now()).build()) // Chỉ cho phép chọn ngày hiện tại đổ đi
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            date_study = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date(selection));
            homeBinding.txtTimeHome.setText(date_study);
            showTimeDialog(); // Schedule time to study
        });

        datePicker.show(requireActivity().getSupportFragmentManager(), "tag");
    }

    private void setupRecyclerView() {
        classroomArrayList = new ArrayList<>();
        roomAdapter = new RoomAdapter(classroomArrayList, getContext(), this);
        homeBinding.recycleViewHome.setLayoutManager(new GridLayoutManager(getContext(), 1));
        homeBinding.recycleViewHome.setAdapter(roomAdapter);
    }

    private void fetchData() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            homeBinding.swipeRefreshLayout.setRefreshing(false);
            return;
        }
        showLoadingDialog();

        DatabaseReference referenceRoom = database.getReference().child("users").child(id).child("data");
        referenceRoom.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classroomArrayList.clear();
                if (snapshot.exists()) {
                    homeBinding.txtCheckRoom.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Classroom classroom = dataSnapshot.getValue(Classroom.class);
                        // Kiểm tra classroom đó có trống và có bị hư hỏng hay hết hạn không?
                        if (classroom != null && (!isTimeExpired(classroom) || classroom.getCodeStatusReport().equals(getString(R.string.code_not_handle)))) {
                            classroomArrayList.add(classroom);
                        }
                    }

                    // Sắp xếp danh sách theo `date_study` và `time_study`
                    classroomArrayList.sort((c1, c2) -> {
                        // So sánh `date_study` trước
                        int dateComparison = c1.getDate_study().compareTo(c2.getDate_study());
                        if (dateComparison == 0) {
                            // Nếu `date_study` giống nhau, so sánh `time_study`
                            return getTimeStudyOrder(c1.getTime_study()) - getTimeStudyOrder(c2.getTime_study());
                        }
                        return dateComparison;
                    });

                    // Update dữ liệu vào RoomAdapter
                    roomAdapter.notifyDataSetChanged();
                } else {
                    homeBinding.txtCheckRoom.setVisibility(View.VISIBLE);
                }
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissLoadingDialog();
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void damagedRoomExpired(){
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference referenceDamaged = database.getReference().child("Damaged rooms");
        referenceDamaged.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        DamagedRoom room = dataSnapshot.getValue(DamagedRoom.class);
                        assert room != null;
                        String codeStatusReport = room.getCodeStatusReport();
                        if(codeStatusReport.equals(getString(R.string.code_handled)) && isTimeExpiredDamaged(room)){
                            database.getReference().child("Damaged rooms").child(room.getRoom()).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chuyển đổi `time_study` thành các giá trị có thể so sánh
    private int getTimeStudyOrder(String timeStudy) {
        switch (timeStudy) {
            case "morning":
                return 0;
            case "afternoon":
                return 1;
            case "evening":
                return 2;
            default:
                return 3;
        }
    }

    // Xem lại phần này
    private boolean isTimeExpired(Classroom classroom) {
        int isTimeValid = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isTimeValid = GetTimes.isTimeValid(classroom.getDate_study());
        }
        if (isTimeValid > 0) {
            // Xóa Classroom đã hết hạn
            database.getReference().child("Classrooms").child(classroom.getDate_study()).removeValue();
            return true;
        }
        return false;
    }

    private boolean isTimeExpiredDamaged(DamagedRoom room) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        int isTimeValid = 0;

        try {
            Date date = inputFormat.parse(room.getTime_report());
            assert date != null;
            String formattedDate = outputFormat.format(date);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isTimeValid = GetTimes.isTimeValid(formattedDate);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return isTimeValid > 0;
    }

    // send data and navigation to Classroom
    private void sendData(String time) {
        Intent intent = new Intent(getActivity(), ClassroomActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("time_study", time);
        intent.putExtra("date_study", date_study);
        startActivity(intent);
    }

    // Dialog time study
    private void showTimeDialog() {
        timeStudyDialogBinding = TimeStudyDialogBinding.inflate(getLayoutInflater());

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(timeStudyDialogBinding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CardView cvMorning = timeStudyDialogBinding.cvMorning;
        CardView cvAfternoon = timeStudyDialogBinding.cvAfternoon;
        CardView cvEvening = timeStudyDialogBinding.cvEvening;

        updateCardVisibility(cvMorning, cvAfternoon, cvEvening);

        // Thiết lập hành động khi chọn buổi học
        setCardClickListeners(cvMorning, "morning", dialog);
        setCardClickListeners(cvAfternoon, "afternoon", dialog);
        setCardClickListeners(cvEvening, "evening", dialog);

        dialog.show();
    }

    // Cập nhật trạng thái hiển thị của các CardView
    private void updateCardVisibility(CardView cvMorning, CardView cvAfternoon, CardView cvEvening) {
        // Lấy ngày hiện tại
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedCurrentDate = sdfDate.format(new Date());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && formattedCurrentDate.equals(date_study)) {
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isBefore(LocalTime.of(6, 50))) {
                setCardState(cvMorning, false);
                setCardState(cvAfternoon, false);
                setCardState(cvEvening, false);
            } else if (currentTime.isBefore(LocalTime.of(11, 50))) {
                setCardState(cvMorning, true);
                setCardState(cvAfternoon, false);
                setCardState(cvEvening, false);
            } else if (currentTime.isBefore(LocalTime.of(16, 50))) {
                setCardState(cvMorning, true);
                setCardState(cvAfternoon, true);
                setCardState(cvEvening, false);
            } else {
                setCardState(cvMorning, true);
                setCardState(cvAfternoon, true);
                setCardState(cvEvening, true);
            }
        } else {
            setCardState(cvMorning, false);
            setCardState(cvAfternoon, false);
            setCardState(cvEvening, false);
        }
    }

    // Thiết lập trạng thái cho các CardView
    private void setCardState(CardView cardView, boolean isDisabled) {
        int grayColor = ContextCompat.getColor(timeStudyDialogBinding.getRoot().getContext(), R.color.gray_light);
        if (isDisabled) {
            cardView.setCardBackgroundColor(grayColor);
            cardView.setAlpha(0.7f); // Thiết lập alpha để tạo hiệu ứng mờ
        } else {
            cardView.setCardBackgroundColor(Color.WHITE);
            cardView.setAlpha(1.0f);
        }
    }

    // Thiết lập hành động khi người dùng nhấn vào CardView
    private void setCardClickListeners(CardView cardView, String time, Dialog dialog) {
        if (cardView.getAlpha() < 1.0f) {
            // CardView đang bị disable
            cardView.setOnClickListener(v -> Toast.makeText(v.getContext(), R.string.out_time, Toast.LENGTH_SHORT).show());
        } else {
            // CardView đang được enable
            cardView.setOnClickListener(v -> selectTime(time, dialog));
        }
    }

    // Xử lý khi người dùng chọn buổi học và đóng dialog
    private void selectTime(String period, Dialog dialog) {
        sendData(period);
        dialog.dismiss();
    }

    // Xóa lớp học từ Firebase
    private void removeRoom(int position, DatabaseUpdateListener listener) {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        Classroom classroom = classroomArrayList.get(position);
        referenceUser = database.getReference().child("users").child(id);
        referenceRoom = database.getReference()
                                .child("Classrooms")
                                .child(classroom.getDate_study())
                                .child(classroom.getTime_study())
                                .child(classroom.getFloorName())
                                .child(classroom.getRoomName());

        // Check Damaged Room
        DatabaseReference damagedRoomRef = database.getReference().child("Damaged rooms").child(classroom.getRoomName());
        damagedRoomRef.child("codeStatusReport").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String codeStatus = snapshot.exists() ? snapshot.getValue(String.class) : null;

                if (Objects.equals(codeStatus, getString(R.string.code_not_handle))) {
                    // Thông báo lỗi nếu phòng đã bị hư hỏng
                    Toast.makeText(requireContext(), getString(R.string.cant_cancel), Toast.LENGTH_SHORT).show();
                } else {
                    showLoadingDialog();
                    // Xóa phòng học và xử lý kết quả
                    referenceRoom.removeValue().addOnSuccessListener(unused -> removeUserData(referenceUser, classroom, listener))
                            .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Something was wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeUserData(DatabaseReference referenceUser, Classroom classroom, DatabaseUpdateListener listener) {
        referenceUser.child("data").child(classroom.getTime_signup()).removeValue()
                .addOnSuccessListener(unused -> {
                    showSuccessNotification(classroom);
                    listener.onUpdateSuccess();
                })
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showSuccessNotification(Classroom classroom) {
        Toast.makeText(requireContext(), getString(R.string.cancel_success), Toast.LENGTH_SHORT).show();
        NotificationsHelper.createNotifications(requireContext(), "CANCEL_REGISTER",
                getString(R.string.noti), getString(R.string.cancel_success));

        String desc_noti = getString(R.string.noti_cancel) + " " + classroom.getRoomName();
        String time_noti = GetTimes.getTimeUpdate(requireContext());
        NotifiClass notifiClass = new NotifiClass(desc_noti, time_noti, 0);
        referenceUser.child("notifications").child(time_noti).setValue(notifiClass);

        // refresh Classroom List
        classroomArrayList.clear();
        roomAdapter.notifyDataSetChanged();
        fetchData();
    }

    // Unregister room
    private void unregisterRoom(int position) {
        // Tạo Dialog thông báo hủy đăng ký
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cancel_dialog);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnDialog = dialog.findViewById(R.id.btnDialog);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Confirm cancel
        btnDialog.setOnClickListener(v -> {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
            if (!isInternetConnected) {
                Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();

            // remove classroom from database
            removeRoom(position, new DatabaseUpdateListener() {
                @Override
                public void onUpdateSuccess() {
                    dismissLoadingDialog();
                }

                @Override
                public void onUpdateFailure(String errorMessage) {
                    dismissLoadingDialog();
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Click to item recycle view
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), RoomInfoActivity.class);
        // Lưu dữ liệu dưới dạng Key-Value
        intent.putExtra("room", classroomArrayList.get(position).getRoomName());
        intent.putExtra("date_study", classroomArrayList.get(position).getDate_study());
        intent.putExtra("teacher", classroomArrayList.get(position).getTeacher());
        intent.putExtra("subject", classroomArrayList.get(position).getSubject());
        intent.putExtra("time_study", classroomArrayList.get(position).getTime_study());
        intent.putExtra("time_signup", classroomArrayList.get(position).getTime_signup());
        intent.putExtra("floor", classroomArrayList.get(position).getFloorName());
        intent.putExtra("id", id);
        startActivity(intent);
    }

    // Long click to item recycle view
    @Override
    public void onItemLongClick(int position) {
        unregisterRoom(position);
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder loadingAlertDialog = new AlertDialog.Builder(requireContext());
            loadingAlertDialog.setView(R.layout.loading_layout);
            loadingAlertDialog.setCancelable(false); // Ngăn người dùng đóng dialog bằng cách nhấn bên ngoài
            loadingDialog = loadingAlertDialog.create();
        }
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        homeBinding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if(!isInternetConnected && checkResume){
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            setupRecyclerView();
        }
        checkResume = true;
    }
}