package com.et.etcare.fragments;

import android.os.Parcel;
import android.os.Parcelable;

public class Article implements Parcelable {
    private int id;
    private String title;
    private String snippet;
    private String body;          // full article text
    private String category;
    private String emoji;         // icon emoji
    private int readTimeMinutes;

    public Article(int id, String title, String snippet, String body,
                   String category, String emoji, int readTimeMinutes) {
        this.id = id;
        this.title = title;
        this.snippet = snippet;
        this.body = body;
        this.category = category;
        this.emoji = emoji;
        this.readTimeMinutes = readTimeMinutes;
    }

    protected Article(Parcel in) {
        id = in.readInt();
        title = in.readString();
        snippet = in.readString();
        body = in.readString();
        category = in.readString();
        emoji = in.readString();
        readTimeMinutes = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(snippet);
        dest.writeString(body);
        dest.writeString(category);
        dest.writeString(emoji);
        dest.writeInt(readTimeMinutes);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) { return new Article(in); }
        @Override
        public Article[] newArray(int size) { return new Article[size]; }
    };

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSnippet() { return snippet; }
    public String getBody() { return body; }
    public String getCategory() { return category; }
    public String getEmoji() { return emoji; }
    public int getReadTimeMinutes() { return readTimeMinutes; }
}