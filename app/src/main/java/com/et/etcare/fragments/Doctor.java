package com.et.etcare.fragments;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class Doctor implements Parcelable {
    private int id;
    private String name;
    private String specialty;
    private List<String> languages;
    private String emoji;
    private double rating;
    private int reviewCount;
    private String nextAvailableSlot;     // computed from availableSlots
    private List<String> availableSlots;  // all time slots
    private String bio;                  // short biography

    private String firestoreId;   // NEW FIELD

    // Add getter and setter
    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
    // Full constructor (used for Firestore or mock data)
    public Doctor(int id, String name, String specialty, List<String> languages,
                  String emoji, double rating, int reviewCount,
                  List<String> availableSlots, String bio) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.languages = languages;
        this.emoji = emoji;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.availableSlots = availableSlots != null ? availableSlots : new ArrayList<>();
        this.bio = bio != null ? bio : "";
        // compute next available slot from the list
        this.nextAvailableSlot = !this.availableSlots.isEmpty() ? this.availableSlots.get(0) : "Not available";
    }

    // Old constructor (for backward compatibility)
    public Doctor(int id, String name, String specialty, List<String> languages, String emoji) {
        this(id, name, specialty, languages, emoji, 0.0, 0, new ArrayList<>(), "");
    }

    // Parcelable constructor
    protected Doctor(Parcel in) {
        id = in.readInt();
        name = in.readString();
        specialty = in.readString();
        languages = new ArrayList<>();
        in.readStringList(languages);
        emoji = in.readString();
        rating = in.readDouble();
        reviewCount = in.readInt();
        nextAvailableSlot = in.readString();
        availableSlots = in.createStringArrayList();
        bio = in.readString();
        firestoreId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(specialty);
        dest.writeStringList(languages);
        dest.writeString(emoji);
        dest.writeDouble(rating);
        dest.writeInt(reviewCount);
        dest.writeString(nextAvailableSlot);
        dest.writeStringList(availableSlots);
        dest.writeString(bio);
        dest.writeString(firestoreId);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Doctor> CREATOR = new Creator<Doctor>() {
        @Override
        public Doctor createFromParcel(Parcel in) { return new Doctor(in); }
        @Override
        public Doctor[] newArray(int size) { return new Doctor[size]; }
    };

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public List<String> getLanguages() { return languages; }
    public String getEmoji() { return emoji; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public String getNextAvailableSlot() { return nextAvailableSlot; }
    public List<String> getAvailableSlots() { return availableSlots; }
    public String getBio() { return bio; }
}