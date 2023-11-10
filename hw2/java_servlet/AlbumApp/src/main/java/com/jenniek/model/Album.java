package com.jenniek.model;

public class Album {
    private String albumKey;
    private String title;
    private String artist;
    private String year;

    // Default constructor for deserialization
    public Album() {
    }

    // Constructor to initialize all fields
    public Album(String albumKey, String title, String artist, String year) {
        this.albumKey = albumKey;
        this.title = title;
        this.artist = artist;
        this.year = year;
    }

    // Getter and Setter for albumKey
    public String getAlbumKey() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for artist
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    // Getter and Setter for year
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumKey='" + albumKey + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
