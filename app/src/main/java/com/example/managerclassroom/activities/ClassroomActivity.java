package com.example.managerclassroom.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.managerclassroom.R;
import com.example.managerclassroom.adapter.SubjectAdapter;
import com.example.managerclassroom.databinding.ActivityClassroomBinding;
import com.example.managerclassroom.methods.GetTimes;
import com.example.managerclassroom.methods.InternetCheck;
import com.example.managerclassroom.methods.NotificationsHelper;
import com.example.managerclassroom.models.Classroom;
import com.example.managerclassroom.models.DamagedRoom;
import com.example.managerclassroom.models.DatabaseUpdateListener;
import com.example.managerclassroom.models.DeviceClassroom;
import com.example.managerclassroom.models.ListSubject;
import com.example.managerclassroom.models.NotifiClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;

public class ClassroomActivity extends AppCompatActivity {

    ActivityClassroomBinding classroomBinding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private int status = 0;
    private String id, date_study, floorName, roomName, time_study, time_signup, teacher, subject;
    private boolean hiddenToggle5 = true, hiddenToggle6 = true;
    Dialog dialog;
    private ValueEventListener valueEventListener;
    private static final String[] FLOORS = {"5", "6"};
    private static final String[] ROOMS = {"01", "02", "03", "04", "05", "06"};
    private DatabaseReference referenceUser;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classroomBinding = ActivityClassroomBinding.inflate(getLayoutInflater());
        setContentView(classroomBinding.getRoot());

        // Get data from HomeFragment
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        time_study = intent.getStringExtra("time_study");
        date_study = intent.getStringExtra("date_study");
        Log.d("ID", id);

        // Khai báo DatabaseReference
        referenceUser = database.getReference().child("users").child(id);

        setClassroomTimeDisplay(); // Set text time

        for (String floor : FLOORS) {
            for (String room : ROOMS) {
                String roomNumber = floor + room;

                // Check status rooms
                LinearLayout layout = getView(classroomBinding, "layoutP" + roomNumber, LinearLayout.class);
                TextView txt = getView(classroomBinding, "txtP" + roomNumber, TextView.class);
                checkStatusClassroom(floor, roomNumber, layout, txt);

                // check damaged room
                checkDamagedRoom(floor, roomNumber);

                // Room item click
                String cvId = "cvRoom" + roomNumber;
                CardView cardView = getView(classroomBinding, cvId, CardView.class);
                cardView.setOnClickListener(v -> {
                    onClickItemRoom(floor, roomNumber);
                    floorName = floor;
                    roomName = roomNumber;
                });
            }
        }
        createDefaultClassrooms();
        // cardview floor5 click
        classroomBinding.cvFloor5.setOnClickListener(v -> {
            LinearLayout layout = getView(classroomBinding, "layoutListRoom5", LinearLayout.class);
            ImageView img = getView(classroomBinding, "imgDrop5", ImageView.class);
            onClickItemFloor(hiddenToggle5, layout, img);
            hiddenToggle5 = !hiddenToggle5;
        });

        // cardview floor6 click
        classroomBinding.cvFloor6.setOnClickListener(v -> {
            LinearLayout layout = getView(classroomBinding, "layoutListRoom6", LinearLayout.class);
            ImageView img = getView(classroomBinding, "imgDrop6", ImageView.class);
            onClickItemFloor(hiddenToggle6, layout, img);
            hiddenToggle6 = !hiddenToggle6;
        });

        // back
        classroomBinding.btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void setClassroomTimeDisplay() {
        int timeTextResId = R.string.morning_time;
        switch (time_study) {
            case "afternoon":
                timeTextResId = R.string.afternoon_time;
                break;
            case "evening":
                timeTextResId = R.string.evening_time;
                break;
        }
        classroomBinding.txtTime.setText(String.format("%s\n%s", date_study, getString(timeTextResId)));
    }

    private void createDefaultClassrooms() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference classroomsRef = database.getReference("devices");

