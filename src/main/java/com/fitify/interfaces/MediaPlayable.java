package com.fitify.interfaces;

import com.fitify.model.Track;

/**
 * MediaPlayable - Contract for any audio playback engine.
 * OOP Concept: ABSTRACTION
 * Extends Trackable, implemented by: MusicService
 */
public interface MediaPlayable extends Trackable {
    void  play(Track track);
    void  skip();
    void  previous();
    void  setVolume(double volume);  // 0.0 to 1.0
    void  seek(double seconds);
    Track getCurrentTrack();
    boolean isMuted();
    void  toggleMute();
}
