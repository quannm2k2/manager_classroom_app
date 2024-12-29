package com.example.managerclassroom.models;

public class DeviceClassroom {
    private boolean light;
    private boolean fan;
    private boolean projector;
    private boolean speaker;

    public DeviceClassroom(boolean light, boolean fan, boolean projector, boolean speaker) {
        this.light = light;
        this.fan = fan;
        this.projector = projector;
        this.speaker = speaker;
    }

    public DeviceClassroom(){

    }
    public boolean isLight() {
        return light;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public boolean isFan() {
        return fan;
    }

    public void setFan(boolean fan) {
        this.fan = fan;
    }

    public boolean isProjector() {
        return projector;
    }

    public void setProjector(boolean projector) {
        this.projector = projector;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public void setSpeaker(boolean speaker) {
        this.speaker = speaker;
    }
}
