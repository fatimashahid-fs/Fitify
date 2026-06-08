package com.fitify.model;

/**
 * Track - Represents a single music track.
 * OOP Concept: ENCAPSULATION
 */
public class Track {
    private String spotifyId = "";

    private int    trackId;
    private int    userId;
    private String title;
    private String artist;
    private String album;
    private String filePath;
    private int    durationSec;
    private int    bpm;
    private String genre;

    public Track() {}

    public Track(String title, String artist, String filePath,
                 int durationSec, int bpm, String genre) {
        this.title       = title;
        this.artist      = artist;
        this.filePath    = filePath;
        this.durationSec = durationSec;
        this.bpm         = bpm;
        this.genre       = genre;
    }

    public String getFormattedDuration() {
        return String.format("%d:%02d", durationSec / 60, durationSec % 60);
    }

    public String getBpmCategory() {
        if (bpm < 100) return "Warm-Up";
        if (bpm < 130) return "Moderate";
        if (bpm < 160) return "High Intensity";
        return "Peak Performance";
    }

    public int    getTrackId()             { return trackId; }
    public void   setTrackId(int id)       { this.trackId = id; }
    public int    getUserId()              { return userId; }
    public void   setUserId(int id)        { this.userId = id; }
    public String getTitle()               { return title; }
    public void   setTitle(String t)       { this.title = t; }
    public String getArtist()              { return artist; }
    public void   setArtist(String a)      { this.artist = a; }
    public String getAlbum()               { return album; }
    public void   setAlbum(String a)       { this.album = a; }
    public String getFilePath()            { return filePath; }
    public void   setFilePath(String p)    { this.filePath = p; }
    public int    getDurationSec()         { return durationSec; }
    public void   setDurationSec(int d)    { this.durationSec = d; }
    public int    getBpm()                 { return bpm; }
    public void   setBpm(int bpm)          { this.bpm = bpm; }
    public String getGenre()               { return genre; }
    public void   setGenre(String g)       { this.genre = g; }

    @Override
    public String toString() {
        return title + " - " + artist + " (" + getFormattedDuration() + ")";
    }

    public String getSpotifyId()          { return spotifyId != null ? spotifyId : ""; }
    public void   setSpotifyId(String id)  { this.spotifyId = id; }
}
