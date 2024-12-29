package com.example.managerclassroom.models;

public class DamagedRoom {
    private String room;
    private String report;
    private String time_report;
    private String date_study;
    private String time_study;
    private String code_status_report;
    private String time_signup;
    private String teacher;
    private String subject;

    public DamagedRoom(String room, String report, String code_status_report, String time_report, String date_study, String time_study, String teacher, String subject, String time_signup) {
        this.room = room;
        this.report = report;
        this.time_report = time_report;
        this.date_study = date_study;
        this.time_study = time_study;
        this.code_status_report = code_status_report;
        this.teacher = teacher;
        this.subject = subject;
        this.time_signup = time_signup;
    }
    public DamagedRoom(){
    }
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getCodeStatusReport() {
        return code_status_report;
    }

    public void setCodeStatusReport(String code_status_report) {
        this.code_status_report = code_status_report;
    }

    public String getTimeSignup() {
        return time_signup;
    }

    public void setTimeSignup(String time_signup) {
        this.time_signup = time_signup;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTime_report() {
        return time_report;
    }

    public void setTime_report(String time_report) {
        this.time_report = time_report;
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
}
