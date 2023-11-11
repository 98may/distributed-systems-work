package com.jenniek.model;

public class Album {
    private String albumID; // Changed from albumKey to albumID to match your table schema
    private String title;
    private String artist;
    private String year;
    private Long imageSize; 

    public Album() {
    }

    // Constructor to initialize all fields
    public Album(String albumID, String title, String artist, String year, Long imageSize) {
        this.albumID = albumID;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.imageSize = imageSize; // Initialize the imageSize
    }

    // Getter for albumID
    public String getAlbumID() {
        return albumID;
    }

    // Setter for albumID
    public void setAlbumID(String albumID) {
        this.albumID = albumID;
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

    // Getter and Setter for imageSize
    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumID='" + albumID + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", year='" + year + '\'' +
                ", imageSize=" + imageSize +
                '}';
    }
}
