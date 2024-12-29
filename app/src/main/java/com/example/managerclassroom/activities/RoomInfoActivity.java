package com.example.managerclassroom.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.databinding.ActivityRoomInfoBinding;
import com.example.managerclassroom.databinding.ReportDialogBinding;
import com.example.managerclassroom.methods.GetTimes;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.methods.NotificationsHelper;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.DamagedRoom;
import com.example.managerclassroom.models.DeviceClassroom;
import com.example.managerclassroom.models.NotifiClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RoomInfoActivity extends AppCompatActivity {

    private ActivityRoomInfoBinding binding;
    String id, room, timeStudy, teacher, timeSignup, subject, dateStudy, floor;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referenceRoom, referenceUser, referenceDamaged, referenceDevices;
    private final StringBuilder report = new StringBuilder();
    private Dialog dialog;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from HomeFragment
        Intent getIntent = getIntent();
        id = getIntent.getStringExtra("id");
        room = getIntent.getStringExtra("room");
        timeStudy = getIntent.getStringExtra("time_study");
        dateStudy = getIntent.getStringExtra("date_study");
        timeSignup = getIntent.getStringExtra("time_signup");
        subject = getIntent.getStringExtra("subject");
        teacher = getIntent.getStringExtra("teacher");
        floor = getIntent.getStringExtra("floor");

        // Set room information
        binding.txtRoomInfo.setText(room); // Phòng học
        binding.txtTimeInfo.setText(getTimePeriod(timeStudy)); // Ca học
        binding.txtDateStudyInfo.setText(dateStudy); // Ngày học
        binding.txtSubjectInfo.setText(subject); // Lớp học phần
        binding.txtTeacherInfo.setText(teacher); // Giảng viên
        binding.txtTimeSignup.setText(timeSignup); // Thời gian đăng kí

        // Set up Firebase reference
        referenceDamaged = database.getReference().child("Damaged rooms").child(room);
        referenceUser = database.getReference().child("users").child(id);
        referenceRoom = database.getReference().child("Classrooms").child(dateStudy).child(timeStudy).child(floor).child(room);

        checkDamagedRoom();

        // Button report
        binding.btnReport.setOnClickListener(v -> {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
            if (!isInternetConnected) {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            // check date study valid
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (GetTimes.isTimeValid(dateStudy) == 0) {
                    showReportDialog(); // Hiện thị Report Dialog
                } else {
                    Toast.makeText(this, getString(R.string.not_date_study), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle Report
        binding.btnHandledReport.setOnClickListener(v -> {
            boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
            if (!isInternetConnected) {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (GetTimes.isTimeValid(dateStudy) >= 0) {
                    String statusReport = binding.statusReport.getText().toString();
                    if(statusReport.isEmpty()){
                        Toast.makeText(this, getString(R.string.dont_have_report), Toast.LENGTH_SHORT).show();
                    } else if(statusReport.equals(getString(R.string.handled))){
                        Toast.makeText(this, getString(R.string.handled_report), Toast.LENGTH_SHORT).show();
                    } else {
                        markReportAsHandled();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.not_date_study), Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Button back
        binding.btnBackInfo.setOnClickListener(v -> finish());
    }

    // Get Time Period Text
    private String getTimePeriod(String timeStudy) {
        switch (timeStudy) {
            case "morning": return getString(R.string.morning_time);
            case "afternoon": return getString(R.string.afternoon_time);
            case "evening": return getString(R.string.evening_time);
            default: return "";
        }
    }

    // Report dialog
    private void showReportDialog() {
        if(dialog == null){
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ReportDialogBinding reportDialogBinding = ReportDialogBinding.inflate(getLayoutInflater());
            dialog.setContentView(reportDialogBinding.getRoot());

            CheckBox checkLight = reportDialogBinding.cbLight;
            CheckBox checkSpeaker = reportDialogBinding.cbSpeaker;
            CheckBox checkFan = reportDialogBinding.cbFan;
            CheckBox checkProjector = reportDialogBinding.cbProjector;
            CheckBox checkOther = reportDialogBinding.cbOthers;
            EditText txtContentOther = reportDialogBinding.edtReport;
            Button btnClose = reportDialogBinding.btnSkipReport;
            Button btnConfirm = reportDialogBinding.btnConfirmReport;

            checkOther.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if(isChecked){
                    setCheckBoxesEnabled(checkLight, checkSpeaker, checkFan, checkProjector);
                }
                txtContentOther.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });

            btnClose.setOnClickListener(view -> dialog.dismiss());

            btnConfirm.setOnClickListener(view -> {
                if (checkOther.isChecked()) {
                    if (txtContentOther.getText().toString().isEmpty()) {
                        Toast.makeText(RoomInfoActivity.this, getString(R.string.enter_other_content), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        report.append(txtContentOther.getText().toString()).append(".");
                    }
                } else {
                    if (checkLight.isChecked()) report.append(checkLight.getText().toString());
                    if (checkSpeaker.isChecked()) report.append(checkSpeaker.getText().toString());
                    if (checkFan.isChecked()) report.append(checkFan.getText().toString());
                    if (checkProjector.isChecked()) report.append(checkProjector.getText().toString());
                }

                if(report.length() == 0){
                    Toast.makeText(RoomInfoActivity.this, getText(R.string.choose_problem), Toast.LENGTH_SHORT).show();
                } else {
                    databaseProcess();
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void setCheckBoxesEnabled(CheckBox... checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setChecked(false);
        }
    }

    // Checkbox checking -- Thieu Check Internet
    private void databaseProcess(){
        String timeReport = GetTimes.getTimeUpdate(RoomInfoActivity.this);
        DamagedRoom damagedRoom = new DamagedRoom(room, report.toString(), getString(R.string.code_not_handle), timeReport, dateStudy, timeStudy, teacher, subject, timeSignup);
        referenceDamaged.setValue(damagedRoom);

        referenceRoom.child("report").setValue(report.toString());
        referenceRoom.child("codeStatusReport").setValue(getString(R.string.code_not_handle));
        referenceRoom.child("status").setValue(2);

        referenceUser.child("data").child(timeSignup).child("report").setValue(report.toString());
        referenceUser.child("data").child(timeSignup).child("codeStatusReport").setValue(getString(R.string.code_not_handle));
        referenceUser.child("data").child(timeSignup).child("status").setValue(2);

        // Xử lý tắt all devices khi có báo cáo hư hỏng
        referenceDevices = database.getReference().child("devices").child(floor).child(room);
        DeviceClassroom deviceClassroom = new DeviceClassroom(false, false, false, false);
        referenceDevices.setValue(deviceClassroom);

        // Tạo thông báo User
        String time_notification = GetTimes.getTimeUpdate(RoomInfoActivity.this);
        String desc_notification = getString(R.string.classroom) + " " + room + " " + getString(R.string.has_damaged);

        // Thông báo trên Database
        NotifiClass notifiClass = new NotifiClass(desc_notification, time_notification, 2);
        referenceUser.child("notifications").child(time_notification).setValue(notifiClass);

        // Thông báo trên App
        NotificationsHelper.createNotifications(RoomInfoActivity.this, "REPORT_PROBLEM",
                getString(R.string.noti), desc_notification);

        Toast.makeText(this, getText(R.string.saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    // Đánh dấu báo cáo là "Đã xử lý"
    private void markReportAsHandled() {
        String handleSuccess = getString(R.string.handled_report_success);

        referenceDamaged.child("codeStatusReport").setValue(getString(R.string.code_handled));

        referenceRoom.child("codeStatusReport").setValue(getString(R.string.code_handled));
        referenceRoom.child("status").setValue(1);

        referenceUser.child("data").child(timeSignup).child("codeStatusReport").setValue(report.toString());
        referenceUser.child("data").child(timeSignup).child("status").setValue(1);

        // Tạo thông báo User
        String time_notification = GetTimes.getTimeUpdate(RoomInfoActivity.this);
        String desc_notification = getString(R.string.classroom) + " " + room + ": " + handleSuccess;

        // Thông báo trên Database
        NotifiClass notifiClass = new NotifiClass(desc_notification, time_notification, 1);
        referenceUser.child("notifications").child(time_notification).setValue(notifiClass);

        // Thông báo trên App
        NotificationsHelper.createNotifications(RoomInfoActivity.this, "HANDLE_REPORT",
                getString(R.string.noti), desc_notification);

        Toast.makeText(this, getText(R.string.handled_report_success), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkDamagedRoom() {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();

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
                                loadClassroom(2,problem, codeStatusReport);
                            } else {
                                loadClassroom(1,problem, codeStatusReport);
                            }
                        }
                    }
                }
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomInfoActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                dismissLoadingDialog();
            }
        });
    }

    // create classroom
    private void loadClassroom(int status, String report, String CodeStatusReport) {
        DatabaseReference referenceRoom = database.getReference().child("Classrooms").child(dateStudy).child(timeStudy).child(floor).child(room);
        referenceRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Classroom classroom = snapshot.getValue(Classroom.class);
                if(classroom != null){
                    referenceRoom.child("status").setValue(status);
                    referenceRoom.child("report").setValue(report);
                    referenceRoom.child("codeStatusReport").setValue(CodeStatusReport);

                    binding.txtReportInfo.setText(report);
                    binding.statusReport.setText(CodeStatusReport.equals(getString(R.string.code_handled)) ? R.string.handled : R.string.not_handle);
                }
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissLoadingDialog();
            }
        });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder loadingAlertDialog = new AlertDialog.Builder(this);
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

    // close dialog
    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}