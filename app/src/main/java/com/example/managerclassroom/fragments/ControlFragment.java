package com.example.managerclassroom.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.FragmentControlBinding;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.DamagedRoom;
import com.example.managerclassroom.models.DeviceClassroom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ControlFragment extends Fragment {

    private FragmentControlBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referenceRoom, referenceDevicesRoom, referenceDevices;
    private int status = 1;
    private String id, time_study, floor, room, date_study, time_signup;
    private Boolean status_light, status_speaker, status_fan, status_projector;
    private AlertDialog loadingDialog;
    private boolean isUpdatingUI = false; // Cờ kiểm soát để tránh vòng lặp
    private ValueEventListener devicesEventListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentControlBinding.inflate(inflater, container, false);

        initializeArguments(); // Lấy dữ liệu từ ListRoomFragment
        updateRoomDetails(); // Cập nhật thông tin phòng học
        checkDamagedRoom(); // Kiểm tra phòng có thiết bị hỏng không
        checkStatusAllDevice(); // // check status device

        // card view light click
        binding.imgLight.setOnClickListener(v -> {
            controlDevice("light", status_light = !status_light);
        });

        // card view speaker click
        binding.imgSpeaker.setOnClickListener(v -> {
            controlDevice("speaker", status_speaker = !status_speaker);
        });

        // card view fan click
        binding.imgFan.setOnClickListener(v -> {
            controlDevice("fan", status_fan = !status_fan);
        });

        // card view projector click
        binding.imgProjector.setOnClickListener(v -> {
            controlDevice("projector", status_projector = !status_projector);
        });

        // turn on/ off all device
        binding.swAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Kiểm tra chỉ khi người dùng nhấn trực tiếp vào switch
                turnOnOffAllDevices(isChecked);
            }
        });

        // Button back
        binding.btnBackControl.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        return binding.getRoot();
    }

    private void checkDamagedRoom() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference referenceDamaged = database.getReference().child("Damaged rooms");
        referenceDamaged.addListenerForSingleValueEvent(new  ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        DamagedRoom damagedRoom = dataSnapshot.getValue(DamagedRoom.class);
                        assert damagedRoom != null;
                        String roomName = damagedRoom.getRoom();
                        String problem = damagedRoom.getReport();
                        String codeStatusReport = damagedRoom.getCodeStatusReport();
                        if(room.equals(roomName)){
                            if(codeStatusReport.equals(getString(R.string.code_not_handle))){
                                updateClassroom(2,problem, codeStatusReport);
                                binding.swAll.setEnabled(false);
                            } else {
                                updateClassroom(1,problem, codeStatusReport);
                                binding.swAll.setEnabled(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(binding.getRoot().getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // create classroom
    private void updateClassroom(int statusDB, String report, String codeStatusReport) {
        referenceRoom = database.getReference().child("users").child(id).child("data").child(time_signup);
        referenceRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Classroom classroom = snapshot.getValue(Classroom.class);
                if(classroom != null){
                    referenceRoom.child("status").setValue(statusDB);
                    referenceRoom.child("report").setValue(report);
                    referenceRoom.child("codeStatusReport").setValue(codeStatusReport);

                    status = statusDB;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeArguments() {
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString("id");
            time_study = args.getString("time_study");
            date_study = args.getString("date_study");
            floor = args.getString("floor");
            room = args.getString("room");
            time_signup = args.getString("time_signup");
        }
    }

    private void updateRoomDetails() {
        String studyTimeText = "";
        switch (time_study) {
            case "morning": studyTimeText = getString(R.string.morning_time); break;
            case "afternoon": studyTimeText = getString(R.string.afternoon_time); break;
            case "evening": studyTimeText = getString(R.string.evening_time); break;
        }
        binding.txtFloorRoom.setText(
                String.format("%s: %s\n%s: %s\n%s: %s", getString(R.string.time_study), date_study, getString(R.string.study_time), studyTimeText, getString(R.string.classroom), room)
        );
    }

    // control device
    private void controlDevice(String device, boolean statusDevice) {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        referenceDevicesRoom = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room).child("deviceClassroom");
        referenceDevices = database.getReference().child("devices").child(floor).child(room);

        if (status != 2) {
            referenceDevicesRoom.child(device).setValue(statusDevice);
            referenceDevices.child(device).setValue(statusDevice);
        } else {
            Toast.makeText(requireContext(), getString(R.string.damaged_device), Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOnOffAllDevices(boolean isTurn) {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        referenceDevicesRoom = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room).child("deviceClassroom");
        referenceDevices = database.getReference().child("devices").child(floor).child(room);

        if (status != 2) {
            DeviceClassroom deviceClassroom = new DeviceClassroom(isTurn, isTurn, isTurn, isTurn);
            referenceDevicesRoom.setValue(deviceClassroom);
            referenceDevices.setValue(deviceClassroom);

            binding.layoutTurnAll.setBackgroundResource(isTurn ? R.color.blur_blue : R.color.blur_black);
            binding.txtTurnAll.setText(getString(isTurn ? R.string.off_all : R.string.on_all));
        }
    }

    // Check Status All Device
    private void checkStatusAllDevice() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(requireContext().getApplicationContext());
        if (!isInternetConnected) return;

        showLoadingDialog();

        referenceDevices = database.getReference().child("devices").child(floor).child(room);
        devicesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Kiểm tra cờ isUpdatingUI để tránh xung đột
                if (isUpdatingUI) return;

                isUpdatingUI = true; // Đặt cờ tránh lặp

                DeviceClassroom dv = snapshot.getValue(DeviceClassroom.class);
                if (dv == null) {
                    dismissLoadingDialog();
                    return;
                }

                boolean allDevicesOn = dv.isLight() && dv.isFan() && dv.isSpeaker() && dv.isProjector();

                // Tạm thời xóa listener trước khi setChecked để tránh kích hoạt listener
                binding.swAll.setOnCheckedChangeListener(null);
                binding.swAll.setChecked(allDevicesOn);
                binding.layoutTurnAll.setBackgroundResource(allDevicesOn ? R.color.blur_blue : R.color.blur_black);
                binding.txtTurnAll.setText(getString(allDevicesOn ? R.string.off_all : R.string.on_all));

                // Đặt lại listener sau khi hoàn tất cập nhật
                binding.swAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        turnOnOffAllDevices(isChecked);
                    }
                });

                // Cập nhật trạng thái của từng thiết bị
                status_light = dv.isLight();
                status_fan = dv.isFan();
                status_projector = dv.isProjector();
                status_speaker = dv.isSpeaker();

                // Gọi hàm cập nhật giao diện cho từng thiết bị
                checkStatusDevice(status_light, "layoutLight", "imgLight", "txtLight", R.drawable.light_on, R.drawable.light_off);
                checkStatusDevice(status_fan, "layoutFan", "imgFan", "txtFan", R.drawable.fan_on, R.drawable.fan_off);
                checkStatusDevice(status_projector, "layoutProjector", "imgProjector", "txtProjector", R.drawable.projector_on, R.drawable.projector_off);
                checkStatusDevice(status_speaker, "layoutSpeaker", "imgSpeaker", "txtSpeaker", R.drawable.amplifier, R.drawable.amplifier_off);

                isUpdatingUI = false; // Reset cờ khi hoàn tất
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                dismissLoadingDialog();
            }
        };

        referenceDevices.addValueEventListener(devicesEventListener);
    }

    // check status device
    private void checkStatusDevice(Boolean isOn, String layoutId, String imgId, String txtId, int imgIdOn, int imgIdOff) {
        LinearLayout layout = getLayout(binding, layoutId);
        ImageView img = getImageView(binding, imgId);
        TextView txt = getTextView(binding, txtId);

        img.setImageResource(isOn ? imgIdOn : imgIdOff);
        layout.setBackgroundColor(ContextCompat.getColor(requireContext(), isOn ? R.color.select : R.color.blank));
        txt.setText(getText(isOn ? R.string.on : R.string.off));
    }

    // get linearlayout id
    private LinearLayout getLayout(FragmentControlBinding binding, String layoutId) {
        int resourceId = binding.getRoot().getResources().getIdentifier(layoutId, "id", requireContext().getPackageName());
        return binding.getRoot().findViewById(resourceId);
    }

    // get textview id
    private TextView getTextView(FragmentControlBinding binding, String textId) {
        int resourceId = binding.getRoot().getResources().getIdentifier(textId, "id", requireContext().getPackageName());
        return binding.getRoot().findViewById(resourceId);
    }

    // get imageview id
    private ImageView getImageView(FragmentControlBinding binding, String imgId) {
        int resourceId = binding.getRoot().getResources().getIdentifier(imgId, "id", requireContext().getPackageName());
        return binding.getRoot().findViewById(resourceId);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (referenceDevices != null && devicesEventListener != null) {
            referenceDevices.removeEventListener(devicesEventListener);
        }
    }
}