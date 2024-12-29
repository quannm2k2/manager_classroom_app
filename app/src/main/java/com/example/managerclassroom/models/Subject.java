package com.example.managerclassroom.models;

import java.io.Serializable;

public class Subject implements Serializable {

    private String subject;

    public Subject(String subject) {
        this.subject = subject;
    }

    public Subject() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
