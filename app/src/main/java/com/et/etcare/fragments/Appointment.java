package com.et.etcare.fragments;

import android.os.Parcel;
import android.os.Parcelable;

public class Appointment implements Parcelable {
    private int id;
    private String firestoreId;
    private String doctorId; // Added to identify the doctor for slot management
    private String doctorName;
    private String dateTimeDisplay;
    private long dateTimeMillis;
    private String type;
    private String status;
    private String emoji;

    public Appointment(int id, String doctorName, String dateTimeDisplay,
                       long dateTimeMillis, String type, String status, String emoji) {
        this.id = id;
        this.doctorName = doctorName;
        this.dateTimeDisplay = dateTimeDisplay;
        this.dateTimeMillis = dateTimeMillis;
        this.type = type;
        this.status = status;
        this.emoji = emoji;
    }

    protected Appointment(Parcel in) {
        id = in.readInt();
        firestoreId = in.readString();
        doctorId = in.readString();
        doctorName = in.readString();
        dateTimeDisplay = in.readString();
        dateTimeMillis = in.readLong();
        type = in.readString();
        status = in.readString();
        emoji = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(firestoreId);
        dest.writeString(doctorId);
        dest.writeString(doctorName);
        dest.writeString(dateTimeDisplay);
        dest.writeLong(dateTimeMillis);
        dest.writeString(type);
        dest.writeString(status);
        dest.writeString(emoji);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) { return new Appointment(in); }
        @Override
        public Appointment[] newArray(int size) { return new Appointment[size]; }
    };

    public int getId() { return id; }
    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDateTimeDisplay() { return dateTimeDisplay; }
    public long getDateTimeMillis() { return dateTimeMillis; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getEmoji() { return emoji; }
    public void setStatus(String status) { this.status = status; }
}