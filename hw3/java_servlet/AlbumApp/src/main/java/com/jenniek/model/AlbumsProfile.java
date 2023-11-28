package com.jenniek.model;

import java.util.Objects;

public class AlbumsProfile {
    private String artist;
    private String title;
    private String year;

    // Default constructor is needed for Jackson deserialization
    public AlbumsProfile() {
    }

    public AlbumsProfile(String artist, String title, String year) {
        this.artist = artist;
        this.title = title;
        this.year = year;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumsProfile that = (AlbumsProfile) o;
        return Objects.equals(artist, that.artist) &&
                Objects.equals(title, that.title) &&
                Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist, title, year);
    }

    @Override
    public String toString() {
        return "AlbumsProfile{" +
                "artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
