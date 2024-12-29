package com.example.managerclassroom.models;

public class Classroom {
    private int status; // 0,1,2
    private String floorName;
    private String roomName;
    private String date_study;
    private String time_study;
    private String time_signup;
    private String teacher;
    private String subject;
    private String report;
    private String codeStatusReport;
    private DeviceClassroom deviceClassroom;

    public Classroom(int status, String floorName, String roomName, String date_study, String time_study, String time_signup, String teacher, String subject, String report, String codeStatusReport, DeviceClassroom deviceClassroom) {
        this.status = status;
        this.floorName = floorName;
        this.roomName = roomName;
        this.date_study = date_study;
        this.time_study = time_study;
        this.time_signup = time_signup;
        this.teacher = teacher;
        this.subject = subject;
        this.report = report;
        this.codeStatusReport = codeStatusReport;
        this.deviceClassroom = deviceClassroom;
    }

    public Classroom(){

    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDate_study() {
        return date_study;
    }

    public void setDate_study(String date_study) {
        this.date_study = date_study;
    }

    public String getTime_study() {
        return time_study;
    }

    public void setTime_study(String time_study) {
        this.time_study = time_study;
    }

    public String getTime_signup() {
        return time_signup;
    }

    public void setTime_signup(String time_signup) {
        this.time_signup = time_signup;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getCodeStatusReport() {
        return codeStatusReport;
    }

    public void setCodeStatusReport(String codeStatusReport) {
        this.codeStatusReport = codeStatusReport;
    }

    public DeviceClassroom getDeviceClassroom() {
        return deviceClassroom;
    }

    public void setDeviceClassroom(DeviceClassroom deviceClassroom) {
        this.deviceClassroom = deviceClassroom;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloor(String  floorName) {
        this.floorName = floorName;
    }
}
