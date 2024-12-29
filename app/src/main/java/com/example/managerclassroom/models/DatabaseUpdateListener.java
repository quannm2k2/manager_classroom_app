package com.example.managerclassroom.models;

public interface DatabaseUpdateListener {
    void onUpdateSuccess();
    void onUpdateFailure(String errorMessage);
}
