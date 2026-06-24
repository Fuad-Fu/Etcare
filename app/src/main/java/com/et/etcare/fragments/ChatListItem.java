package com.et.etcare.fragments;

public class ChatListItem {
    private int appointmentId;
    private String doctorName;
    private String emoji;
    private String lastMessage;
    private String time;
    private String status; // "active" or "completed"

    public ChatListItem(int appointmentId, String doctorName, String emoji,
                        String lastMessage, String time, String status) {
        this.appointmentId = appointmentId;
        this.doctorName = doctorName;
        this.emoji = emoji;
        this.lastMessage = lastMessage;
        this.time = time;
        this.status = status;
    }

    // Getters (required for the adapter)
    public int getAppointmentId() {
        return appointmentId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}