package com.example.managerclassroom.models;

public class NotifiClass {
    private String description;
    private String time_noti;
    private int status; //0:cancel register, 1:success register, 2:report room

    public NotifiClass(String description, String time_noti, int status) {
        this.description = description;
        this.time_noti = time_noti;
        this.status = status;
    }

    public NotifiClass() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime_noti() {
        return time_noti;
    }

    public void setTime_noti(String time_noti) {
        this.time_noti = time_noti;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
