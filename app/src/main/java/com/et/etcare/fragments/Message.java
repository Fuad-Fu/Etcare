package com.et.etcare.fragments;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    public static final int TYPE_PATIENT = 0;
    public static final int TYPE_DOCTOR = 1;

    private String text;
    private int senderType;
    private String senderId;
    @ServerTimestamp
    private Date serverTimestamp;
    private String timestampDisplay; // For local immediate display

    // Required no-arg constructor for Firestore
    public Message() {}

    public Message(String text, int senderType, String senderId, String timestampDisplay) {
        this.text = text;
        this.senderType = senderType;
        this.senderId = senderId;
        this.timestampDisplay = timestampDisplay;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getSenderType() { return senderType; }
    public void setSenderType(int senderType) { this.senderType = senderType; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public Date getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(Date serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public String getTimestampDisplay() { return timestampDisplay; }
    public void setTimestampDisplay(String timestampDisplay) { this.timestampDisplay = timestampDisplay; }
}