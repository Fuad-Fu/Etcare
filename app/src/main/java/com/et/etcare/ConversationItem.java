package com.et.etcare;

public class ConversationItem {
    private int appointmentId;
    private String doctorName;
    private String emoji;
    private String lastMessage;

    public ConversationItem(int appointmentId, String doctorName, String emoji, String lastMessage) {
        this.appointmentId = appointmentId;
        this.doctorName = doctorName;
        this.emoji = emoji;
        this.lastMessage = lastMessage;
    }

    public int getAppointmentId() { return appointmentId; }
    public String getDoctorName() { return doctorName; }
    public String getEmoji() { return emoji; }
    public String getLastMessage() { return lastMessage; }
}