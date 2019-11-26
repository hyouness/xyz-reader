package com.example.xyzreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Article implements Parcelable {
    private long id;
    private String title;
    private String byline;
    private String author;
    private String body;
    private String photoUrl;
    private Date publishDate;

    public Article(long id, String title, String byline, String author, String body, String photoUrl, Date publishDate) {
        this.id = id;
        this.title = title;
        this.byline = byline;
        this.author = author;
        this.body = body;
        this.photoUrl = photoUrl;
        this.publishDate = publishDate;
    }

    private Article(Parcel in) {
        id = in.readLong();
        title = in.readString();
        byline = in.readString();
        author = in.readString();
        body = in.readString();
        photoUrl = in.readString();
    }

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getByline() {
        return byline;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(byline);
        dest.writeString(author);
        dest.writeString(body);
        dest.writeString(photoUrl);
    }
}