        for (String floor : FLOORS) {
            DatabaseReference floorRef = classroomsRef.child( floor);

            for (int i = 1; i <= 6; i++) {
                @SuppressLint("DefaultLocale") String room = String.format("%02d", i); // Tạo số phòng từ 01 đến 06
                String roomPath = floor + room; // Đường dẫn phòng

                // Tạo một bản ghi Classroom với tất cả các thiết bị ở trạng thái false
                DeviceClassroom defaultDevices = new DeviceClassroom(false, false, false, false);

                // Cập nhật cấu trúc Firebase cho từng phòng
                DatabaseReference roomRef = floorRef.child(roomPath);
                roomRef.setValue(defaultDevices)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Firebase", "Created room: " + roomPath + " on floor: " + floor);
                            } else {
                                Log.e("Firebase", "Error creating room: " + roomPath + " on floor: " + floor);
                            }
                        });
            }
        }
    }


    // Check room is damaged?
    private void checkDamagedRoom(String floor,String room) {
        DatabaseReference referenceDamaged = database.getReference().child("Damaged rooms");
        referenceDamaged.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    DamagedRoom damagedRoom = dataSnapshot.getValue(DamagedRoom.class);
                    assert damagedRoom != null;
                    if(room.equals(damagedRoom.getRoom())){
                        // Phòng hỏng status = 2
                        int status = damagedRoom.getCodeStatusReport().equals(getString(R.string.code_not_handle)) ? 2 : 0;
                        createClassroom(status, floor, room, damagedRoom.getReport(), damagedRoom.getCodeStatusReport());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClassroomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // create classroom
    private void createClassroom(int status, String floor, String room, String report, String codeStatusReport) {
        DeviceClassroom deviceClassroom = new DeviceClassroom(false, false, false, false);
        DatabaseReference referenceRoom = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room);
        referenceRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Classroom classroom = snapshot.getValue(Classroom.class);
                if(classroom != null){
                    // Status = 1 nếu phòng đã đăng ký
                    referenceRoom.child("status").setValue((status == 0 && !classroom.getTime_signup().isEmpty()) ? 1 : status);
                    referenceRoom.child("report").setValue(report);
                    referenceRoom.child("codeStatusReport").setValue(codeStatusReport);
                } else {
                    classroom = new Classroom(status, floorName, roomName, date_study, time_study, "", "", "", report, codeStatusReport, deviceClassroom);
                    referenceRoom.setValue(classroom);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Click item to select room
    private void onClickItemRoom(String floor, String room) {
        boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
        if (!isInternetConnected) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference referenceRoom = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer status = snapshot.child("status").getValue(Integer.class);
                // Status 0: Clean room
                if (status == null || status == 0) {
                    createClassroom(0, floor, room, "", "");
                    showAlertDialog(floor, room);
                } else {
                    // Status 1: used room - Status 2: damaged room
                    Toast.makeText(ClassroomActivity.this,
                            getText(status == 1 ? R.string.room_full : R.string.room_repair),
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClassroomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        referenceRoom.addValueEventListener(valueEventListener);
    }

    // Check status rooms
    private void checkStatusClassroom(String floor, String room, LinearLayout layout, TextView txt) {
        DatabaseReference reference = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer status = snapshot.child("status").getValue(Integer.class);
                    assert status != null;
                    updateLayoutColors(status, layout, txt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClassroomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLayoutColors(Integer status, LinearLayout layout, TextView txt) {
        int color;
        switch (status) {
            case 1: // used room
                color = ContextCompat.getColor(ClassroomActivity.this, R.color.select);
                break;
            case 2: // damaged room
                color = ContextCompat.getColor(ClassroomActivity.this, R.color.damaged);
                break;
            default: // empty room (status = 0 or undefined)
                color = ContextCompat.getColor(ClassroomActivity.this, R.color.blank);
                break;
        }
        layout.setBackgroundColor(color);
        txt.setTextColor(color);
    }

    // Simplify get view methods
    private <T extends View> T getView(ActivityClassroomBinding binding, String viewId, Class<T> viewType) {
        @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier(viewId, "id", getPackageName());
        return viewType.cast(binding.getRoot().findViewById(resourceId));
    }

    // Confirm dialog
    private void showAlertDialog(String floor, String room) {
        if (dialog == null) {
            dialog = new Dialog(ClassroomActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.confirm_select);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edtSubject = dialog.findViewById(R.id.edtSubject);
        Button btnDialog = dialog.findViewById(R.id.btnConfirmRegis);
        Button btnSkip = dialog.findViewById(R.id.btnCancelRegis);
        Spinner spinner = dialog.findViewById(R.id.spinner);

        selectSubject(spinner, edtSubject);

        // Confirm select this room
        btnDialog.setOnClickListener(v -> {
            if (subject != null) {
                boolean isInternetConnected = InternetCheck.isInternetAvailable(getApplicationContext());
                if(isInternetConnected){
                    updateDatabase(subject, floor, room, new DatabaseUpdateListener() {
                        @Override
                        public void onUpdateSuccess() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            Toast.makeText(ClassroomActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getText(R.string.subject_warning), Toast.LENGTH_SHORT).show();
            }
        });
        // Cancel Dialog
        btnSkip.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Select subject from spinner
    private void selectSubject(Spinner spinner, EditText edtSubject) {
        SubjectAdapter subjectAdapter = new SubjectAdapter(getApplicationContext(), ListSubject.getSubjectList(this));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subject = parent.getItemAtPosition(position).toString();
                edtSubject.setVisibility(subject.equals(getString(R.string.others)) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // update data from edittext
        edtSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                subject = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinner.setAdapter(subjectAdapter);
    }

    // Update room and user data
    private void updateDatabase(String subject, String floor, String room, DatabaseUpdateListener listener) {
        time_signup = GetTimes.getTimeUpdate(ClassroomActivity.this);

        DatabaseReference referenceRoom = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floor).child(room);
        Classroom classroom = new Classroom(1, floorName, roomName, date_study, time_study, time_signup, teacher, subject, "", "", new DeviceClassroom(false, false, false, false));

        referenceRoom.setValue(classroom).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotifications();
                Toast.makeText(ClassroomActivity.this, getText(R.string.select_success), Toast.LENGTH_SHORT).show();
                listener.onUpdateSuccess();
            } else {
                listener.onUpdateFailure(Objects.requireNonNull(task.getException()).getMessage());
            }
        });

        // set username to Classrooms database
        referenceUser.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    teacher = snapshot.child("username").getValue(String.class);
                    // Create classroom and user data
                    referenceRoom.child("teacher").setValue(teacher);
                    Classroom classroom = new Classroom(status, floorName, roomName, date_study, time_study, time_signup, teacher, subject, "", "", new DeviceClassroom(false, false, false, false));
                    referenceUser.child("data").child(time_signup).setValue(classroom);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClassroomActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotifications() {
        NotificationsHelper.createNotifications(ClassroomActivity.this, "REGISTER", getString(R.string.noti), getString(R.string.select_success));

        String desc_noti = getString(R.string.noti_register) + " " + roomName;
        NotifiClass notifiClass = new NotifiClass(desc_noti, time_signup, 1);
        referenceUser.child("notifications").child(time_signup).setValue(notifiClass);
    }

    // Click item floor
    private void onClickItemFloor(boolean isHidden, LinearLayout layout, ImageView img) {
        layout.setVisibility(isHidden ? View.VISIBLE : View.GONE);
        img.setRotation(isHidden ? 180 : 0);
    }
    // close dialog
    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    // disconnect to firebase
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(floorName!=null && roomName!=null) {
            DatabaseReference classroomRef = database.getReference().child("Classrooms").child(date_study).child(time_study).child(floorName).child(roomName);
            classroomRef.removeEventListener(valueEventListener);
        }
    }
}