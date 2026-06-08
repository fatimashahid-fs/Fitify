package com.fitify.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlist - An ordered collection of Tracks.
 * OOP Concept: COMPOSITION (has-a List of Track)
 */
public class Playlist {

    private int         playlistId;
    private int         userId;
    private String      name;
    private String      moodTag;
    private String      workoutType;
    private List<Track> tracks;

    public Playlist() { this.tracks = new ArrayList<>(); }

    public Playlist(int userId, String name, String moodTag, String workoutType) {
        this();
        this.userId      = userId;
        this.name        = name;
        this.moodTag     = moodTag;
        this.workoutType = workoutType;
    }

    public void addTrack(Track t)    { tracks.add(t); }
    public void removeTrack(int idx) { if (idx >= 0 && idx < tracks.size()) tracks.remove(idx); }

    public int getTotalDurationSec() {
        return tracks.stream().mapToInt(Track::getDurationSec).sum();
    }

    public String getFormattedTotalDuration() {
        int secs = getTotalDurationSec();
        int hrs  = secs / 3600;
        int mins = (secs % 3600) / 60;
        return hrs > 0 ? hrs + "h " + mins + "m" : mins + " min";
    }

    public double getAverageBpm() {
        if (tracks.isEmpty()) return 0;
        return tracks.stream().mapToInt(Track::getBpm).average().orElse(0);
    }

    public int         getPlaylistId()              { return playlistId; }
    public void        setPlaylistId(int id)        { this.playlistId = id; }
    public int         getUserId()                  { return userId; }
    public void        setUserId(int uid)           { this.userId = uid; }
    public String      getName()                    { return name; }
    public void        setName(String n)            { this.name = n; }
    public String      getMoodTag()                 { return moodTag; }
    public void        setMoodTag(String m)         { this.moodTag = m; }
    public String      getWorkoutType()             { return workoutType; }
    public void        setWorkoutType(String w)     { this.workoutType = w; }
    public List<Track> getTracks()                  { return tracks; }
    public void        setTracks(List<Track> t)     { this.tracks = t; }

    @Override
    public String toString() {
        return name + " [" + moodTag + "] - " + tracks.size() + " tracks";
    }
}
