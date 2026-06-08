package com.fitify.service;

import com.fitify.dao.TrackDAO;
import com.fitify.interfaces.MediaPlayable;
import com.fitify.model.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

/**
 * MusicService - Concrete implementation of MediaPlayable using JavaFX MediaPlayer.
 * OOP Concept: ABSTRACTION (implements MediaPlayable which extends Trackable)
 */
public class MusicService implements MediaPlayable {

    private final TrackDAO   trackDAO = new TrackDAO();
    private MediaPlayer      player;
    private Playlist         currentPlaylist;
    private int              currentIndex = 0;
    private double           volume       = 0.7;
    private boolean          muted        = false;
    private boolean          active       = false;
    private final int        userId;

    public MusicService(int userId) { this.userId = userId; }

    @Override
    public void play(Track track) {
        stopPlayer();
        if (track == null || track.getFilePath() == null) return;
        try {
            String path = track.getFilePath();
            // Support both local files and HTTP preview URLs (Deezer)
            String uri = (path.startsWith("http://") || path.startsWith("https://"))
                ? path
                : new File(path).toURI().toString();
            Media media = new Media(uri);
            player = new MediaPlayer(media);
            player.setVolume(muted ? 0 : volume);
            player.setOnEndOfMedia(this::skip);
            player.play();
            active = true;
        } catch (Exception e) {
            System.err.println("MusicService.play: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        if (player != null) { player.play(); active = true; }
        else if (currentPlaylist != null && !currentPlaylist.getTracks().isEmpty()) {
            play(currentPlaylist.getTracks().get(currentIndex));
        }
    }

    @Override
    public void pause() {
        if (player != null) { player.pause(); active = false; }
    }

    @Override
    public void stop() { stopPlayer(); currentIndex = 0; }

    @Override
    public void skip() {
        if (currentPlaylist == null || currentPlaylist.getTracks().isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentPlaylist.getTracks().size();
        play(currentPlaylist.getTracks().get(currentIndex));
    }

    @Override
    public void previous() {
        if (currentPlaylist == null || currentPlaylist.getTracks().isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentPlaylist.getTracks().size())
                       % currentPlaylist.getTracks().size();
        play(currentPlaylist.getTracks().get(currentIndex));
    }

    @Override
    public void setVolume(double vol) {
        this.volume = Math.max(0, Math.min(1, vol));
        if (player != null && !muted) player.setVolume(this.volume);
    }

    @Override
    public void seek(double seconds) {
        if (player != null) player.seek(Duration.seconds(seconds));
    }

    @Override
    public Track getCurrentTrack() {
        if (currentPlaylist == null || currentPlaylist.getTracks().isEmpty()) return null;
        if (currentIndex >= currentPlaylist.getTracks().size()) return null;
        return currentPlaylist.getTracks().get(currentIndex);
    }

    @Override
    public boolean isMuted() { return muted; }

    @Override
    public void toggleMute() {
        muted = !muted;
        if (player != null) player.setVolume(muted ? 0 : volume);
    }

    @Override
    public double getProgress() {
        if (player == null) return 0;
        Duration total = player.getTotalDuration();
        Duration cur   = player.getCurrentTime();
        if (total == null || total.isUnknown() || total.toMillis() == 0) return 0;
        return (cur.toSeconds() / total.toSeconds()) * 100.0;
    }

    @Override
    public boolean isActive() { return active; }

    public void playPlaylist(Playlist pl) {
        stopPlayer();
        currentPlaylist = pl;
        currentIndex    = 0;
        if (!pl.getTracks().isEmpty()) play(pl.getTracks().get(0));
    }

    public void loadPlaylist(Playlist pl) {
        stopPlayer();
        currentPlaylist = pl;
        currentIndex    = 0;
    }

    public List<Playlist> getUserPlaylists()       { return trackDAO.findPlaylistsByUser(userId); }
    public boolean        savePlaylist(Playlist pl) { return trackDAO.savePlaylist(pl); }
    public List<Track>    getAllTracks()            { return trackDAO.findByUser(userId); }
    public boolean        addTrack(Track t)         { t.setUserId(userId); return trackDAO.save(t); }
    public boolean        deleteTrack(Track t)      { return trackDAO.delete(t.getTrackId()); }

    public Playlist getCurrentPlaylist() { return currentPlaylist; }
    public int      getCurrentIndex()    { return currentIndex; }
    public double   getVolume()          { return volume; }

    private void stopPlayer() {
        if (player != null) { player.stop(); player.dispose(); player = null; }
        active = false;
    }
}
